package org.defalsified.android.badged.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for parsing and validating QR codes
 */
public class QrCodeParser {

    // Required fields in a badge QR code
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_MERCHANT_ID = "merchant_id";
    private static final String FIELD_BADGE_ID = "badge_id";
    private static final String TYPE_BADGE = "badge";

    /**
     * Parse a QR code string into a JSONObject
     *
     * @param qrContent String content from QR code
     * @return Parsed JSONObject
     * @throws JSONException if parsing fails
     */
    public static JSONObject parse(String qrContent) throws JSONException {
        return new JSONObject(qrContent);
    }

    /**
     * Check if a parsed QR code JSON is a valid badge QR code
     *
     * @param qrData Parsed JSON data from QR code
     * @return true if it's a valid badge QR code
     */
    public static boolean isBadgeQrCode(JSONObject qrData) {
        try {
            // Check if it has required fields
            if (!qrData.has(FIELD_TYPE) || !qrData.has(FIELD_MERCHANT_ID) || !qrData.has(FIELD_BADGE_ID)) {
                return false;
            }

            // Check if type is "badge"
            String type = qrData.getString(FIELD_TYPE);
            return TYPE_BADGE.equals(type);

        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Extract the merchant ID from a badge QR code
     *
     * @param qrData Parsed JSON data from QR code
     * @return Merchant ID string
     * @throws JSONException if field doesn't exist
     */
    public static String getMerchantId(JSONObject qrData) throws JSONException {
        return qrData.getString(FIELD_MERCHANT_ID);
    }

    /**
     * Extract the badge ID from a badge QR code
     *
     * @param qrData Parsed JSON data from QR code
     * @return Badge ID string
     * @throws JSONException if field doesn't exist
     */
    public static String getBadgeId(JSONObject qrData) throws JSONException {
        return qrData.getString(FIELD_BADGE_ID);
    }
}