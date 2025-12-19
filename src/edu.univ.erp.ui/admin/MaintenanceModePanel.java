package edu.univ.erp.ui.admin;

import edu.univ.erp.dao.AdminDAO;
import javax.swing.*;
import java.awt.*;

public class MaintenanceModePanel extends JPanel {
    private AdminDAO adminDAO;
    private JToggleButton toggleButton;
    private JLabel statusLabel;
    private JTextArea messageArea;
    private Timer refreshTimer;

    public MaintenanceModePanel() {
        this.adminDAO = new AdminDAO();
        initializeUI();
        updateStatus();
        startAutoRefresh();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Maintenance & Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Toggle button panel
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        togglePanel.setBackground(Color.WHITE);

        toggleButton = new JToggleButton("ENABLE MAINTENANCE MODE");
        toggleButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleButton.setPreferredSize(new Dimension(320, 55));
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> onToggleMaintenanceMode());

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        togglePanel.add(toggleButton);
        togglePanel.add(statusLabel);

        panel.add(togglePanel, BorderLayout.NORTH);
        JLabel messageLabel = new JLabel("System Message When Maintenance is ON:");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        messageArea = new JTextArea(3, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(240, 240, 240));
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setText("System is in maintenance mode. You can view data but cannot make changes. Please try again later.");
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(messagePanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("ℹ️ Impact:");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoLabel.setForeground(new Color(100, 100, 100));

        JTextArea infoArea = new JTextArea(4, 40);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(255, 243, 205));
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoArea.setText(
                "• Students CAN login but CANNOT register/drop courses\n" +
                        "• Instructors CAN login but CANNOT modify grades\n" +
                        "• Admin CAN login and perform all operations\n" +
                        "• All non-admin write attempts will be blocked with a warning message"
        );
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(infoArea, BorderLayout.CENTER);

        bottomPanel.add(infoPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void onToggleMaintenanceMode() {
        boolean newState = toggleButton.isSelected();
        boolean success = adminDAO.updateMaintenanceMode(newState);

        if (success) {
            boolean verifiedState = adminDAO.isMaintenanceModeOn();
            System.out.println("Database updated: " + verifiedState);

            if (verifiedState == newState) {
                updateStatus();

                String message = newState ?
                        "MAINTENANCE MODE ENABLED\n\n" +
                                "• Students and Instructors can login but CANNOT make changes\n" +
                                "• All write operations (register, drop, grade entry) are blocked\n" +
                                "• Warning banner will display for non-admin users\n\n" +
                                "The system is now in READ-ONLY mode for non-admins." :

                        "MAINTENANCE MODE DISABLED\n\n" +
                                "• All users can login and make changes normally\n" +
                                "• System is fully operational\n" +
                                "• No restrictions on write operations";

                JOptionPane.showMessageDialog(this,
                        message,
                        "Maintenance Mode Updated",
                        newState ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.err.println("ERROR: Verification failed!");
                JOptionPane.showMessageDialog(this,
                        "Database verification failed!\n\n" +
                                "Expected: " + newState + "\n" +
                                "Got: " + verifiedState + "\n\n" +
                                "Please check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                updateStatus();
            }
        } else {
            System.err.println("ERROR: Database update failed!");
            JOptionPane.showMessageDialog(this,
                    "Failed to update database.\n\n" +
                            "Please check:\n" +
                            "• Database connection\n" +
                            "• settings table exists\n" +
                            "• Proper permissions",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            updateStatus();
        }
    }

    private void updateStatus() {
        boolean isEnabled = adminDAO.isMaintenanceModeOn();
        toggleButton.setSelected(isEnabled);
        if (isEnabled) {
            toggleButton.setText("🛑 DISABLE MAINTENANCE MODE");
            toggleButton.setBackground(new Color(220, 53, 69));
            toggleButton.setForeground(Color.WHITE);
            statusLabel.setText("STATUS: MAINTENANCE ACTIVE");
            statusLabel.setForeground(new Color(220, 53, 69));
        }
        else {
            toggleButton.setText("⚠️  ENABLE MAINTENANCE MODE");
            toggleButton.setBackground(new Color(40, 167, 69));
            toggleButton.setForeground(Color.WHITE);
            statusLabel.setText("STATUS: SYSTEM OPERATIONAL");
            statusLabel.setForeground(new Color(40, 167, 69));
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(3000, e -> {
            boolean currentState = toggleButton.isSelected();
            boolean dbState = adminDAO.isMaintenanceModeOn();
            if (currentState != dbState) updateStatus();
        });
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) refreshTimer.stop();
    }
}