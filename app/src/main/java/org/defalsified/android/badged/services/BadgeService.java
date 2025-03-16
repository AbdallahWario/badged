package org.defalsified.android.badged.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.defalsified.android.badged.models.Badge;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  a placeholder Service for badge operations
 */
public class BadgeService {
    private final Context context;

    public BadgeService(Context context) {
        this.context = context;
    }

    /**
     * Mint a new badge from QR data (nft for the proof of stay at the apartments/the cafe)
     *
     * @param qrData QR code data
     * @param walletAddress User's wallet address
     * @param callback Callback for mint operation
     */
    public void mintBadge(JSONObject qrData, String walletAddress, BadgeCallback callback) {
        // A placeholder implementation
        //  this will call the blockchain APIs

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Extract badge info from QR code
                String badgeId = qrData.getString("badge_id");
                String merchantId = qrData.getString("merchant_id");

                // Create a badge (the nft metadata to come from wala or the source we will use, not on chain prolly)
                Badge badge = new Badge();
                badge.setId(badgeId);
                badge.setName("Sample Badge #" + (badgeId.length() >= 4 ? badgeId.substring(0, 4) : badgeId));

                badge.setDescription("A badge from merchant " + merchantId);
                badge.setImageUrl("https://org.defalsified.badged" + badgeId + ".png");
                badge.setTimestamp(System.currentTimeMillis());

                // Return success
                callback.onSuccess(badge);

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