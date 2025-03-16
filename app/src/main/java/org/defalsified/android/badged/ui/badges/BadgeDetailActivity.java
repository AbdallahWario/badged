package org.defalsified.android.badged.ui.badges;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.defalsified.android.badged.R;

public class BadgeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BADGE_ID = "badge_id";
    public static final String EXTRA_IS_NEW_BADGE = "is_new_badge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TO DO
        TextView textView = new TextView(this);
        textView.setText("Badge Detail Screen Coming Soon");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(20);

        setContentView(textView);
    }
}