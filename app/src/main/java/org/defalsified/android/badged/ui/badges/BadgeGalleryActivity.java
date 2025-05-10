package org.defalsified.android.badged.ui.badges;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.services.BadgeRepository;
import org.defalsified.android.badged.ui.scanner.QrScannerActivity;

import java.util.List;

/**
 * Activity to display all collected badges
 */
public class BadgeGalleryActivity extends AppCompatActivity implements BadgeAdapter.OnBadgeClickListener {

    private View emptyState;
    private RecyclerView recyclerView;
    private BadgeRepository badgeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_gallery);

        // Initialize repository
        badgeRepository = new BadgeRepository(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        emptyState = findViewById(R.id.empty_state);
        recyclerView = findViewById(R.id.badge_recycler_view);
        Button scanButton = findViewById(R.id.scan_badge_button);
        TextView scanVoucherText = findViewById(R.id.scan_voucher_text);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set up scan button in empty state
        scanButton.setOnClickListener(v -> openScanner());

        // Set up scan voucher text in toolbar
        scanVoucherText.setOnClickListener(v -> openScanner());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge list each time the activity is shown
        loadBadges();
    }

    /**
     * Load badges from repository and update UI
     */
    private void loadBadges() {
        // Get all badges
        List<Badge> badges = badgeRepository.getAllBadges();

        if (badges.isEmpty()) {
            // Show empty state
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Show badges in RecyclerView
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Set up adapter
            BadgeAdapter adapter = new BadgeAdapter(badges, this);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Handle badge click
     */
    @Override
    public void onBadgeClick(Badge badge) {
        // Open badge detail screen
        Intent intent = new Intent(this, BadgeDetailActivity.class);
        intent.putExtra(BadgeDetailActivity.EXTRA_BADGE_SERIAL, badge.getSerial());
        startActivity(intent);
    }

    /**
     * Open QR scanner
     */
    private void openScanner() {
        Intent intent = new Intent(this, QrScannerActivity.class);
        startActivity(intent);
    }
}