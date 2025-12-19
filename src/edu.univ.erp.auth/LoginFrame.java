package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseInitializer;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.access.AccessControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;


// Login screen with a clean, minimal design inspired by modern web apps
public class LoginFrame extends JFrame {

    // Color scheme - keeping it simple and professional

    private static final Color BACKGROUND = new Color(255, 255, 255);
    private static final Color SURFACE = new Color(248, 249, 250);
    private static final Color BORDER = new Color(218, 220, 224);
    private static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    private static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    private static final Color ACCENT = new Color(26, 115, 232);
    private static final Color ERROR_COLOR = new Color(217, 48, 37);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;
    private int failedAttempts = 0;


    public LoginFrame() {
        DatabaseInitializer.initializeDatabases();
        initializeUI();
    }


    private void initializeUI() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main container - centers everything on screen
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND);

        // The actual login card
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(SURFACE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(50, 60, 50, 60)
        ));

        // Header section
        JLabel logoLabel = new JLabel("University ERP");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoLabel.setForeground(TEXT_PRIMARY);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(logoLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(50));

        // Username input
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(TEXT_PRIMARY);

        usernameField = new JTextField();
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(400, 45));
        usernameField.setMaximumSize(new Dimension(400, 45));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        cardPanel.add(usernameLabel);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(usernameField);
        cardPanel.add(Box.createVerticalStrut(25));


        // Password input
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(400, 45));
        passwordField.setMaximumSize(new Dimension(400, 45));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(passwordField);
        cardPanel.add(Box.createVerticalStrut(35));



        // Login button
        loginButton = new JButton("Sign in");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setBackground(ACCENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(400, 45));
        loginButton.setMaximumSize(new Dimension(400, 45));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(new LoginActionListener());

        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(20));

        // Error/status messages show up here
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(messageLabel);



        // Quick shortcuts - Enter key moves between fields and submits
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> loginButton.doClick());

        mainPanel.add(cardPanel);
        add(mainPanel);
    }


    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Basic validation first
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password");
                return;
            }

            // Try to authenticate
            UserSession user = AuthAPI.login(username, password);

            if (user != null) {
                messageLabel.setText(" ");

                // Non-admin users need to be warned about maintenance mode
                if (!user.isAdmin() && AccessControl.isMaintenanceMode()) {
                    int choice = JOptionPane.showConfirmDialog(
                            LoginFrame.this,
                            "System Maintenance Mode\n\n" +
                                    "The system is in maintenance mode.\n" +
                                    "You can view data but cannot make changes.\n\n" +
                                    "Continue?",
                            "Maintenance Mode",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                openMainFrame(user);

            } else {
                // Track failed login attempts to prevent brute force
                failedAttempts++;
                messageLabel.setText("Incorrect username or password");

                if (failedAttempts >= 5) {
                    messageLabel.setText("Too many failed attempts. Please wait 30 seconds.");
                    loginButton.setEnabled(false);

                    // Re-enable login after 30 seconds
                    new Timer(30000, evt -> {
                        loginButton.setEnabled(true);
                        messageLabel.setText(" ");
                        failedAttempts = 0;
                        ((Timer) evt.getSource()).stop();
                    }).start();
                }
            }
        }
    }





    private void openMainFrame(UserSession user) {
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
                dispose();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error loading application",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}