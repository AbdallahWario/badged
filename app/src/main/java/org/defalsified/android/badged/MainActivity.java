package org.defalsified.android.badged;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.defalsified.android.badged.services.LibQaeda;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate started");

        TextView textView = new TextView(this);
        textView.setText("Welcome to Badges!");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(24);

        try {
            Log.d(TAG, "Starting LibQaeda test");
            LibQaeda libQaeda = new LibQaeda();
            Log.d(TAG, "LibQaeda instance created");

            long storePtr = libQaeda.createDummyStore();
            Log.d(TAG, "Store created: " + storePtr);

            byte[] key = "test_key".getBytes();
            Log.d(TAG, "Key created, length: " + key.length);

            String result = libQaeda.dummyContentGet(1, storePtr, key);
            Log.d(TAG, "dummyContentGet returned: " + (result != null ? result : "null"));

            textView.append("\n\nLibQaeda Test:\n" + result);
        } catch (Exception e) {
            Log.e(TAG, "Error testing LibQaeda", e);
            e.printStackTrace();
            textView.append("\n\nLibQaeda Test Error:\n" + e.getMessage());
        }

        setContentView(textView);
        Log.d(TAG, "MainActivity onCreate completed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");
    }
}