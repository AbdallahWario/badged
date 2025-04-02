#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include "lq/store.h"
#include "lq/err.h"

#define LOG_TAG "LibQaeda-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// External reference to LQDummyContent
extern struct lq_store_t LQDummyContent;

JNIEXPORT jlong JNICALL Java_org_defalsified_android_LibQaeda_createDummyStore
        (JNIEnv *env, jobject obj) {
    LOGI("Creating dummy store");
    return (jlong)&LQDummyContent;
}

JNIEXPORT jstring JNICALL Java_org_defalsified_android_LibQaeda_dummyContentGet
        (JNIEnv *env, jobject obj, jint payloadType, jlong storePtr, jbyteArray keyArray) {
    // Cast pointer to LQStore
    LQStore *store = (LQStore *)storePtr;

    // Get key data
    jsize keyLen = (*env)->GetArrayLength(env, keyArray);
    char *key = (char *)malloc(keyLen);
    if (key == NULL) {
        LOGE("Memory allocation failed");
        return NULL;
    }

    (*env)->GetByteArrayRegion(env, keyArray, 0, keyLen, (jbyte *)key);

    // Buffer for result
    char value[4096] = {0};
    size_t valueLen = sizeof(value);

    //get function
    LOGI("Calling get with type %d, key length %d", payloadType, keyLen);
    int result = lq_dummy_content_get((enum payload_e)payloadType, store, key, keyLen, value, &valueLen);
    LOGI("Get returned: %d", result);

    // Clean up
    free(key);

    // Return result
    if (result == 0) {
        return (*env)->NewStringUTF(env, "");
    } else {
        LOGE("Get failed: %d", result);
        return NULL;
    }
}