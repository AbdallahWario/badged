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

        TextView textView = new TextView(this);
        textView.setText("Welcome to Badges!");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(24);

        // Testing the LibQaeda get functionality
        try {
            LibQaeda libQaeda = new LibQaeda();
            long storePtr = libQaeda.createDummyStore();
            byte[] key = "test_key".getBytes();
            String result = libQaeda.dummyContentGet(1, storePtr, key);
            
            Log.d(TAG, "LibQaeda test result: " + result);
            
            // Update the text view to show the result
            textView.append("\n\nLibQaeda Test:\n" + result);
        } catch (Exception e) {
            Log.e(TAG, "Error testing LibQaeda", e);
            textView.append("\n\nLibQaeda Test Error:\n" + e.getMessage());
        }

        setContentView(textView);
    }
}