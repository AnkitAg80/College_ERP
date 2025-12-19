package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


 //Handles all authentication operations against our auth database.
 //This class manages user login, password changes, and session creation.
public class AuthAPI {

     //Validates user credentials and creates a session if authentication succeeds.
    public static UserSession login(String username, String password) {
        String userQuery = "SELECT user_id, password_hash, role FROM users_auth WHERE username = ?";

        try (Connection authDbConnection = DatabaseConnection.getInstance().getAuthConnection();
             PreparedStatement statement = authDbConnection.prepareStatement(userQuery)) {

            statement.setString(1, username);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Found the user, now verify the password
                    int id = resultSet.getInt("user_id");
                    String hashedPassword = resultSet.getString("password_hash");
                    String userRole = resultSet.getString("role");

                    // Check if the provided password matches the stored hash
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        updateLastLogin(id, authDbConnection);
                        // Return a new session for this user
                        return new UserSession(id, username, userRole);
                    }
                    // Password didn't match
                    return null;
                }
                // No user found with this username
                return null;
            }
        } catch (SQLException dbError) {
            System.err.println("Database connection failed during login: " + dbError.getMessage());
            dbError.printStackTrace();
            return null;
        }
    }


    private static void updateLastLogin(int userId, Connection dbConnection) {
        String updateQuery = "UPDATE users_auth SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, userId);
            updateStatement.executeUpdate();
        } catch (SQLException updateError) {
            System.err.println("Failed to update last login time for user " + userId + ": " + updateError.getMessage());
        }
    }


    //Changes a user's password after verifying their current password.

    public static boolean changePassword(int userId, String currentPassword, String newPassword) {
        // First, verify the current password
        String verifyQuery = "SELECT password_hash FROM users_auth WHERE user_id = ?"; // "?" tells Java that a value will be safely inserted here at runtime.
        String updateQuery = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";

        try (Connection authDbConnection = DatabaseConnection.getInstance().getAuthConnection()) {

            // first getting the current password hash and verifying it
            try (PreparedStatement verifyStatement = authDbConnection.prepareStatement(verifyQuery)) {
                verifyStatement.setInt(1, userId);

                try (ResultSet resultSet = verifyStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        System.err.println("Password change failed: User ID " + userId + " not found");
                        return false;
                    }

                    String storedHash = resultSet.getString("password_hash");
                    if (!BCrypt.checkpw(currentPassword, storedHash)) {
                        System.err.println("Password change failed: Current password incorrect for user " + userId);
                        return false;
                    }
                }
            }

            // hashing the new password
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            // adding the new password hash to the database
            try (PreparedStatement updateStatement = authDbConnection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newHash);
                updateStatement.setInt(2, userId);

                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows > 0) {
                    System.out.println("Password successfully updated for user " + userId);
                    return true;
                } else {
                    System.err.println("Password update failed: No rows affected for user " + userId);
                    return false;
                }
            }

        } catch (SQLException dbError) {
            System.err.println("Database error during password change for user " + userId + ": " + dbError.getMessage());
            dbError.printStackTrace();
            return false;
        }
    }
}