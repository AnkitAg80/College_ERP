package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;
import java.sql.*;
import java.util.*;

// this class handles system configuration and provides centralized access to system-wide settings
public class SettingsService {

    private static final String reg_deadline = "registration_deadline";

    private Map<String, String> settingsCache = new HashMap<>();

    // this method retrieves a setting value by its key from the cache
    public String getSetting(String key) {
        return settingsCache.getOrDefault(key, "");
    }

    // this method sets a setting value and saves it to the database
    public void setSetting(String key, String value) {
        settingsCache.put(key, value);
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = ?";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error saving setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // this method gets the registration deadline from settings
    public String getRegistrationDeadline() {
        return getSetting(reg_deadline);
    }

    // this method checks if the current date is after the registration deadline
    public boolean isAfterRegistrationDeadline() throws SQLException {
        loadAllSettings();
        String deadline = getRegistrationDeadline();

        if (deadline == null || deadline.isEmpty()) {
            return false; // No deadline set means registration is open
        }

        try {
            java.sql.Date deadlineDate = java.sql.Date.valueOf(deadline);
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            return today.after(deadlineDate);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid deadline format: " + deadline);
            return false;
        }
    }

    // this method loads all settings from the database
    public void loadAllSettings() {
        settingsCache.clear();
        String sql = "SELECT setting_key, setting_value FROM settings";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             // executing the query
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                settingsCache.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }

        }
        catch (SQLException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }

}