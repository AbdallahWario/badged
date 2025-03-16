package org.defalsified.android.badged.ui.onboarding;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Simple data class representing a single onboarding page
 */
public class OnboardingPage {
    // Resource IDs for page content
    private final int imageResId;
    private final int titleResId;
    private final int descriptionResId;

    /**
     * Constructor for an onboarding page
     *
     * @param imageResId Resource ID for the illustration
     * @param titleResId Resource ID for the title text
     * @param descriptionResId Resource ID for the description text
     */
    public OnboardingPage(
            @DrawableRes int imageResId,
            @StringRes int titleResId,
            @StringRes int descriptionResId) {
        this.imageResId = imageResId;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
    }

    // Getters
    public int getImageResId() {
        return imageResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }
}