package org.defalsified.android.badged.services;

public class LibQaeda {
    static {
        System.loadLibrary("qaeda");
    }
    public native long createDummyStore();
    

    public native String dummyContentGet(int payloadType, long storePtr, byte[] key);
}