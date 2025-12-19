package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Announcement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//this class is to handle all the announcements that are made by the admin to the students.
public class AnnouncementDAO {



    //this method is to create a new announcement in the database
    public int create(Announcement newAnnouncement) throws SQLException {
        String createAnnouncementQuery = "INSERT INTO announcements (title, message, created_by, created_at) VALUES (?, ?, ?, ?)";

        // connect with the erpdb and execute the query
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement createStatement = dbConnection.prepareStatement(createAnnouncementQuery, Statement.RETURN_GENERATED_KEYS)) {

            createStatement.setString(1, newAnnouncement.getTitle());
            createStatement.setString(2, newAnnouncement.getMessage());
            createStatement.setInt(3, newAnnouncement.getCreatedBy());
            createStatement.setTimestamp(4, Timestamp.valueOf(newAnnouncement.getCreatedAt()));

            // add the values into the annoucements table
            int rowsAdded = createStatement.executeUpdate();

            if (rowsAdded > 0) {
                try (ResultSet generatedKeys = createStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Return the new announcement ID
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1; // an error occurred
        }
    }



   // this is to show all the announcements that are made by the admin to the students.
    public List<Announcement> getAllAnnouncements() throws SQLException {
        System.out.println("DEBUG: Fetching all announcements from the database");
        List<Announcement> announcementList = new ArrayList<>();

        // We need to join with the auth database to get the creator's username
        String fetchAllQuery = "SELECT a.announcement_id, a.title, a.message, a.created_by, a.created_at, " +
                "u.username as creator_name " +
                "FROM announcements a " +
                "JOIN authdb.users_auth u ON a.created_by = u.user_id " +
                "ORDER BY a.created_at DESC"; // Show the latest announcements first

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement fetchStatement = dbConnection.prepareStatement(fetchAllQuery);
             ResultSet announcementResults = fetchStatement.executeQuery()) {

            System.out.println("DEBUG: Query executed successfully");

            while (announcementResults.next()) {
                Announcement currentAnnouncement = new Announcement();
                currentAnnouncement.setAnnouncementId(announcementResults.getInt("announcement_id"));
                currentAnnouncement.setTitle(announcementResults.getString("title"));
                currentAnnouncement.setMessage(announcementResults.getString("message"));
                currentAnnouncement.setCreatedBy(announcementResults.getInt("created_by"));

                // Add the creator's name to display it
                currentAnnouncement.setCreatedByName(announcementResults.getString("creator_name"));

                // to convert the SQL timestamp to LocalDateTime of java
                currentAnnouncement.setCreatedAt(announcementResults.getTimestamp("created_at").toLocalDateTime());
                announcementList.add(currentAnnouncement);
            }
            System.out.println("DEBUG: Loaded " + announcementList.size() + " announcements");
        }
        return announcementList;
    }



    //this function gets the unique announcement id from the database
    public Announcement getById(int announcementId) throws SQLException {
        // We're joining with the users_auth table to get the creator's username
        String fetchByIdQuery = "SELECT a.announcement_id, a.title, a.message, a.created_by, a.created_at, " +
                "u.username as creator_name " +
                "FROM announcements a " +
                "JOIN authdb.users_auth u ON a.created_by = u.user_id " +
                "WHERE a.announcement_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement fetchStatement = dbConnection.prepareStatement(fetchByIdQuery)) {
            fetchStatement.setInt(1, announcementId);

            try (ResultSet announcementResults = fetchStatement.executeQuery()) {
                if (announcementResults.next()) {
                    Announcement foundAnnouncement = new Announcement();
                    foundAnnouncement.setAnnouncementId(announcementResults.getInt("announcement_id"));
                    foundAnnouncement.setTitle(announcementResults.getString("title"));
                    foundAnnouncement.setMessage(announcementResults.getString("message"));
                    foundAnnouncement.setCreatedBy(announcementResults.getInt("created_by"));

                    // Get the username from the users_auth table
                    foundAnnouncement.setCreatedByName(announcementResults.getString("creator_name"));
                    foundAnnouncement.setCreatedAt(announcementResults.getTimestamp("created_at").toLocalDateTime());
                    return foundAnnouncement;
                }
            }
        }

        return null; // No announcement found with this ID
    }



    // this function is to edit the existing announcement in the database
    public boolean update(Announcement updatedAnnouncement) throws SQLException {
        String updateQuery = "UPDATE announcements SET title = ?, message = ? WHERE announcement_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement updateStatement = dbConnection.prepareStatement(updateQuery)) {

            updateStatement.setString(1, updatedAnnouncement.getTitle());
            updateStatement.setString(2, updatedAnnouncement.getMessage());
            updateStatement.setInt(3, updatedAnnouncement.getAnnouncementId());

            int rowsUpdated = updateStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }



    //this method deletes an announcement from the database
    public boolean delete(int announcementId) throws SQLException {
        String deleteQuery = "DELETE FROM announcements WHERE announcement_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteQuery)) {
            deleteStatement.setInt(1, announcementId);
            int rowsDeleted = deleteStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}