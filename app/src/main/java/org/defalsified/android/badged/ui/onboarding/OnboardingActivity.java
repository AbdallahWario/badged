package org.defalsified.android.badged.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import org.defalsified.android.badged.MainActivity;
import org.defalsified.android.badged.R;
import org.defalsified.android.badged.utils.PrefsManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.defalsified.android.badged.ui.scanner.QrScannerActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * first-time users are taken through what the app aims to do(collecting digital proofs on qrs).
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button nextButton;
    private Button skipButton;

    // Data
    private PrefsManager prefsManager;
    private List<OnboardingPage> pages;
    private static final String PREF_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        prefsManager = new PrefsManager(this);

        // Check if completed onboarding
        if (prefsManager.getBoolean(PREF_ONBOARDING_COMPLETED, false)) {
            navigateToMainScreen();
            return;
        }

        // Initialize views
        initViews();

        // Set up onboarding pages
        setupPages();

        // Set up ViewPager with adapter
        setupViewPager();
    }


    private void initViews() {
        viewPager = findViewById(R.id.onboarding_view_pager);
        TabLayout tabLayout = findViewById(R.id.onboarding_tab_layout);
        nextButton = findViewById(R.id.onboarding_next_button);
        skipButton = findViewById(R.id.onboarding_skip_button);

        // Setup button click listeners
        nextButton.setOnClickListener(v -> onNextClicked());
        skipButton.setOnClickListener(v -> onSkipClicked());

    }


    /**
     * Set up the onboarding pages
     */
    private void setupPages() {
        pages = new ArrayList<>();

        // Add onboarding pages
        pages.add(new OnboardingPage(
                R.drawable.onboarding_collect,
                R.string.onboarding_title_collect,
                R.string.onboarding_desc_collect
        ));

        pages.add(new OnboardingPage(
                R.drawable.onboarding_scan,
                R.string.onboarding_title_scan,
                R.string.onboarding_desc_scan
        ));

        pages.add(new OnboardingPage(
                R.drawable.onboarding_wallet,
                R.string.onboarding_title_wallet,
                R.string.onboarding_desc_wallet
        ));
    }

    /**
     * Set up the ViewPager with adapter and page change listener
     */
    private void setupViewPager() {
        // Create and set adapter
        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        // Setting TabLayout with ViewPager2
        TabLayout tabLayout = findViewById(R.id.onboarding_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {

        }).attach();

        //  page change callback to update UI
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonsForPage(position);
            }
        });

        // Initial button setup
        updateButtonsForPage(0);
    }

    /**
     * Update button text and visibility based on current page
     *
     * @param position The current page position
     */
    private void updateButtonsForPage(int position) {
        boolean isLastPage = position == pages.size() - 1;

        // Update Next button text based on page
        if (isLastPage) {
            nextButton.setText(R.string.onboarding_get_started);
        } else {
            nextButton.setText(R.string.onboarding_next);
        }
    }

    /**
     * Handle Next button clicks
     */
    private void onNextClicked() {
        // Get current page position
        int currentPage = viewPager.getCurrentItem();

        // If last page, complete onboarding
        if (currentPage == pages.size() - 1) {
            completeOnboarding();
        } else {
            // Otherwise, go to next page
            viewPager.setCurrentItem(currentPage + 1);
        }
    }

    /**
     * Handle Skip button clicks (will mark onboarding completed and takes user to main screen)
     */
    private void onSkipClicked() {
        completeOnboarding();
    }


    private void completeOnboarding() {
        // Save that onboarding is complete
        prefsManager.setBoolean(PREF_ONBOARDING_COMPLETED, true);

        // Navigate to main screen
        navigateToMainScreen();
    }


    private void navigateToMainScreen() {
        Intent intent = new Intent(this, QrScannerActivity.class);
        startActivity(intent);

        // Close this activity so users won't go back to onboarding
        finish();
    }
}