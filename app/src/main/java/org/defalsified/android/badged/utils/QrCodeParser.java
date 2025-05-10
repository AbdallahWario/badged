package org.defalsified.android.badged.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import org.defalsified.android.badged.models.Badge;

public class QrCodeParser {
    private static final String TAG = "QrCodeParser";

    // Required fields for badge QR code
    private static final String FIELD_SERIAL = "serial";
    private static final String FIELD_OFFER = "offer";
    private static final String FIELD_HOLDER = "holder";
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_CERT = "cert";

    /**
     * Parse QR code string to JSONObject
     */
    public static JSONObject parse(String qrContent) throws JSONException {
        return new JSONObject(qrContent);
    }

    /**
     * Validate if JSON is a valid badge QR code
     */
    public static boolean isBadgeQrCode(JSONObject qrData) {
        try {
            // Check for all required fields
            return qrData.has(FIELD_SERIAL) &&
                    qrData.has(FIELD_OFFER) &&
                    qrData.has(FIELD_HOLDER) &&
                    qrData.has(FIELD_PROJECT) &&
                    qrData.has(FIELD_CERT);
        } catch (Exception e) {
            Log.e(TAG, "QR code validation error", e);
            return false;
        }
    }

    /**
     * Create Badge from QR code data
     */
    public static Badge createBadgeFromQrData(JSONObject qrData) {
        try {
            // Validate QR code first
            if (!isBadgeQrCode(qrData)) {
                Log.e(TAG, "Invalid QR code data");
                return null;
            }

            // Create Badge with QR code fields
            return new Badge(
                    qrData.getString(FIELD_SERIAL),
                    qrData.getString(FIELD_OFFER),
                    qrData.getString(FIELD_HOLDER),
                    qrData.getString(FIELD_PROJECT),
                    qrData.getString(FIELD_CERT)
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error creating badge from QR data", e);
            return null;
        }
    }
}