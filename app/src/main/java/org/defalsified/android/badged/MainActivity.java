package org.defalsified.android.badged;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate started");

        TextView textView = new TextView(this);
        textView.setText("Welcome to vouchers!");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(24);



        setContentView(textView);
        Log.d(TAG, "MainActivity onCreate completed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");
    }
}