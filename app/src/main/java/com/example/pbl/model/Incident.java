package com.example.pbl.model;

public class Incident {
    private long id;
    private String type; // CRASH, DANGER_SOS, BYSTANDER_REPORT
    private long timestamp;
    private String location;
    private String status; // ALERT_SENT, AMBULANCE_DISPATCHED, HOSPITAL_READY, POLICE_NOTIFIED, RESOLVED
    private boolean critical;
    private String details;

    public Incident() {
    }

    public Incident(long id, String type, long timestamp, String location, String status, boolean critical, String details) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.location = location;
        this.status = status;
        this.critical = critical;
        this.details = details;
    }

    public Incident(String type, long timestamp, String location, String status, boolean critical, String details) {
        this.type = type;
        this.timestamp = timestamp;
        this.location = location;
        this.status = status;
        this.critical = critical;
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
