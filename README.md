## App for Digital Vouchers Verification

Using cryptographic trust verification, this Android app offers a safe method for scanning, confirming, and using digital certificates (vouchers in this prototype). The app has the following features:

### Essential Features

- Interface for scanning QR codes with built-in camera
- Deserialization of certificates and cryptographic validation
- Assessment of trust level (0–100%)
- System for redeeming vouchers

### Features of Security

- Processing of ASN.1 encoded certificates
- Verification of digital signatures
- Evaluation of public key trust (using `lq_trust_check`)
- Chain-of-trust verification

### User Flow

1. Scan the QR code that has the base64-encoded certificate on it.
2. Verify cryptographic signatures automatically
3. Verify the issuer's trust level (a configurable threshold)
4. Use legitimate coupons to redeem vouchers.
5. View the vouchers gallery to view those that have already been redeemed

### Technical Points of Interest

- For crypto operations, JNI bridge to the native C library (`libqaeda`)
- Using CameraX to scan QR codes
- JSON processing and Base64
- Trust assessment with a precision of 0.0001% (parts-per-million)

With visual feedback regarding verification status and trust levels, the trust system guarantees that only certificates from authorized issuers can be redeemed.
