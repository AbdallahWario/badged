package org.defalsified.android.badged.ui.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private static final long SCANNING_COOLDOWN_MS = 2000; // 2s cooldown between scans

    // UI Components
    private PreviewView previewView;
    private TextView statusTextView;
    private Button galleryButton;
    private View loadingView;

    // Camera and analysis
    private ExecutorService cameraExecutor;
    private QrCodeAnalyzer qrCodeAnalyzer;
    private ProcessCameraProvider cameraProvider;

    // Services
    private BadgeService badgeService;
    private Cert certHandler;

    // Processing state
    private boolean isProcessing = false;
    private long lastProcessingTime = 0;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        // Init services
        badgeService = new BadgeService(this);
        certHandler = new Cert();
        mainHandler = new Handler(Looper.getMainLooper());

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

    // Init UI components
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        statusTextView = findViewById(R.id.status_text);
        galleryButton = findViewById(R.id.gallery_button);
        loadingView = findViewById(R.id.loading_view);

        galleryButton.setOnClickListener(v -> openBadgeGallery());
        updateStatusText(R.string.scanner_ready);
    }

    // Update status with resource ID
    private void updateStatusText(int stringResId) {
        statusTextView.setText(stringResId);
    }

    // Update status with custom text
    private void updateStatusText(String message) {
        statusTextView.setText(message);
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
                cameraProvider = cameraProviderFuture.get();

                // Setup preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Setup QR code analysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                //  QR analyzer with cooldown
                qrCodeAnalyzer = new QrCodeAnalyzer(qrContent -> {
                    // Only process if not busy and after cooldown
                    if (!isProcessing && System.currentTimeMillis() > lastProcessingTime + SCANNING_COOLDOWN_MS) {
                        mainHandler.post(() -> processQrContent(qrContent));
                    }
                });

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

    // Pause camera during processing
    private void pauseCameraScanning() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    // Resume camera after processing
    private void resumeCameraScanning() {
        startCamera();
    }

    // Process scanned QR content
    private void processQrContent(String qrContent) {
        // Set processing state
        isProcessing = true;
        lastProcessingTime = System.currentTimeMillis();

        // Pause camera during processing
        pauseCameraScanning();

        // Show loading UI
        showLoading(true);
        updateStatusText("Processing voucher...");

        // Process in background
        ExecutorService processingExecutor = Executors.newSingleThreadExecutor();
        processingExecutor.execute(() -> {
            Cert.CertificateResult result = null;

            try {
                // Decode base64
                byte[] decodedBytes = Base64.decode(qrContent, Base64.DEFAULT);
                Log.d(TAG, "Decoded QR Content (Hex): " + bytesToHex(decodedBytes));

                // Process certificate
                result = certHandler.processAndCleanup(decodedBytes);

                if (!result.success) {
                    String errorMessage = result.error != null ? result.error : "Certificate processing failed";
                    mainHandler.post(() -> {
                        showError(errorMessage);
                        finishProcessing();
                    });
                    return;
                }

                try {
                    // Parse JSON
                    String jsonContent = result.jsonContent;
                    JSONObject certJson = new JSONObject(jsonContent);

                    // Create QR data
                    JSONObject qrData = new JSONObject();
                    qrData.put("serial", certJson.optString("serial", "unknown"));
                    qrData.put("offer", certJson.optString("offer", "Unknown Offer"));
                    qrData.put("holder", certJson.optString("holder", "Anonymous"));
                    qrData.put("project", certJson.optString("project", "Unknown Project"));
                    qrData.put("cert", qrContent);

                    // Update status
                    mainHandler.post(() -> updateStatusText("Redeeming voucher..."));

                    // Process badge
                    processBadgeQrCode(qrData);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                    mainHandler.post(() -> {
                        showError("Error parsing certificate data");
                        finishProcessing();
                    });
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error decoding base64: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showError("Invalid QR code format");
                    finishProcessing();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error processing QR code: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showError("Error processing QR code");
                    finishProcessing();
                });
            }
        });

        processingExecutor.shutdown();
    }

    // Process badge creation/retrieval
    private void processBadgeQrCode(JSONObject qrData) {
        if (!QrCodeParser.isBadgeQrCode(qrData)) {
            mainHandler.post(() -> {
                showError("Invalid voucher QR code");
                finishProcessing();
            });
            return;
        }

        badgeService.mintBadge(qrData, new BadgeService.BadgeCallback() {
            @Override
            public void onSuccess(Badge badge, boolean isNewBadge) {
                mainHandler.post(() -> {
                    // Hide loading
                    showLoading(false);

                    // Create message based on badge status
                    String message = isNewBadge
                            ? "Voucher redeemed successfully!"
                            : "This voucher was already redeemed";

                    // Show toast
                    Toast.makeText(QrScannerActivity.this, message, Toast.LENGTH_LONG).show();

                    // Launch badge detail
                    Intent intent = new Intent(QrScannerActivity.this, BadgeDetailActivity.class);
                    intent.putExtra(BadgeDetailActivity.EXTRA_BADGE_SERIAL, badge.getSerial());
                    intent.putExtra(BadgeDetailActivity.EXTRA_IS_NEW_BADGE, isNewBadge);
                    intent.putExtra(BadgeDetailActivity.EXTRA_VERIFICATION_STATUS, "VERIFIED");
                    startActivity(intent);

                    // Reset state
                    finishProcessing();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    showError(errorMessage);
                    finishProcessing();
                });
            }
        });
    }

    // Reset processing state
    private void finishProcessing() {
        showLoading(false);
        isProcessing = false;
        updateStatusText(R.string.scanner_ready);

        // Resume camera after delay
        mainHandler.postDelayed(this::resumeCameraScanning, 500);
    }

    // Show/hide loading
    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    // Show error message
    private void showError(String message) {
        showLoading(false);
        Toast.makeText(QrScannerActivity.this, message, Toast.LENGTH_SHORT).show();
        updateStatusText("Error: " + message);
    }

    // Go to badge gallery
    private void openBadgeGallery() {
        Intent intent = new Intent(this, BadgeGalleryActivity.class);
        startActivity(intent);
    }

    // Convert bytes to hex for logging
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
        // Clean up when paused
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset state on resume
        isProcessing = false;
        if (hasCameraPermission()) {
            resumeCameraScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        certHandler.cleanupJNI();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}