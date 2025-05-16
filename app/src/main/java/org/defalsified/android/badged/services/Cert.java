package org.defalsified.android.badged.services;

import android.util.Log;

/**
 * Certificate handler for verifying digital vouchers using libqaeda
 *
 * This class provides a secure interface to native certificate verification
 * functionality. It ensures certificates are properly verified against
 * trusted public keys before accepting their contents.
 */
public class Cert {
    private static final String TAG = "Cert";

    // Load libqaeda library
    static {
        try {
            System.loadLibrary("qaeda");
            Log.d(TAG, "Successfully loaded libqaeda");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load libqaeda: " + e.getMessage());
        }
    }

    /**
     * Deserialize certificate and extract JSON content
     *
     * @param serializedData The serialized certificate bytes
     * @return JSON string extracted from the certificate, or null if failed
     */
    public native String deserialize(byte[] serializedData);

    /**
     * Verify the currently loaded certificate
     *
     * This checks both the certificate's cryptographic integrity and
     * validates that it was signed by a trusted key.
     *
     * @return true if verification succeeds and the certificate is trusted, false otherwise
     */
    public native boolean verify();

    /**
     * Free certificate resources
     * Should be called when done with the certificate to prevent memory leaks
     */
    public native void destroy();

    /**
     * Clean up any global JNI resources
     * Should be called when the application is shutting down
     */
    public native void cleanupJNI();

    /**
     * Helper method to deserialize and verify a certificate
     *
     * @param serializedData The serialized certificate bytes
     * @return JSON string extracted from the certificate if valid, null otherwise
     */
    public String deserializeAndVerify(byte[] serializedData) {
        String jsonContent = deserialize(serializedData);
        if (jsonContent != null && verify()) {
            return jsonContent;
        }
        // Clean up if verification failed
        destroy();
        return null;
    }

    /**
     * Process a serialized certificate completely
     * This method deserializes, verifies, and then cleans up the certificate
     *
     * @param serializedData The serialized certificate bytes
     * @return CertificateResult containing the status and JSON if successful
     */
    public CertificateResult processAndCleanup(byte[] serializedData) {
        CertificateResult result = new CertificateResult();

        try {
            // Deserialize
            String jsonContent = deserialize(serializedData);
            if (jsonContent == null) {
                result.success = false;
                result.error = "Failed to deserialize certificate";
                return result;
            }

            // Verify
            boolean isValid = verify();
            if (!isValid) {
                result.success = false;
                result.error = "Certificate verification failed: Not trusted or invalid signature";
                destroy(); // Clean up on failure
                return result;
            }

            // Success
            result.success = true;
            result.jsonContent = jsonContent;
        } catch (Exception e) {
            Log.e(TAG, "Error processing certificate: " + e.getMessage(), e);
            result.success = false;
            result.error = "Exception during certificate processing: " + e.getMessage();
        } finally {
            // Clean up resources
            destroy();
        }

        return result;
    }

    /**
     * Result wrapper for certificate processing
     */
    public static class CertificateResult {
        public boolean success;
        public String jsonContent;
        public String error;
    }
}