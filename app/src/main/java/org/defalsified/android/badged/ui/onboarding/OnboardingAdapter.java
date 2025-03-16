package org.defalsified.android.badged.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.defalsified.android.badged.R;

import java.util.List;

/**
 *  ViewPager2 adapter(displays onboarding pages)
 */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // List of pages to display
    private final List<OnboardingPage> pages;

    /**
     * Constructor
     *
     * @param pages List of OnboardingPage objects to display
     */
    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single onboarding page
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);

        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Get the page's  position and bind to the holder
        OnboardingPage page = pages.get(position);
        holder.bind(page);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    /**
     * ViewHolder for an onboarding page
     */
    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        // UI elements from layout
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views from layout
            imageView = itemView.findViewById(R.id.onboarding_image);
            titleTextView = itemView.findViewById(R.id.onboarding_title);
            descriptionTextView = itemView.findViewById(R.id.onboarding_description);
        }

        /**
         * Bind data from an OnboardingPage to this ViewHolder
         *
         * @param page The page data to display
         */
        public void bind(OnboardingPage page) {
            // Set image and text resources
            imageView.setImageResource(page.getImageResId());
            titleTextView.setText(page.getTitleResId());
            descriptionTextView.setText(page.getDescriptionResId());
        }
    }
}