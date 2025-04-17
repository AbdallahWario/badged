#include <jni.h>
#include <stdlib.h>
#include "lq/store.h"
#include "lq/err.h"
#include <hex.h>

//function declaration
extern int lq_dummy_content_get(enum payload_e typ, LQStore *store, const char *key, size_t key_len, char *value, size_t *value_len);
extern struct lq_store_t LQDummyContent;

JNIEXPORT jlong JNICALL Java_org_defalsified_android_badged_services_LibQaeda_createDummyStore
        (JNIEnv *env, jobject thiz) {
    LQStore *store = (LQStore*)malloc(sizeof(LQStore));
    *store = LQDummyContent;
    return (jlong)store;
}

JNIEXPORT jstring JNICALL Java_org_defalsified_android_badged_services_LibQaeda_dummyContentGet
        (JNIEnv *env, jobject thiz, jint payloadType, jlong storePtr, jbyteArray key) {
    LQStore *store = (LQStore*)storePtr;
    jbyte *keyBytes = (*env)->GetByteArrayElements(env, key, NULL);
    jsize keyLength = (*env)->GetArrayLength(env, key);

    char value[4096] = {0};
    size_t valueLen = sizeof(value);

    int result = lq_dummy_content_get(
            (enum payload_e)payloadType,
            store,
            (const char*)keyBytes,
            (size_t)keyLength,
            value,
            &valueLen
    );

    (*env)->ReleaseByteArrayElements(env, key, keyBytes, JNI_ABORT);

    if (result != 0) {
        return NULL;
    }

    return (*env)->NewStringUTF(env, value);
}
