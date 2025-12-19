package edu.univ.erp.domain;

import java.time.LocalDateTime;

 //Corresponds to the 'audit_log' table in erpdb
public class AuditLog {
    private int auditId;
    private Integer userId;
    private String action;
    private String objectType;
    private Integer objectId;
    private String details;
    private LocalDateTime createdAt;

    //default constructor
    public AuditLog() {
    }

    // constructor with all the fields
    public AuditLog(int auditId, Integer userId, String action, String objectType, Integer objectId, String details, LocalDateTime createdAt) {
        this.auditId = auditId;
        this.userId = userId;
        this.action = action;
        this.objectType = objectType;
        this.objectId = objectId;
        this.details = details;
        this.createdAt = createdAt;
    }

    // constructor without some fields for new entries
    public AuditLog(Integer userId, String action, String objectType,
                    Integer objectId, String details) {
        this.userId = userId;
        this.action = action;
        this.objectType = objectType;
        this.objectId = objectId;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "auditId=" + auditId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", objectType='" + objectType + '\'' +
                ", objectId=" + objectId +
                ", details='" + details + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
