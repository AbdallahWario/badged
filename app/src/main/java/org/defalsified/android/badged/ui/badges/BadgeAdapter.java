package org.defalsified.android.badged.ui.badges;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.defalsified.android.badged.R;
import org.defalsified.android.badged.models.Badge;

import java.util.List;

/**
 * Adapter for displaying badges in a RecyclerView
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<Badge> badges;
    private final OnBadgeClickListener listener;

    /**
     * Interface for badge click events
     */
    public interface OnBadgeClickListener {
        void onBadgeClick(Badge badge);
    }

    /**
     * Constructor
     *
     * @param badges List of badges to display
     * @param listener Click listener for badge items
     */
    public BadgeAdapter(List<Badge> badges, OnBadgeClickListener listener) {
        this.badges = badges;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.bind(badge, listener);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    /**
     * ViewHolder for badge items
     */
    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView badgeImage;
        private final TextView badgeName;
        private final TextView badgeDate;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeImage = itemView.findViewById(R.id.badge_image);
            badgeName = itemView.findViewById(R.id.badge_name);
            badgeDate = itemView.findViewById(R.id.badge_date);
        }

        /**
         * Bind data to views
         *
         * @param badge Badge data
         * @param listener Click listener
         */
        public void bind(Badge badge, OnBadgeClickListener listener) {
            // Set badge name and date
            badgeName.setText(badge.getDisplayName());
            badgeDate.setText(badge.getFormattedDate());

            // Set badge image (using a placeholder for now)
            badgeImage.setImageResource(R.drawable.badge_placeholder);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBadgeClick(badge);
                }
            });
        }
    }
}