package org.defalsified.android.badged.services;

import android.util.Log;

public class LibQaeda {
    private static final String TAG = "LibQaeda";

    static {
        try {
            Log.d(TAG, "Loading qaeda library");
            System.loadLibrary("qaeda");
            Log.d(TAG, "qaeda library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load qaeda library", e);
            throw e;
        }
    }


    public native long createDummyStore();

    public native String dummyContentGet(int payloadType, long storePtr, byte[] key);
}