package edu.univ.erp.domain;

import java.time.LocalDateTime;

 //corresponds to the 'announcements' table in erpdb
 //this class is used for displaying general messages to users in the system
public class Announcement {
    private int announcementId;
    private String title;
    private String message;
    private int createdBy;           // creator user id
    private String createdByName;    // creator display name
    private LocalDateTime createdAt;

    // default constructor
    public Announcement() {
    }

    // this is the constructor with all the fields
    public Announcement(int announcementId, String title, String message, int createdBy,
                        String createdByName, LocalDateTime createdAt) {
        this.announcementId = announcementId;
        this.title = title;
        this.message = message;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }


    //constructor without announcementId for creating new announcements
    public Announcement(String title, String message, int createdBy) {
        this.title = title;
        this.message = message;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public int getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "announcementId=" + announcementId +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", createdBy=" + createdBy +
                ", createdByName='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

