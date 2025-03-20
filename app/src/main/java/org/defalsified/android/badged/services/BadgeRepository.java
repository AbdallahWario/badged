package org.defalsified.android.badged.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.utils.PrefsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing badge data
 * Note: This is a simplified implementation using SharedPreferences
 * we will do a wala fetch from here
 */
public class BadgeRepository {
    private static final String TAG = "BadgeRepository";
    private static final String PREF_BADGES = "user_badges";

    private final PrefsManager prefsManager;

    public BadgeRepository(Context context) {
        this.prefsManager = new PrefsManager(context);
    }

    /**
     * Save a badge to storage
     *
     * @param badge Badge to save
     * @return true if save was successful
     */
    public boolean saveBadge(Badge badge) {
        try {
            // Get all badges
            List<Badge> badges = getAllBadges();

            // Check if badge already exists
            for (int i = 0; i < badges.size(); i++) {
                if (badges.get(i).getId().equals(badge.getId())) {
                    // Update existing badge
                    badges.set(i, badge);
                    return saveBadgeList(badges);
                }
            }

            // Add new badge
            badges.add(badge);
            return saveBadgeList(badges);

        } catch (Exception e) {
            Log.e(TAG, "Error saving badge", e);
            return false;
        }
    }

    /**
     * Get a badge by its ID
     *
     * @param badgeId ID of the badge to retrieve
     * @return Badge object or null if not found
     */
    public Badge getBadgeById(String badgeId) {
        List<Badge> badges = getAllBadges();

        for (Badge badge : badges) {
            if (badge.getId().equals(badgeId)) {
                return badge;
            }
        }

        return null;
    }

    /**
     * Get and return all badges
     */
    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();

        String badgesJson = prefsManager.getString(PREF_BADGES, "[]");

        try {
            JSONArray badgesArray = new JSONArray(badgesJson);

            for (int i = 0; i < badgesArray.length(); i++) {
                JSONObject badgeJson = badgesArray.getJSONObject(i);

                Badge badge = new Badge();
                badge.setId(badgeJson.getString("id"));
                badge.setName(badgeJson.getString("name"));
                badge.setDescription(badgeJson.getString("description"));
                badge.setImageUrl(badgeJson.getString("imageUrl"));
                badge.setTimestamp(badgeJson.getLong("timestamp"));

                badges.add(badge);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing badges JSON", e);
        }

        return badges;
    }

    /**
     * Save a list of badges to storage
     *
     * @param badges List of badges to save
     * @return true if save was successful
     */
    private boolean saveBadgeList(List<Badge> badges) {
        try {
            JSONArray badgesArray = new JSONArray();

            for (Badge badge : badges) {
                JSONObject badgeJson = new JSONObject();
                badgeJson.put("id", badge.getId());
                badgeJson.put("name", badge.getName());
                badgeJson.put("description", badge.getDescription());
                badgeJson.put("imageUrl", badge.getImageUrl());
                badgeJson.put("timestamp", badge.getTimestamp());

                badgesArray.put(badgeJson);
            }

            prefsManager.setString(PREF_BADGES, badgesArray.toString());
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating badges JSON", e);
            return false;
        }
    }
}