package org.defalsified.android.badged.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.defalsified.android.badged.dto.BadgeDTO;
import org.defalsified.android.badged.models.Badge;
import org.json.JSONException;
import org.json.JSONObject;

public class BadgeService {
    private static final String TAG = "BadgeService";
    private final Context context;
    private final BadgeRepository badgeRepository;
    private final CertificateService certificateService;

    public BadgeService(Context context) {
        this.context = context;
        this.badgeRepository = new BadgeRepository(context);
        this.certificateService = new CertificateService();
    }

    /**
     * Mint a new badge from QR data
     */
    public void mintBadge(JSONObject qrData, BadgeCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Create BadgeDTO from QR data
                BadgeDTO badgeDto = new BadgeDTO(qrData);

                // Get certificate data
                String certData = badgeDto.getCertificateData();

                // Verify certificate using new method
                boolean isValid = certificateService.verifyCertificate(certData);

                if (!isValid) {
                    callback.onError("Certificate verification failed");
                    return;
                }

                // Check if badge already exists
                Badge existingBadge = badgeRepository.getBadgeById(badgeDto.getSerial());
                if (existingBadge != null) {
                    callback.onSuccess(existingBadge, false);
                    return;
                }

                // Extract certificate data if needed
                JSONObject certJson = certificateService.extractCertificateData(certData);
                if (certJson == null) {
                    callback.onError("Failed to extract certificate data");
                    return;
                }

                // Create new badge using constructor
                Badge badge = new Badge(
                        badgeDto.getSerial(),
                        badgeDto.getOffer() + " Badge",
                        badgeDto.getHolder(),
                        badgeDto.getProject(),
                        badgeDto.getCertificateData()
                );

                // Save badge to repository
                boolean saved = badgeRepository.saveBadge(badge);

                if (saved) {
                    callback.onSuccess(badge, true);
                } else {
                    callback.onError("Failed to save badge");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error processing badge", e);
                callback.onError("Processing error: " + e.getMessage());
            }
        }, 500);
    }

    /**
     * Callback interface for badge operations
     */
    public interface BadgeCallback {
        void onSuccess(Badge badge, boolean isNewBadge);
        void onError(String errorMessage);
    }
}