package org.defalsified.android.badged.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Badge {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private long timestamp;

    public Badge() {
        this.timestamp = System.currentTimeMillis();
    }

    public Badge(String id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * formatted date string for the badge acquisition date
     */

    public String getFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}