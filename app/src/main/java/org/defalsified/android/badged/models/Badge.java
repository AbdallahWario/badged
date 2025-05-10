package org.defalsified.android.badged.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Badge implements Serializable {
    private String serial;
    private String offer;
    private String holder;
    private String project;
    private String certificateData;
    private long timestamp;

    public Badge() {
        this.timestamp = System.currentTimeMillis();
    }

    public Badge(String serial, String offer, String holder,
                 String project, String certificateData) {
        this.serial = serial;
        this.offer = offer;
        this.holder = holder;
        this.project = project;
        this.certificateData = certificateData;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public String getOffer() { return offer; }
    public void setOffer(String offer) { this.offer = offer; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

    public String getCertificateData() { return certificateData; }
    public void setCertificateData(String certificateData) {
        this.certificateData = certificateData;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Display methods
    public String getDisplayName() {
        return offer + " Badge";
    }

    public String getDisplayDescription() {
        return String.format(
                "Offer: %s\nHolder: %s\nProject: %s",
                offer,
                holder,
                project
        );
    }

    public String getFormattedDate() {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMMM d, yyyy hh:mm:ss a", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Badge badge = (Badge) o;
        return serial != null ? serial.equals(badge.serial) : badge.serial == null;
    }

    @Override
    public int hashCode() {
        return serial != null ? serial.hashCode() : 0;
    }
}