package org.defalsified.android.badged.ui.badges;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.services.BadgeRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;

/**
 * Activity to display badge details
 */
public class BadgeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BADGE_ID = "badge_id";
    public static final String EXTRA_IS_NEW_BADGE = "is_new_badge";

    private BadgeRepository badgeRepository;
    private Badge badge;

    // UI Elements
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView badgeImage;
    private TextView badgeId;
    private TextView badgeDescription;
    private TextView acquisitionDate;
    private Button shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_detail);

        // Initialize repository
        badgeRepository = new BadgeRepository(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Initialize views

        initViews();

        // Get badge ID from intent

        String badgeId = getIntent().getStringExtra(EXTRA_BADGE_ID);
        boolean isNewBadge = getIntent().getBooleanExtra(EXTRA_IS_NEW_BADGE, false);

        if (badgeId != null) {
            // Load badge details
            loadBadgeDetails(badgeId);

            // Show toast if it's a new badge
            if (isNewBadge) {
                Toast.makeText(this, "New badge acquired!", Toast.LENGTH_LONG).show();
            }
        } else {
            // No badge ID provided, finish activity
            Toast.makeText(this, "Error: No badge information", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initialize UI elements
     */
    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        badgeImage = findViewById(R.id.badge_image);
        badgeId = findViewById(R.id.badge_id);
        badgeDescription = findViewById(R.id.badge_description);
        acquisitionDate = findViewById(R.id.acquisition_date);
        shareButton = findViewById(R.id.share_button);

        // Set up share button
        shareButton.setOnClickListener(v -> shareBadge());
    }

    /**
     * Load badge details from repository
     *
     * @param badgeId ID of the badge to load
     */
    private void loadBadgeDetails(String badgeId) {
        badge = badgeRepository.getBadgeById(badgeId);

        if (badge != null) {
            // Update UI with badge details
            updateUI();
        } else {
            // Badge not found
            Toast.makeText(this, "Error: Badge not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Update UI with badge details
     */
    private void updateUI() {
        // Set badge title
        collapsingToolbar.setTitle(badge.getName());

        // Set badge image
        badgeImage.setImageResource(R.drawable.badge_placeholder);

        // Set badge details
        badgeId.setText(badge.getId());
        badgeDescription.setText(badge.getDescription());
        acquisitionDate.setText(badge.getFormattedDate());
    }

    /**
     * Share badge information
     */
    private void shareBadge() {
        if (badge == null) return;

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my " + badge.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "I just earned the " + badge.getName() + " badge in BadgeStay!\n\n" +
                        badge.getDescription());

        // Start share chooser
        startActivity(Intent.createChooser(shareIntent, "Share Badge"));
    }

    /**
     * Handle back button in toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}