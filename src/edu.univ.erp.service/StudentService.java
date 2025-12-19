package edu.univ.erp.service;

import edu.univ.erp.dao.AnnouncementDAO;
import edu.univ.erp.domain.Announcement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// this class makes sure that the student is able to view all the annouuncements made by the admin.
public class StudentService {

    private AnnouncementDAO announcementDAO;

    public StudentService() {
        this.announcementDAO = new AnnouncementDAO();
    }

    // this method retrieves all announcements from the database for students to view
    public List<Announcement> getAllAnnouncements() {
        try {
            System.out.println("DEBUG: StudentService.getAllAnnouncements() called");
            List<Announcement> result = announcementDAO.getAllAnnouncements();
            System.out.println("DEBUG: Retrieved " + result.size() + " announcements from database");
            return result;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get announcements: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}