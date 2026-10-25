package com.processhub.dto;

import java.time.Instant;

public class SlaInfoResponse {

    private Instant slaDueAt;
    private boolean slaBreached;
    private String slaStatus;
    private long timeRemainingMinutes;

    public SlaInfoResponse() {
    }

    public SlaInfoResponse(Instant slaDueAt, boolean slaBreached, String slaStatus, long timeRemainingMinutes) {
        this.slaDueAt = slaDueAt;
        this.slaBreached = slaBreached;
        this.slaStatus = slaStatus;
        this.timeRemainingMinutes = timeRemainingMinutes;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(Instant slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String slaStatus) {
        this.slaStatus = slaStatus;
    }

    public long getTimeRemainingMinutes() {
        return timeRemainingMinutes;
    }

    public void setTimeRemainingMinutes(long timeRemainingMinutes) {
        this.timeRemainingMinutes = timeRemainingMinutes;
    }
}
