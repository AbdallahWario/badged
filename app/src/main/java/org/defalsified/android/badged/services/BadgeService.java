package org.defalsified.android.badged.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.defalsified.android.badged.models.Badge;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service for badge operations
 * TO be replaced with the C implementation
 */
public class BadgeService {
    private final Context context;
    private final BadgeRepository badgeRepository;

    public BadgeService(Context context) {
        this.context = context;
        this.badgeRepository = new BadgeRepository(context);
    }

    /**
     * Mint a new badge from QR data
     *
     * @param qrData QR code data
     * @param walletAddress User's wallet address
     * @param callback Callback for mint operation
     */
    public void mintBadge(JSONObject qrData, String walletAddress, BadgeCallback callback) {
        // This is a placeholder implementation

        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Extract badge info from QR code
                String badgeId = qrData.getString("badge_id");
                String merchantId = qrData.getString("merchant_id");

                // Check if badge already exists
                Badge existingBadge = badgeRepository.getBadgeById(badgeId);
                if (existingBadge != null) {
                    // Badge already exists
                    callback.onSuccess(existingBadge);
                    return;
                }

                // Create a badge (to be be minted and stored off chain)
                Badge badge = new Badge();
                badge.setId(badgeId);
                badge.setName("Sample Badge #" + (badgeId.length() >= 4 ? badgeId.substring(0, 4) : badgeId));
                badge.setDescription("A badge from merchant " + merchantId);
                badge.setImageUrl("https://example.com/badge/" + badgeId + ".png");
                badge.setTimestamp(System.currentTimeMillis());

                // Save badge to repository
                boolean saved = badgeRepository.saveBadge(badge);

                if (saved) {
                    // Return success
                    callback.onSuccess(badge);
                } else {
                    callback.onError("Failed to save badge");
                }

            } catch (JSONException e) {
                callback.onError("Invalid badge data: " + e.getMessage());
            }
        }, 2000); // Simulate 2 second delay
    }

    /**
     * Callback interface for badge operations
     */
    public interface BadgeCallback {
        void onSuccess(Badge badge);
        void onError(String errorMessage);
    }
}