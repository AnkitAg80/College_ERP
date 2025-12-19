package edu.univ.erp.ui.common;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.admin.AdminPanel;
import edu.univ.erp.ui.instructor.InstructorPanel;
import edu.univ.erp.auth.LoginFrame;
import edu.univ.erp.ui.student.StudentPanel;
import edu.univ.erp.access.AccessControl; // ADD THIS

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;


/**
this class acts as the decision maker for loading different panels after the user will login
 Mainframe will check the role of the user and will laod the respective panel only.
**/

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private JPanel maintenanceBanner;
    private UserSession userSession;

    public MainFrame(UserSession userSession) throws SQLException {
        this.userSession = userSession;

        setTitle("University ERP System - " + userSession.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        maintenanceBanner = createMaintenanceBanner();
        add(maintenanceBanner, BorderLayout.NORTH);

        // ensuring that only the instructor and the student gets to see the banner.
        updateMaintenanceBanner();

        /**when the user will logout from any of the panel then the code won't stop
        instead it will again show the loginpage.
        **/
        ActionListener logoutAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new LoginFrame().setVisible(true);
                });
            }
        };

        // Main content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        switch (userSession.getRole()) {
            case "Admin":
                contentPanel.add(new AdminPanel(userSession, logoutAction), BorderLayout.CENTER);
                break;
            case "Instructor":
                contentPanel.add(new InstructorPanel(userSession, logoutAction), BorderLayout.CENTER);
                break;
            default:
                contentPanel.add(new StudentPanel(userSession, logoutAction), BorderLayout.CENTER);
                break;
        }

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createMaintenanceBanner() {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        banner.setBackground(new Color(255, 193, 7)); // Warning yellow
        banner.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel warningText = new JLabel("MAINTENANCE MODE ACTIVE - System is in read-only mode.");
        warningText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warningText.setForeground(new Color(133, 100, 4)); // Dark yellow text

        banner.add(Box.createHorizontalStrut(15));
        banner.add(warningText);

        return banner;
    }

    // this method ensures that only the instructor and student see the maintainance banner.
    private void updateMaintenanceBanner() {
        boolean isMaintenanceMode = AccessControl.isMaintenanceMode();
        boolean isAdmin = userSession.isAdmin();

        // Show banner ONLY for non-admin users when maintenance is ON
        maintenanceBanner.setVisible(isMaintenanceMode && !isAdmin);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to set FlatLaf look and feel.");
        }
        UserSession testSession = new UserSession(1, "test_admin", "Admin");
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame(testSession);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}