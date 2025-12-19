package edu.univ.erp.access;

import edu.univ.erp.dao.AdminDAO;

// this class handles the logic of maintaince mode and write permissions when maintaince mode is on or off.
public class AccessControl {

    // Create an instance of the DAO to use
    private static AdminDAO adminDAO = new AdminDAO();

    // Checks if the system is in maintenance mode
    public static boolean isMaintenanceMode() {
        return adminDAO.isMaintenanceModeOn();
    }

    //Checks if the write action is allowed for the user that is the instructor or the student.
    public static boolean isWriteAllowed(String role) {
        if (isMaintenanceMode()) {
            // Only Admin can write when maintenance is on
            return "Admin".equalsIgnoreCase(role);
        }
        // Everyone can write when maintenance is off
        return true;
    }
}