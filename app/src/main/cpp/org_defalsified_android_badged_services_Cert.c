#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <android/log.h>
#include <libtasn1.h>

// Forward declarations for structures
typedef struct lq_msg_t LQMsg;
typedef struct LQSig LQSig;
typedef struct lq_privatekey_t LQPrivKey;
typedef struct lq_publickey_t LQPubKey;

// Constants
#ifndef LQ_DIGEST_LEN
#define LQ_DIGEST_LEN 64
#endif

#ifndef LQ_CERT_DOMAIN_LEN
#define LQ_CERT_DOMAIN_LEN 8
#endif

// Trust mode constants
enum trust_mode_e {
    TRUST_MATCH_NONE, // Ignore flags
    TRUST_MATCH_ONE,  // Success on first matched flag
    TRUST_MATCH_BEST, // Match as many flags as possible
    TRUST_MATCH_ALL,  // Strictly match all flags
};

// Content types
enum payload_e {
    LQ_CONTENT_RAW,         // Arbitrary data
    LQ_CONTENT_MSG,         // Data is a message type
    LQ_CONTENT_CERT,        // Data is a cert type
    LQ_CONTENT_KEY,         // Data is a private key type
    LQ_CONTENT_KEY_PUBLIC,  // Data is a public key type
};

// Store structure definition
typedef struct lq_store_t LQStore;
struct lq_store_t {
    int store_typ;
    void *userdata;
    int (*get)(enum payload_e typ, LQStore *store, const char *key, size_t key_len, char *value, size_t *value_len);
    int (*put)(enum payload_e typ, LQStore *store, const char *key, size_t *key_len, char *value, size_t value_len);
    int (*count)(enum payload_e typ, LQStore *store, const char *key, size_t key_len);
    void (*free)(LQStore *store);
};

// Resolve structure definition
typedef struct lq_resolve_t LQResolve;
struct lq_resolve_t {
    LQStore *store;
    LQResolve *next;
};

// Error codes
#define ERR_OK 0

// Certificate structure
typedef struct lq_certificate_t LQCert;
struct lq_certificate_t {
    char domain[LQ_CERT_DOMAIN_LEN];
    LQMsg *request;
    LQSig *request_sig;
    LQMsg *response;
    LQSig *response_sig;
    LQCert *parent;
    char parent_hash[LQ_DIGEST_LEN];
};

// Message structure
struct lq_msg_t {
    char state;
    char *data;
    size_t len;
    struct timespec time;
    LQPubKey *pubkey;
};

// External ASN.1 node
extern asn1_node asn;

// Function declarations
extern int lq_init(void);
extern int lq_certificate_deserialize(LQCert **cert, LQResolve *resolve, char *in, size_t in_len);
extern int lq_certificate_verify(LQCert *cert, LQPubKey **request_pubkey, LQPubKey **response_pubkey);
extern void lq_certificate_free(LQCert *cert);
extern int lq_trust_check(LQPubKey *pubkey, LQStore *store, enum trust_mode_e mode, const unsigned char *flags);
extern LQStore* lq_store_new(const char *spec);
extern void lq_store_free(LQStore *store);
extern int lq_publickey_bytes(LQPubKey *pk, char **out);

// Logging
#define LOG_TAG "CertJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Trust check result values
#define TRUST_RESULT_NOT_FOUND -1
#define TRUST_RESULT_MATCH 1000000

// State variables
static LQCert *currentCert = NULL;
static int isInitialized = 0;
static LQPubKey *requestPubKey = NULL;
static LQStore *trustStore = NULL;

// Hard-coded trusted public key
static const unsigned char TRUSTED_PUBLIC_KEY_1[] = {
        0x70, 0xB7, 0x36, 0x7F, 0xD7, 0xC4, 0x86, 0x79,
        0x6E, 0xA4, 0x45, 0x7B, 0xC0, 0x6C, 0x2F, 0xEB,
        0xDD, 0xC5, 0x94, 0x7B, 0xDE, 0x77, 0xC6, 0xCE,
        0x0D, 0x81, 0x74, 0x42, 0xB8, 0xFB, 0x96, 0x7E
};

typedef struct {
    const unsigned char *key;
    size_t length;
} TrustedKey;

static const TrustedKey TRUSTED_KEYS[] = {
        { TRUSTED_PUBLIC_KEY_1, 32 },
        // Add more trusted keys as needed
};

static const int NUM_TRUSTED_KEYS = sizeof(TRUSTED_KEYS) / sizeof(TrustedKey);

// Our custom in-memory store for trusted keys with  verification
static int store_get(enum payload_e typ, LQStore *store, const char *key, size_t key_len, char *value, size_t *value_len) {
    LOGD("store_get called with key_len=%zu", key_len);

    // Only handle public key requests
    if (typ != LQ_CONTENT_KEY_PUBLIC || key == NULL || key_len == 0) {
        LOGD("Invalid parameters or not a public key request");
        return -1;
    }

    // Check if the key is in our trusted list
    for (int i = 0; i < NUM_TRUSTED_KEYS; i++) {
        // Verify key length matches
        if (key_len == TRUSTED_KEYS[i].length) {
            // Compare key bytes
            if (memcmp(key, TRUSTED_KEYS[i].key, key_len) == 0) {
                // Key found in trusted list
                if (value != NULL && value_len != NULL && *value_len >= 2) {
                    // Set empty flags (all zeros) - we don't use flags in TRUST_MATCH_NONE mode
                    memset(value, 0, 2);
                    *value_len = 2;
                    LOGD("Found trusted key at index %d", i);
                    return ERR_OK;
                }
            }
        }
    }

    LOGD("Key not found in trusted list");
    return -1;  // Key not found
}

static int store_put(enum payload_e typ, LQStore *store, const char *key, size_t *key_len, char *value, size_t value_len) {
    // Read-only store
    return -1;
}

static int store_count(enum payload_e typ, LQStore *store, const char *key, size_t key_len) {
    // For any public key that's in our trusted list, return 1
    if (typ == LQ_CONTENT_KEY_PUBLIC && key != NULL && key_len > 0) {
        for (int i = 0; i < NUM_TRUSTED_KEYS; i++) {
            if (key_len == TRUSTED_KEYS[i].length &&
                memcmp(key, TRUSTED_KEYS[i].key, key_len) == 0) {
                return 1;
            }
        }
    }

    return 0;
}

static void store_free(LQStore *store) {
    // Nothing to free
}

// Initialize library and trust store
static void ensureInitialized() {
    if (!isInitialized) {
        // Initialize libqaeda
        lq_init();

        // Create our custom in-memory trust store
        if (trustStore == NULL) {
            // Allocate memory for the store
            trustStore = (LQStore*)malloc(sizeof(LQStore));
            if (trustStore != NULL) {
                // Initialize the store with our functions
                trustStore->store_typ = 1;  // Custom store type
                trustStore->userdata = NULL;
                trustStore->get = store_get;
                trustStore->put = store_put;
                trustStore->count = store_count;
                trustStore->free = store_free;

                LOGD("Custom trust store initialized");
            } else {
                LOGE("Failed to allocate memory for trust store");
            }
        }

        isInitialized = 1;
        LOGD("Initialized libqaeda");
    }
}

// Deserialize certificate and extract JSON data
JNIEXPORT jstring JNICALL
Java_org_defalsified_android_badged_services_Cert_deserialize(
        JNIEnv *env, jobject thiz, jbyteArray serializedData) {
    // Ensure library is initialized
    ensureInitialized();

    if (!serializedData) {
        LOGE("Serialized data is null");
        return NULL;
    }

    // Free any previously stored certificate
    if (currentCert != NULL) {
        lq_certificate_free(currentCert);
        currentCert = NULL;
    }

    // Reset request public key pointer
    requestPubKey = NULL;

    // Get byte array from Java
    jsize length = (*env)->GetArrayLength(env, serializedData);
    jbyte *bytes = (*env)->GetByteArrayElements(env, serializedData, NULL);
    if (!bytes) {
        LOGE("Failed to get byte array elements");
        return NULL;
    }

    // Deserialize the certificate
    int result = lq_certificate_deserialize(&currentCert, NULL, (char *)bytes, length);

    // Release the Java byte array
    (*env)->ReleaseByteArrayElements(env, serializedData, bytes, JNI_ABORT);

    if (result != ERR_OK) {
        LOGE("Failed to deserialize certificate: %d", result);
        return NULL;
    }

    if (!currentCert) {
        LOGE("Deserialization succeeded but returned NULL certificate");
        return NULL;
    }

    LOGD("Certificate domain: %.*s", LQ_CERT_DOMAIN_LEN, currentCert->domain);

    // Extract JSON from request
    jstring jsonString = NULL;
    if (currentCert->request && currentCert->request->data && currentCert->request->len > 0) {
        // Create Java string from request data - ensure null termination
        char *json_copy = malloc(currentCert->request->len + 1);
        if (json_copy) {
            memcpy(json_copy, currentCert->request->data, currentCert->request->len);
            json_copy[currentCert->request->len] = '\0';

            jsonString = (*env)->NewStringUTF(env, json_copy);
            free(json_copy);

            LOGD("Extracted JSON from certificate (length: %zu)", currentCert->request->len);
        } else {
            LOGE("Failed to allocate memory for JSON string");
        }
    } else {
        LOGE("Certificate request message is null or empty");
    }

    return jsonString;
}

// Verify certificate and extract public key
JNIEXPORT jboolean JNICALL
Java_org_defalsified_android_badged_services_Cert_verify(
        JNIEnv *env, jobject thiz) {
    if (currentCert == NULL) {
        LOGE("No certificate available to verify");
        return JNI_FALSE;
    }

    // Declare local variables for pubkeys
    LQPubKey *reqKey = NULL, *respKey = NULL;

    LOGD("Starting verification for domain: %.*s",
         LQ_CERT_DOMAIN_LEN, currentCert->domain);

    // Verify and get keys
    int result = lq_certificate_verify(currentCert, &reqKey, &respKey);

    if (result != ERR_OK) {
        LOGE("Verification failed: %d", result);
        return JNI_FALSE;
    }

    // Check if we got the request public key
    if (reqKey == NULL) {
        // Fallback: Try to get key from message
        if (currentCert->request && currentCert->request->pubkey) {
            reqKey = (LQPubKey*)currentCert->request->pubkey;
            LOGD("Using fallback pubkey from message");
        } else {
            LOGE("No public key extracted");
            return JNI_FALSE;
        }
    }

    // Store the key for trust checks
    requestPubKey = reqKey;

    // Now check if this key is trusted
    if (trustStore == NULL) {
        LOGE("Trust store not initialized");
        return JNI_FALSE;
    }

    char *keydata;
    size_t keylen = lq_publickey_bytes(requestPubKey, &keydata);

    if (keydata == NULL || keylen == 0) {
        LOGE("Failed to extract public key bytes");
        return JNI_FALSE;
    }

    // Log key bytes for debugging
    if (keylen <= 64) {  // Avoids logging very large keys
        char hexbuf[128] = {0};
        for (int i = 0; i < keylen && i < 32; i++) {
            sprintf(hexbuf + (i*3), "%02X ", (unsigned char)keydata[i]);
        }
        LOGD("Key bytes (first 32): %s", hexbuf);
    }

    // Create empty flags for TRUST_MATCH_NONE mode
    unsigned char flags[2] = {0x00, 0x00};

    // Check trust level using lq_trust_check
    int trust_level = lq_trust_check(requestPubKey, trustStore, TRUST_MATCH_NONE, flags);

    LOGD("Trust check result: %d", trust_level);

    // Only consider verification successful if the key is trusted
    if (trust_level == TRUST_RESULT_NOT_FOUND) {
        LOGE("Certificate verification failed: Key not trusted");
        return JNI_FALSE;
    }

    LOGD("Verification successful. Key trusted: %p", requestPubKey);
    return JNI_TRUE;
}

// Free certificate resources
JNIEXPORT void JNICALL
Java_org_defalsified_android_badged_services_Cert_destroy(
        JNIEnv *env, jobject thiz) {
    // Free the stored certificate
    if (currentCert != NULL) {
        lq_certificate_free(currentCert);
        currentCert = NULL;
    }

    // Reset request public key pointer
    requestPubKey = NULL;

    LOGD("Certificate resources freed");
}

// Clean up global resources
JNIEXPORT void JNICALL
Java_org_defalsified_android_badged_services_Cert_cleanupJNI(
        JNIEnv *env, jobject thiz) {
    // Free certificate
    if (currentCert != NULL) {
        lq_certificate_free(currentCert);
        currentCert = NULL;
    }

    // Free trust store
    if (trustStore != NULL) {
        free(trustStore);
        trustStore = NULL;
    }

    // Reset request public key pointer
    requestPubKey = NULL;

    isInitialized = 0;

    LOGD("JNI resources cleaned up");
}