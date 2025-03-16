package org.defalsified.android.badged.ui.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.services.BadgeService;
import org.defalsified.android.badged.services.WalletManager;
import org.defalsified.android.badged.ui.badges.BadgeDetailActivity;
import org.defalsified.android.badged.utils.QrCodeParser;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for scanning QR codes to collect badges.
 * This handles camera permission, QR code scanning, and wallet creation if needed.
 */
public class QrScannerActivity extends AppCompatActivity {

    // UI Components
    private PreviewView previewView;
    private TextView statusTextView;
    private Button galleryButton;

    // Camera and analysis components
    private ExecutorService cameraExecutor;

    // Service components
    private WalletManager walletManager;
    private BadgeService badgeService;

    // Permission request code
    private static final int REQUEST_CAMERA_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        // Initialize services
        walletManager = new WalletManager(this);
        badgeService = new BadgeService(this);

        // Initialize UI
        initViews();

        // Set up camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check and request camera permission
        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Initialize view references and click listeners
     */
    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        statusTextView = findViewById(R.id.status_text);
        galleryButton = findViewById(R.id.gallery_button);

        // Set up gallery button click listener
        galleryButton.setOnClickListener(v -> openBadgeGallery());

        // Update status text
        updateStatusText();
    }

    /**
     * Update the status text based on wallet status
     */
    private void updateStatusText() {
        if (walletManager.hasWallet()) {
            statusTextView.setText(R.string.scanner_ready);
        } else {
            statusTextView.setText(R.string.scanner_no_wallet);
        }
    }

    /**
     * Check if we have camera permission
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION
        );
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start camera
                startCamera();
            } else {
                // Permission denied
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Set up and start the camera
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set up image analysis for QR code
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Create QR scanner and set it as the analyzer
                QrCodeAnalyzer qrCodeAnalyzer = new QrCodeAnalyzer(
                        qrContent -> runOnUiThread(() -> processQrContent(qrContent))
                );
                imageAnalysis.setAnalyzer(cameraExecutor, qrCodeAnalyzer);

                // Select back camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Unbind any existing use cases
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                // Handle errors
                Toast.makeText(this, "Error starting camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Process the content from a scanned QR code
     *
     * @param qrContent The string content of the QR code
     */
    private void processQrContent(String qrContent) {
        try {
            // Parse QR content
            JSONObject qrData = QrCodeParser.parse(qrContent);

            // Check if this is a valid badge QR code
            if (QrCodeParser.isBadgeQrCode(qrData)) {
                // Handle the badge QR code
                handleBadgeQrCode(qrData);
            } else {
                // Not a valid badge QR code
                Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            // Error parsing QR code
            Toast.makeText(this, R.string.error_parsing_qr, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle a badge QR code
     *
     * @param qrData The parsed JSON data from the QR code
     */
    private void handleBadgeQrCode(JSONObject qrData) {
        // Check if user has a wallet
        if (!walletManager.hasWallet()) {
            // Show dialog to create wallet
            showCreateWalletDialog(qrData);
        } else {
            // User has wallet, proceed to mint badge
            mintBadge(qrData);
        }
    }

    /**
     * Show dialog to confirm wallet creation
     *
     * @param qrData The QR data to process after wallet creation
     */
    private void showCreateWalletDialog(JSONObject qrData) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_wallet_title)
                .setMessage(R.string.create_wallet_message)

                .setPositiveButton(R.string.create_wallet_confirm, (dialog, which) -> {
                            // Create wallet
                            createWalletAndMintBadge(qrData);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
    }

    /**
     * Create a wallet and then mint the nft( we'll display the badge)
     *
     * @param qrData The QR data for the badge
     */
    private void createWalletAndMintBadge(JSONObject qrData) {
        // Show loading state
        showLoading(true);

        // Create wallet
        walletManager.createWallet(new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String walletAddress) {
                // Wallet created, update UI
                updateStatusText();

                // Now mint the badge
                mintBadge(qrData);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                showLoading(false);
                Toast.makeText(QrScannerActivity.this,
                        getString(R.string.wallet_creation_error, errorMessage),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Mint a new badge from QR data
     *
     * @param qrData The QR data for the badge
     */
    private void mintBadge(JSONObject qrData) {
        // Show loading state
        showLoading(true);

        // Get wallet address
        String walletAddress = walletManager.getWalletAddress();

        // Call service to mint badge
        badgeService.mintBadge(qrData, walletAddress, new BadgeService.BadgeCallback() {
            @Override
            public void onSuccess(Badge badge) {
                // Hide loading
                showLoading(false);

                // Show badge acquired
                showBadgeAcquired(badge);
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading
                showLoading(false);

                // Show error
                Toast.makeText(QrScannerActivity.this,
                        getString(R.string.badge_mint_error, errorMessage),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Show loading indicator
     *
     * @param isLoading True to show loading, false to hide
     */
    private void showLoading(boolean isLoading) {
        View loadingView = findViewById(R.id.loading_view);
        loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Show badge acquired success screen
     *
     * @param badge The acquired badge
     */
    private void showBadgeAcquired(Badge badge) {
        // Launch badge detail activity
        Intent intent = new Intent(this, BadgeDetailActivity.class);
        intent.putExtra(BadgeDetailActivity.EXTRA_BADGE_ID, badge.getId());
        intent.putExtra(BadgeDetailActivity.EXTRA_IS_NEW_BADGE, true);
        startActivity(intent);
    }

    /**
     * Open the badge gallery
     */
    private void openBadgeGallery() {
        // For now, just show a toast
        Toast.makeText(this, "Badge Gallery coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}