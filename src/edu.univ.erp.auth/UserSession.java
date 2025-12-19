package edu.univ.erp.auth;

/**
 * UserSession holds information about the currently logged-in user.
 * This is used to store the user's login info.
 */
public class UserSession {
    private int userId;
    private String username;
    private String role;

    // constructor used to create a new UserSession
    public UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }


    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return "UserSession{" + "userId=" + userId + ", username='" + username + '\'' + ", role='" + role + '\'' + '}';
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    public boolean isInstructor() {
        return "Instructor".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "Student".equalsIgnoreCase(role);
    }
}