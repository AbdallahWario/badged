#include <libtasn1.h>
#include <jni.h>
#include <string.h>
#include <android/log.h>

//  libqaeda headers
#include "lq/cert.h"
#include "lq/mem.h"
#include "lq/wire.h"
#include "lq/err.h"
#include "lq/store.h"
#include "debug.h"

#define LOG_TAG "CertJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


JNIEXPORT jlong JNICALL
Java_org_defalsified_android_badged_services_Cert_deserializeCertificate(
        JNIEnv *env, jobject thiz, jbyteArray data) {

    // Get byte array length and elements
    jsize length = (*env)->GetArrayLength(env, data);
    jbyte *bytes = (*env)->GetByteArrayElements(env, data, NULL);

    if (bytes == NULL) {
        LOGE("Failed to get byte array elements");
        return 0;
    }

    // Deserialize the certificate
    LQCert *cert = NULL;
    int result = lq_certificate_deserialize(&cert, NULL, (char *)bytes, length);

    (*env)->ReleaseByteArrayElements(env, data, bytes, JNI_ABORT);

    if (result != ERR_OK || cert == NULL) {
        LOGE("Failed to deserialize certificate: %d", result);
        return 0;
    }

    // Return the certificate handle as a long
    return (jlong)(intptr_t)cert;
}


JNIEXPORT jint JNICALL
Java_org_defalsified_android_badged_services_Cert_verifyCertificate(
        JNIEnv *env, jobject thiz, jlong certHandle) {

    LQCert *cert = (LQCert *)(intptr_t)certHandle;

    if (cert == NULL) {
        LOGE("Invalid certificate handle");
        return ERR_INIT;
    }

    // Verify the certificate
    int result = lq_certificate_verify(cert);

    LOGI("Certificate verification result: %d", result);
    return result;
}


JNIEXPORT void JNICALL
Java_org_defalsified_android_badged_services_Cert_freeCertificate(
        JNIEnv *env, jobject thiz, jlong certHandle) {

    LQCert *cert = (LQCert *)(intptr_t)certHandle;

    if (cert != NULL) {
        lq_certificate_free(cert);
        LOGI("Certificate freed");
    } else {
        LOGE("Attempted to free null certificate");
    }
}