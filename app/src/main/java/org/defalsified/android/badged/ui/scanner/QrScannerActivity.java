package org.defalsified.android.badged.ui.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.services.BadgeService;
import org.defalsified.android.badged.services.Cert;
import org.defalsified.android.badged.ui.badges.BadgeDetailActivity;
import org.defalsified.android.badged.ui.badges.BadgeGalleryActivity;
import org.defalsified.android.badged.utils.QrCodeParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerActivity extends AppCompatActivity {
    private static final String TAG = "QrScannerActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 10;

    // UI Components
    private PreviewView previewView;
    private TextView statusTextView;
    private Button galleryButton;
    private View loadingView;

    // Camera and analysis
    private ExecutorService cameraExecutor;

    // Services
    private BadgeService badgeService;
    private Cert certHandler;

    // Processing state
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        // Initialize services
        badgeService = new BadgeService(this);
        certHandler = new Cert();

        // Setup UI
        initializeViews();

        // Setup camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Request camera permission if needed
        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    // Initialize UI components
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        statusTextView = findViewById(R.id.status_text);
        galleryButton = findViewById(R.id.gallery_button);
        loadingView = findViewById(R.id.loading_view);

        galleryButton.setOnClickListener(v -> openBadgeGallery());
        updateStatusText();
    }

    // Update status message
    private void updateStatusText() {
        statusTextView.setText(R.string.scanner_ready);
    }

    // Check camera permission
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Setup and start camera
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Setup preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Setup QR code analysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Create QR scanner analyzer
                QrCodeAnalyzer qrCodeAnalyzer = new QrCodeAnalyzer(
                        qrContent -> runOnUiThread(() -> processQrContent(qrContent)));
                imageAnalysis.setAnalyzer(cameraExecutor, qrCodeAnalyzer);

                // Select back camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Unbind existing use cases
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Process scanned QR content
    private void processQrContent(String qrContent) {
        // Prevent multiple simultaneous processing
        if (isProcessing) return;

        isProcessing = true;

        try {
            showLoading(true);
            statusTextView.setText("Processing voucher...");

            // Decode base64 content
            byte[] decodedBytes = Base64.decode(qrContent, Base64.DEFAULT);
            Log.d(TAG, "Decoded QR Content (Hex): " + bytesToHex(decodedBytes));

            // Deserialize certificate
            String jsonContent = certHandler.deserialize(decodedBytes);
            if (jsonContent == null || jsonContent.isEmpty()) {
                showError("Certificate deserialization failed");
                cleanupCertificate();
                isProcessing = false;
                return;
            }

            Log.d(TAG, "Deserialized JSON: " + jsonContent);

            // Verify certificate
            statusTextView.setText("Verifying voucher...");
            boolean isValid = certHandler.verify();

            if (!isValid) {
                showError("Certificate verification failed");
                cleanupCertificate();
                isProcessing = false;
                return;
            }

            Log.d(TAG, "Certificate verification successful");

            try {
                // Parse JSON data
                JSONObject certJson = new JSONObject(jsonContent);

                // Create QR data for badge service
                JSONObject qrData = new JSONObject();
                qrData.put("serial", certJson.optString("serial", "unknown"));
                qrData.put("offer", certJson.optString("offer", "Unknown Offer"));
                qrData.put("holder", certJson.optString("holder", "Anonymous"));
                qrData.put("project", certJson.optString("project", "Unknown Project"));
                qrData.put("cert", qrContent);

                statusTextView.setText("Redeeming voucher...");
                processBadgeQrCode(qrData);

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                showError("Error parsing certificate data");
                cleanupCertificate();
                isProcessing = false;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding base64: " + e.getMessage(), e);
            showError("Invalid QR code format");
            isProcessing = false;
        } catch (Exception e) {
            Log.e(TAG, "Error processing QR code: " + e.getMessage(), e);
            showError("Error processing QR code");
            cleanupCertificate();
            isProcessing = false;
        }
    }

    // Process badge creation/retrieval
    private void processBadgeQrCode(JSONObject qrData) {
        if (!QrCodeParser.isBadgeQrCode(qrData)) {
            showError("Invalid voucher QR code");
            cleanupCertificate();
            isProcessing = false;
            return;
        }

        badgeService.mintBadge(qrData, new BadgeService.BadgeCallback() {
            @Override
            public void onSuccess(Badge badge, boolean isNewBadge) {
                showLoading(false);
                cleanupCertificate();

                // Show success message
                String message = isNewBadge
                        ? "Voucher redeemed successfully!"
                        : "This voucher was already redeemed";
                Toast.makeText(QrScannerActivity.this, message, Toast.LENGTH_LONG).show();

                // Launch badge detail activity
                Intent intent = new Intent(QrScannerActivity.this, BadgeDetailActivity.class);
                intent.putExtra(BadgeDetailActivity.EXTRA_BADGE_SERIAL, badge.getSerial());
                intent.putExtra(BadgeDetailActivity.EXTRA_IS_NEW_BADGE, isNewBadge);
                intent.putExtra(BadgeDetailActivity.EXTRA_VERIFICATION_STATUS, "VERIFIED");
                startActivity(intent);

                isProcessing = false;
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
                cleanupCertificate();
                isProcessing = false;
            }
        });
    }

    // Cleanup certificate resources
    private void cleanupCertificate() {
        try {
            certHandler.destroy();
            Log.d(TAG, "Certificate resources cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up certificate: " + e.getMessage(), e);
        }
    }

    // Show/hide loading indicator
    private void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
            if (loadingView != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
    }

    // Show error message
    private void showError(String message) {
        runOnUiThread(() -> {
            showLoading(false);
            Toast.makeText(QrScannerActivity.this, message, Toast.LENGTH_SHORT).show();
            statusTextView.setText("Error: " + message);
        });
    }

    // Navigate to badge gallery
    private void openBadgeGallery() {
        Intent intent = new Intent(this, BadgeGalleryActivity.class);
        startActivity(intent);
    }

    // Convert bytes to hex string for logging
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clean up certificate when activity is paused
        cleanupCertificate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up certificate and camera executor
        cleanupCertificate();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}