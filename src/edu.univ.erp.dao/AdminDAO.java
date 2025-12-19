package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.ui.admin.AdminPanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//this class is for connecting the authdb and erpdb with the admin panel, it handles all the admin related db operations.
public class AdminDAO {

    //it is used to create a new user in the auth database, this is done through the admin panel, when the admin adds a new user.
    public int insertAuthUser(String username, String passwordHash, String role) {
        String addUserQuery = "INSERT INTO users_auth (username, password_hash, role, status) VALUES (?, ?, ?, 'active')";

        // making the connection between java and authdb this is done by calling the getAuthConnection method from DatabaseConnection class
        try (Connection dbConnection = DatabaseConnection.getInstance().getAuthConnection();
             PreparedStatement addUserStatement = dbConnection.prepareStatement(addUserQuery, Statement.RETURN_GENERATED_KEYS)) {

            //after making the connection setting the values in the sql
            addUserStatement.setString(1, username);
            addUserStatement.setString(2, passwordHash);
            addUserStatement.setString(3, role);

            int rowsAffected = addUserStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = addUserStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the new user's ID
                    }
                }
            }
        }
        catch (SQLException dbError) {
            System.err.println("Error creating new user: " + dbError.getMessage());
            dbError.printStackTrace();
        }
        return -1; // Something went wrong
    }

    /**
     * Removes a user from the authentication database
     * Thanks to our ON DELETE CASCADE rule, this will also clean up related records
     * @param userId The ID of the user to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAuthUser(int userId) throws SQLException {
        String removeUserQuery = "DELETE FROM users_auth WHERE user_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getAuthConnection();
             PreparedStatement removeUserStatement = dbConnection.prepareStatement(removeUserQuery)) {
            removeUserStatement.setInt(1, userId);
            int rowsAffected = removeUserStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    //in the admin dashboard we show some statistics like total number of students, instructors, courses and branches
    // so the below method fetches those statistics from the erp database
    // to do so we are retrieving the data from the 4 tables that are students, instructors, courses and branches.
    public AdminPanel.DashboardStats getDashboardStats() throws SQLException {
        int totalStudents = 0;
        int totalInstructors = 0;
        int totalCourses = 0;
        int totalBranches = 0; // New addition to our stats

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection()) {
            // counting the students
            try (Statement countStatement = dbConnection.createStatement();
                 ResultSet studentResults = countStatement.executeQuery("SELECT COUNT(*) FROM students")) {
                if (studentResults.next()) totalStudents = studentResults.getInt(1);
            }

            // counting the instructors
            try (Statement countStatement = dbConnection.createStatement();
                 ResultSet instructorResults = countStatement.executeQuery("SELECT COUNT(*) FROM instructors")) {
                if (instructorResults.next()) totalInstructors = instructorResults.getInt(1);
            }

            // counting the courses
            try (Statement countStatement = dbConnection.createStatement();
                 ResultSet courseResults = countStatement.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (courseResults.next()) {
                    {
                        totalCourses = courseResults.getInt(1);
                    }
                }
            }

            // counting the branches
            try (Statement countStatement = dbConnection.createStatement();
                 ResultSet branchResults = countStatement.executeQuery("SELECT COUNT(*) FROM branches")) {
                if (branchResults.next()) {
                    {
                        totalBranches = branchResults.getInt(1);
                    }
                }
            }
        }
        return new AdminPanel.DashboardStats(totalStudents, totalInstructors, totalCourses, totalBranches);
    }

    //this function is called in the maintenance panel and through this the maintenance mode is either set to true or false.
    public boolean updateMaintenanceMode(boolean isEnabled) {
        String maintenanceQuery = "INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_on', ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement maintenanceStatement = dbConnection.prepareStatement(maintenanceQuery)) {
            String maintenanceValue = String.valueOf(isEnabled);
            maintenanceStatement.setString(1, maintenanceValue);
            maintenanceStatement.setString(2, maintenanceValue);
            return maintenanceStatement.executeUpdate() > 0;
        }
        catch (SQLException dbError) {
            System.err.println("Failed to update maintenance mode: " + dbError.getMessage());
            dbError.printStackTrace();
            return false;
        }
    }

   // this method checks if the maintenance mode is on or off by connecting with the sql settings table, and in that if
   // the value of maintaince mode is true then it also returns true
    public boolean isMaintenanceModeOn() {
        String checkMaintenanceQuery = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement checkStatement = dbConnection.prepareStatement(checkMaintenanceQuery);
             ResultSet maintenanceResults = checkStatement.executeQuery()) {
            if (maintenanceResults.next()) {
                // Convert the string value that we get from database to a boolean.
                return Boolean.parseBoolean(maintenanceResults.getString("setting_value"));
            }
        } catch (SQLException dbError) {
            System.err.println("Error checking maintenance mode: " + dbError.getMessage());
            dbError.printStackTrace();
            // if the connection with the settings table fails then we assume that the maintenance is off.
            return false;
        }
        // If no setting is found, we'll assume maintenance is off
        return false;
    }
}