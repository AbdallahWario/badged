package org.defalsified.android.badged;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("Welcome to Badges!");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(24);

        setContentView(textView);
    }
}