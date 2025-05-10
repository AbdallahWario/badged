package org.defalsified.android.badged.dto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class BadgeDTO implements Serializable {
    private String serial;
    private String offer;
    private String holder;
    private String project;
    private String certificateData;

    public BadgeDTO() {}

    public BadgeDTO(JSONObject qrData) throws JSONException {
        this.serial = qrData.optString("serial", "unknown");
        this.offer = qrData.optString("offer", "Unclaimed Offer");
        this.holder = qrData.optString("holder", "Anonymous");
        this.project = qrData.optString("project", "Unknown");
        this.certificateData = qrData.optString("cert", "");
    }

    // Getters
    public String getSerial() { return serial; }
    public String getOffer() { return offer; }
    public String getHolder() { return holder; }
    public String getProject() { return project; }
    public String getCertificateData() { return certificateData; }
}