#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <android/log.h>
#include <libtasn1.h>

// Forward declarations for structures
typedef struct lq_msg_t LQMsg;
typedef struct LQSig LQSig;
typedef struct LQResolve LQResolve;
typedef struct lq_privatekey_t LQPrivKey;
typedef struct lq_publickey_t LQPubKey;

// Constants
#ifndef LQ_DIGEST_LEN
#define LQ_DIGEST_LEN 64
#endif

#ifndef LQ_CERT_DOMAIN_LEN
#define LQ_CERT_DOMAIN_LEN 8
#endif

// Error codes from libqaeda
#define ERR_OK 0
#define ERR_INIT -1
#define ERR_READ -2
#define ERR_WRITE -3
#define ERR_ENCODING -4
#define ERR_OVERFLOW -5
#define ERR_NOOP -6
#define ERR_NONSENSE -7
#define ERR_DUP -8
#define ERR_SEQ -9
#define ERR_RESPONSE -10
#define ERR_REQUEST -11
#define ERR_FAIL -12

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
extern int lq_certificate_verify(LQCert *cert);
extern void lq_certificate_free(LQCert *cert);
extern int lq_msg_verify_extra(LQMsg *msg, LQSig *sig, const char *salt, const char *extra, size_t extra_len);

// Logging
#define LOG_TAG "CertJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// State variables
static LQCert *currentCert = NULL;
static int isInitialized = 0;

// Initialize library
static void ensureInitialized() {
    if (!isInitialized) {
        // Check if lq_init is available
        if (lq_init != NULL) {
            int result = lq_init();
            LOGD("Initialized libqaeda: %d", result);
        } else {
            LOGE("lq_init function not available");
        }
        isInitialized = 1;
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

    // Get byte array from Java
    jsize length = (*env)->GetArrayLength(env, serializedData);
    jbyte *bytes = (*env)->GetByteArrayElements(env, serializedData, NULL);
    if (!bytes) {
        LOGE("Failed to get byte array elements");
        return NULL;
    }

    // Log first few bytes for debugging
    if (length > 16) {
        LOGD("First 16 bytes: %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X",
             bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
             bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
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

// Verify certificate
JNIEXPORT jboolean JNICALL
Java_org_defalsified_android_badged_services_Cert_verify(
        JNIEnv *env, jobject thiz) {
    if (currentCert == NULL) {
        LOGE("No certificate available to verify");
        return JNI_FALSE;
    }

    // Verify the certificate
    int result = lq_certificate_verify(currentCert);
    if (result != ERR_OK) {
        LOGE("Certificate verification failed: %d", result);
        return JNI_FALSE;
    }

    LOGD("Certificate verification successful");
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
        LOGD("Certificate freed");
    }
}

// Get the certificate domain
JNIEXPORT jstring JNICALL
Java_org_defalsified_android_badged_services_Cert_getDomain(
        JNIEnv *env, jobject thiz) {
    if (currentCert == NULL) {
        LOGE("No certificate available to get domain");
        return NULL;
    }

    // Create a Java string from the domain - ensure null termination
    char domain_copy[LQ_CERT_DOMAIN_LEN + 1];
    memcpy(domain_copy, currentCert->domain, LQ_CERT_DOMAIN_LEN);
    domain_copy[LQ_CERT_DOMAIN_LEN] = '\0';

    jstring domainString = (*env)->NewStringUTF(env, domain_copy);
    return domainString;
}

// Check if certificate has a response
JNIEXPORT jboolean JNICALL
Java_org_defalsified_android_badged_services_Cert_hasResponse(
        JNIEnv *env, jobject thiz) {
    if (currentCert == NULL) {
        LOGE("No certificate available to check response");
        return JNI_FALSE;
    }

    return (currentCert->response != NULL) ? JNI_TRUE : JNI_FALSE;
}

// Get response JSON if available
JNIEXPORT jstring JNICALL
Java_org_defalsified_android_badged_services_Cert_getResponse(
        JNIEnv *env, jobject thiz) {
    if (currentCert == NULL || currentCert->response == NULL ||
        currentCert->response->data == NULL || currentCert->response->len == 0) {
        LOGE("No response available in certificate");
        return NULL;
    }

    // Create a Java string from the response data - ensure null termination
    char *response_copy = malloc(currentCert->response->len + 1);
    if (response_copy) {
        memcpy(response_copy, currentCert->response->data, currentCert->response->len);
        response_copy[currentCert->response->len] = '\0';

        jstring responseString = (*env)->NewStringUTF(env, response_copy);
        free(response_copy);

        return responseString;
    } else {
        LOGE("Failed to allocate memory for response string");
        return NULL;
    }
}