package org.defalsified.android.badged.services;

import android.content.Context;
import android.util.Log;

import org.defalsified.android.badged.models.Badge;
import org.defalsified.android.badged.utils.PrefsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository for managing badge data using SharedPreferences
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
     */
    public boolean saveBadge(Badge badge) {
        try {
            // Get all badges
            List<Badge> badges = getAllBadges();

            // Check if badge already exists
            for (int i = 0; i < badges.size(); i++) {
                if (badges.get(i).getSerial().equals(badge.getSerial())) {
                    // Update existing badge
                    badges.set(i, badge);
                    return saveBadgeList(badges);
                }
            }

            // Add new badge
            badges.add(badge);

            // Sorting vouchers latest first( descending order by serial number)
            badges.sort(new Comparator<Badge>() {
                @Override
                public int compare(Badge b1, Badge b2) {
                    try {
                        int serial1 = Integer.parseInt(b1.getSerial());
                        int serial2 = Integer.parseInt(b2.getSerial());
                        return Integer.compare(serial2, serial1);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Failed to parse serial numbers as integers, using string comparison");
                        return b2.getSerial().compareTo(b1.getSerial());
                    }
                }
            });

            return saveBadgeList(badges);

        } catch (Exception e) {
            Log.e(TAG, "Error saving badge", e);
            return false;
        }
    }

    /**
     * Get a badge by its serial
     */
    public Badge getBadgeById(String badgeSerial) {
        List<Badge> badges = getAllBadges();

        for (Badge badge : badges) {
            if (badge.getSerial().equals(badgeSerial)) {
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
                badge.setSerial(badgeJson.getString("serial"));
                badge.setOffer(badgeJson.getString("offer"));
                badge.setHolder(badgeJson.getString("holder"));
                badge.setProject(badgeJson.getString("project"));
                badge.setCertificateData(badgeJson.getString("certificateData"));
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
     */
    private boolean saveBadgeList(List<Badge> badges) {
        try {
            JSONArray badgesArray = new JSONArray();

            for (Badge badge : badges) {
                JSONObject badgeJson = new JSONObject();
                badgeJson.put("serial", badge.getSerial());
                badgeJson.put("offer", badge.getOffer());
                badgeJson.put("holder", badge.getHolder());
                badgeJson.put("project", badge.getProject());
                badgeJson.put("certificateData", badge.getCertificateData());
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