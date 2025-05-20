package org.defalsified.android.badged.ui.badges;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.services.BadgeRepository;

/**
 * Shows badge details (verification done during QR scanning)
 */
public class BadgeDetailActivity extends AppCompatActivity {
    private static final String TAG = "BadgeDetailActivity";

    public static final String EXTRA_BADGE_SERIAL = "badge_serial";
    public static final String EXTRA_IS_NEW_BADGE = "is_new_badge";
    public static final String EXTRA_VERIFICATION_STATUS = "verification_status";

    private BadgeRepository badgeRepository;
    private TextView verificationStatusText;
    private View newBadgeIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup collapsing toolbar
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);

        // Init services
        badgeRepository = new BadgeRepository(this);

        // Init views
        verificationStatusText = findViewById(R.id.verification_status);
        newBadgeIndicator = findViewById(R.id.new_badge_indicator);

        // Get badge data from intent
        String badgeSerial = getIntent().getStringExtra(EXTRA_BADGE_SERIAL);
        boolean isNewBadge = getIntent().getBooleanExtra(EXTRA_IS_NEW_BADGE, false);
        String verificationStatus = getIntent().getStringExtra(EXTRA_VERIFICATION_STATUS);

        if (badgeSerial == null) {
            finish();
            return;
        }

        // Get badge
        Badge badge = badgeRepository.getBadgeById(badgeSerial);
        if (badge == null) {
            finish();
            return;
        }

        // Populate views
        ImageView badgeImage = findViewById(R.id.badge_image);
        TextView badgeIdText = findViewById(R.id.badge_id);
        TextView descriptionText = findViewById(R.id.badge_description);
        TextView acquisitionDateText = findViewById(R.id.acquisition_date);
        Button shareButton = findViewById(R.id.share_button);
        Button verifyButton = findViewById(R.id.verify_button);

        // Badge name in toolbar
        collapsingToolbar.setTitle(badge.getDisplayName());

        // Badge details
        badgeIdText.setText(badge.getSerial());
        descriptionText.setText(badge.getDisplayDescription());
        acquisitionDateText.setText(badge.getFormattedDate());

        // Placeholder image
        badgeImage.setImageResource(R.drawable.badge_placeholder);

        // Share button
        shareButton.setOnClickListener(v -> shareBadge(badge));

        // Show verification status if available
        if (verificationStatus != null && !verificationStatus.isEmpty()) {
            showVerificationStatus(verificationStatus);
        } else {
            verificationStatusText.setVisibility(View.GONE);
        }

        // Hide verify button (verification done during scanning)
        verifyButton.setVisibility(View.GONE);

        // Show new badge indicator
        if (isNewBadge) {
            newBadgeIndicator.setVisibility(View.VISIBLE);
        } else {
            newBadgeIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Display verification status
     */
    private void showVerificationStatus(String status) {
        verificationStatusText.setVisibility(View.VISIBLE);

        switch (status.toUpperCase()) {
            case "VERIFIED":
            case "VALID":
                verificationStatusText.setText("✓ Verified");
                verificationStatusText.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "INVALID":
            case "FAILED":
                verificationStatusText.setText("✗ Invalid");
                verificationStatusText.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            default:
                verificationStatusText.setText(status);
                verificationStatusText.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
    }

    /**
     * Share badge info
     */
    private void shareBadge(Badge badge) {
        // Create sharing intent
        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Share text
        String shareText = String.format("voucher redeemed: %s\nSerial: %s\nDate: %s",
                badge.getDisplayName(),
                badge.getSerial(),
                badge.getFormattedDate());

        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

        // Start sharing
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Badge"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}