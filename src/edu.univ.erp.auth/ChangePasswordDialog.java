package edu.univ.erp.auth;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changeButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    private UserSession userSession;

    public ChangePasswordDialog(Frame owner, UserSession userSession) {
        super(owner, "Change Password", true);
        this.userSession = userSession;
        initializeUI();
    }

    private void initializeUI() {
        setSize(450, 430);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new MigLayout("wrap, fillx, insets 20", "[grow, fill]", "[]10[]10[]10[]10[]10[]10[]"));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel currentPasswordLabel = new JLabel("Current Password:");
        currentPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentPasswordLabel.setForeground(new Color(52, 73, 94));

        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newPasswordLabel.setForeground(new Color(52, 73, 94));

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPasswordLabel.setForeground(new Color(52, 73, 94));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(231, 76, 60));

        JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]10[]", "[]"));
        buttonPanel.setBackground(Color.WHITE);

        changeButton = new JButton("Change Password");
        changeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changeButton.setBackground(new Color(41, 128, 185));
        changeButton.setForeground(Color.WHITE);
        changeButton.setFocusPainted(false);
        changeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        changeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(new Color(52, 73, 94));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(changeButton, "grow");
        buttonPanel.add(cancelButton, "grow");

        mainPanel.add(titleLabel, "center, wrap, gapbottom 20");
        mainPanel.add(currentPasswordLabel, "wrap");
        mainPanel.add(currentPasswordField, "h 35!");
        mainPanel.add(newPasswordLabel, "wrap");
        mainPanel.add(newPasswordField, "h 35!");
        mainPanel.add(confirmPasswordLabel, "wrap");
        mainPanel.add(confirmPasswordField, "h 35!");
        mainPanel.add(messageLabel, "center, h 20!, wrap");
        mainPanel.add(buttonPanel, "grow");

        add(mainPanel);

        // Add action listeners
        changeButton.addActionListener(e -> changePassword());
        cancelButton.addActionListener(e -> dispose());
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("New passwords do not match");
            return;
        }

        if (newPassword.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters");
            return;
        }

        // Call Auth API to change password with userId
        boolean success = AuthAPI.changePassword(userSession.getUserId(), currentPassword, newPassword);

        if (success) {
            messageLabel.setText("Password changed successfully!");
            messageLabel.setForeground(new Color(39, 174, 96));

            // Close dialog after a short delay
            Timer timer = new Timer(1500, e -> dispose());
            timer.setRepeats(false);
            timer.start();
        } else {
            messageLabel.setText("Current password is incorrect");
            messageLabel.setForeground(new Color(231, 76, 60));
        }
    }
}