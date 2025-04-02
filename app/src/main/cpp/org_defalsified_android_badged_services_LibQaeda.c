#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "lq/store.h"
#include "lq/err.h"

extern struct lq_store_t LQDummyContent;

JNIEXPORT jlong JNICALL
Java_org_defalsified_android_badged_services_LibQaeda_createDummyStore(JNIEnv *env, jobject thiz) {
    return (jlong)&LQDummyContent;
}

JNIEXPORT jlong JNICALL
Java_org_defalsified_android_badged_services_LibQaeda_dummyContentGet(JNIEnv *env, jobject thiz,
                                                                      jint payloadType,
                                                                      jlong storePtr,
                                                                      jbyteArray key) {
    LQStore *store = (LQStore *)storePtr;

    jbyte *keyBytes = (*env)->GetByteArrayElements(env, key, NULL);
    jsize keyLen = (*env)->GetArrayLength(env, key);

    char value[10] = {0};
    size_t valueLen = sizeof(value);

    int result = lq_dummy_content_get((enum payload_e)payloadType, store,
                                      (const char *)keyBytes, (size_t)keyLen,
                                      value, &valueLen);

    (*env)->ReleaseByteArrayElements(env, key, keyBytes, JNI_ABORT);

    return (jlong)result;
}