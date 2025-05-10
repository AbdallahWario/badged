package org.defalsified.android.badged.services;

/**
 * Certificate handler for verifying digital vouchers using libqaeda
 */
public class Cert {

    // Load native library
    static {
        System.loadLibrary("qaeda");
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
     * @return true if verification succeeds, false otherwise
     */
    public native boolean verify();

    /**
     * Free certificate resources
     * Should be called when done with the certificate to prevent memory leaks
     */
    public native void destroy();

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
            result.error = "Certificate verification failed";
            destroy(); // Clean up on failure
            return result;
        }

        // Success
        result.success = true;
        result.jsonContent = jsonContent;

        // Clean up resources
        destroy();

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