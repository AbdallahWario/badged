package org.defalsified.android.badged.services;

public class Cert{

    static {
        System.loadLibrary("qaeda");
    }

    /**
     * Deserialize a certificate from a byte array.
     *
     * @param data The serialized certificate data
     * @return A handle to the deserialized certificate, or 0 if deserialization failed
     */
    public native long deserializeCertificate(byte[] data);

    /**
     * Verify a certificate's signatures.
     *
     * @param certHandle The handle to the certificate to verify
     * @return 0 on success, error code otherwise
     */
    public native int verifyCertificate(long certHandle);

    /**
     * Free a certificate to prevent memory leaks.
     *
     * @param certHandle The handle to the certificate to free
     */
    public native void freeCertificate(long certHandle);
}
