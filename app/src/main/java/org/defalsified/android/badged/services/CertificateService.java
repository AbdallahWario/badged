package org.defalsified.android.badged.services;

import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service class for certificate operations
 */
public class CertificateService {
    private static final String TAG = "CertificateService";
    private final Cert certHandler;

    public CertificateService() {
        this.certHandler = new Cert();
    }

    /**
     * Processes a certificate from base64 encoded data
     * Deserializes, verifies, and extracts JSON data
     *
     * @param base64CertData Base64 encoded certificate data
     * @return JSONObject with certificate data, or null if invalid
     */
    public JSONObject processCertificate(String base64CertData) {
        try {
            // Decode certificate data from base64
            byte[] certData = Base64.decode(base64CertData, Base64.DEFAULT);

            // Deserialize certificate to get JSON content
            String jsonContent = certHandler.deserialize(certData);

            if (jsonContent == null || jsonContent.isEmpty()) {
                Log.e(TAG, "Certificate deserialization failed");
                return null;
            }

            // Verify the certificate
            boolean isValid = certHandler.verify();

            if (!isValid) {
                Log.e(TAG, "Certificate verification failed");
                return null;
            }

            // Parse JSON data
            JSONObject certJson = new JSONObject(jsonContent);

            // Add original certificate data
            certJson.put("cert", base64CertData);

            return certJson;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing certificate JSON", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Certificate processing error", e);
            return null;
        } finally {
            // Always release certificate resources
            certHandler.destroy();
        }
    }

    /**
     * Verify a certificate from base64 encoded data without extracting JSON
     *
     * @param base64CertData Base64 encoded certificate data
     * @return true if certificate is valid, false otherwise
     */
    public boolean verifyCertificate(String base64CertData) {
        try {
            // Decode certificate data
            byte[] certData = Base64.decode(base64CertData, Base64.DEFAULT);

            // Deserialize certificate
            certHandler.deserialize(certData);

            // Verify certificate
            boolean isValid = certHandler.verify();

            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Certificate verification error", e);
            return false;
        } finally {
            // Always release certificate resources
            certHandler.destroy();
        }
    }

    /**
     * Extract certificate data without verification
     *
     * @param base64CertData Base64 encoded certificate data
     * @return JSONObject with certificate data, or null if invalid
     */
    public JSONObject extractCertificateData(String base64CertData) {
        try {
            // Decode certificate data
            byte[] certData = Base64.decode(base64CertData, Base64.DEFAULT);

            // Deserialize certificate to get JSON content
            String jsonContent = certHandler.deserialize(certData);

            if (jsonContent == null || jsonContent.isEmpty()) {
                Log.e(TAG, "Certificate deserialization failed");
                return null;
            }

            // Parse JSON data
            JSONObject certJson = new JSONObject(jsonContent);

            // Add original certificate data
            certJson.put("cert", base64CertData);

            return certJson;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing certificate JSON", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Certificate data extraction error", e);
            return null;
        } finally {
            // Always release certificate resources
            certHandler.destroy();
        }
    }
}