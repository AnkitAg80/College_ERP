package edu.univ.erp.ui.instructor;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.awt.Image;
import edu.univ.erp.auth.ChangePasswordDialog;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.dao.InstructorDAO;
import edu.univ.erp.domain.Instructor;

public class InstructorPanel extends JPanel {

    private static final Color BACKGROUND = new Color(255, 255, 255);
    private static final Color SURFACE = new Color(248, 249, 250);
    private static final Color BORDER = new Color(218, 220, 224);
    private static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    private static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    private static final Color ACCENT = new Color(26, 115, 232);
    private static final Color ACCENT_HOVER = new Color(23, 78, 166);
    private static final Color SIDEBAR_BG = new Color(241, 243, 244);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private UserSession userSession;
    private Instructor currentInstructor;

    public InstructorPanel(UserSession userSession, ActionListener logoutAction) {
        this.userSession = userSession;

        try {
            InstructorDAO instructorDAO = new InstructorDAO();
            this.currentInstructor = instructorDAO.getInstructorByUserId(userSession.getUserId());
        } catch (Exception e) {
            System.err.println("Error loading instructor data: " + e.getMessage());
            this.currentInstructor = null;
        }

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        add(createSidebar(logoutAction), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);

        contentPanel.add(createDashboardPanel(), "Dashboard");

        if (currentInstructor != null) {
            contentPanel.add(new GradebookPanel(currentInstructor), "Manage Grading");
            contentPanel.add(new StatisticsPanel(currentInstructor), "Class Statistics");
        } else {
            contentPanel.add(createErrorPanel("Failed to load instructor data"), "Manage Grading");
            contentPanel.add(createErrorPanel("Failed to load instructor data"), "Class Statistics");
        }

        contentPanel.add(createProfilePanel(), "My Profile");
        contentPanel.add(createSettingsPanel(), "Settings");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar(ActionListener logoutAction) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel logoLabel = new JLabel("University ERP", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        logoLabel.setForeground(TEXT_PRIMARY);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(SIDEBAR_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        String[] menuItems = {"Dashboard", "Manage Grading", "Class Statistics", "My Profile", "Settings"};

        for (String item : menuItems) {
            JButton btn = createSidebarButton(item);
            btn.addActionListener(e -> cardLayout.show(contentPanel, item));
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(3));
        }

        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setBackground(SIDEBAR_BG);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        JButton logoutBtn = createSidebarButton("Logout");
        logoutBtn.addActionListener(logoutAction);
        logoutPanel.add(logoutBtn, BorderLayout.NORTH);

        sidebar.add(logoLabel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(200, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(232, 234, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(SIDEBAR_BG);
            }
        });

        return btn;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new MigLayout(
                "insets 30 40 30 40, fill, gapx 40",
                "[grow, fill][300!]",
                "[][][grow, fill]"
        ));
        dashboardPanel.setBackground(BACKGROUND);

        JPanel welcomePanel = new JPanel(new MigLayout("wrap"));
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, Instructor!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        welcomeLabel.setForeground(TEXT_PRIMARY);

        JLabel subText = new JLabel("You are logged in as: Instructor");
        subText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subText.setForeground(TEXT_SECONDARY);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(subText);
        dashboardPanel.add(welcomePanel, "cell 0 0, aligny top");

        JPanel cardPanel = new JPanel(new MigLayout("wrap 2, fillx, gap 15", "[grow, fill]"));
        cardPanel.setOpaque(false);

        cardPanel.add(createInstructorStatCard("My Sections", "3"), "growx, h 100!");
        cardPanel.add(createInstructorStatCard("Total Students", "180"), "growx, h 100!");
        cardPanel.add(createInstructorStatCard("Pending Grades", "25"), "growx, h 100!");
        cardPanel.add(createInstructorStatCard("Avg. Attendance", "87%"), "growx, h 100!");

        dashboardPanel.add(cardPanel, "cell 0 1, gaptop 20, growx");

        JPanel quickActionsPanel = new JPanel(new MigLayout("fillx, insets 20, gap 15", "[grow][grow]"));
        quickActionsPanel.setBackground(SURFACE);
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel quickTitle = new JLabel("Quick Actions");
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        quickTitle.setForeground(TEXT_PRIMARY);
        quickActionsPanel.add(quickTitle, "span 2, wrap, gapbottom 10");

        JButton viewSectionsBtn = new JButton("View My Sections");
        viewSectionsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        viewSectionsBtn.setBackground(ACCENT);
        viewSectionsBtn.setForeground(Color.WHITE);
        viewSectionsBtn.setFocusPainted(false);
        viewSectionsBtn.setBorderPainted(false);
        viewSectionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewSectionsBtn.addActionListener(e -> cardLayout.show(contentPanel, "Manage Grading"));
        viewSectionsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewSectionsBtn.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewSectionsBtn.setBackground(ACCENT);
            }
        });

        JButton viewStatsBtn = new JButton("View Statistics");
        viewStatsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        viewStatsBtn.setBackground(ACCENT);
        viewStatsBtn.setForeground(Color.WHITE);
        viewStatsBtn.setFocusPainted(false);
        viewStatsBtn.setBorderPainted(false);
        viewStatsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewStatsBtn.addActionListener(e -> cardLayout.show(contentPanel, "Class Statistics"));
        viewStatsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewStatsBtn.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewStatsBtn.setBackground(ACCENT);
            }
        });

        quickActionsPanel.add(viewSectionsBtn, "growx, h 50!");
        quickActionsPanel.add(viewStatsBtn, "growx, h 50!");

        dashboardPanel.add(quickActionsPanel, "cell 0 2, gaptop 20, growx, growy");

        JPanel rightSidebar = new JPanel(new MigLayout("wrap, fillx, insets 0, gap 20"));
        rightSidebar.setOpaque(false);

        rightSidebar.add(createAnnouncementsPanel(), "h 140!, growx");
        rightSidebar.add(createUpcomingPanel(), "growx");

        dashboardPanel.add(rightSidebar, "cell 1 0, spany 3, aligny top");

        return dashboardPanel;
    }

    private JPanel createInstructorStatCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelLabel.setForeground(TEXT_SECONDARY);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(labelLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createAnnouncementsPanel() {
        JPanel announcementsPanel = new JPanel(new BorderLayout(0, 5));
        announcementsPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 3, 0, 0, ACCENT),
                new EmptyBorder(10, 15, 10, 15)
        ));
        announcementsPanel.setBackground(SURFACE);

        JLabel title = new JLabel("Announcements");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);

        JLabel announcement = new JLabel("<html>Semester registration opens from 5th Nov.</html>");
        announcement.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        announcement.setForeground(TEXT_SECONDARY);

        announcementsPanel.add(title, BorderLayout.NORTH);
        announcementsPanel.add(announcement, BorderLayout.CENTER);

        return announcementsPanel;
    }

    private JPanel createUpcomingPanel() {
        JPanel upcomingPanel = new JPanel(new BorderLayout(0, 5));
        upcomingPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 3, 0, 0, ACCENT),
                new EmptyBorder(10, 15, 10, 15)
        ));
        upcomingPanel.setBackground(SURFACE);

        JLabel title = new JLabel("Upcoming");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);

        JLabel upcoming = new JLabel("<html>Next Lecture: CS101<br/>Tomorrow 10:00 AM</html>");
        upcoming.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        upcoming.setForeground(TEXT_SECONDARY);

        upcomingPanel.add(title, BorderLayout.NORTH);
        upcomingPanel.add(upcoming, BorderLayout.CENTER);

        return upcomingPanel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, insets 40, fillx", "[][grow, fill]", "[]15[]"));
        panel.setBackground(BACKGROUND);

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, "span 2, wrap, gapbottom 20");

        JPanel topProfileSection = new JPanel(new MigLayout("insets 0", "[100!][grow, fill]", "[]0[]"));
        topProfileSection.setOpaque(false);

        JLabel profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(80, 80));
        profilePicLabel.setBorder(BorderFactory.createLineBorder(BORDER));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicLabel.setVerticalAlignment(SwingConstants.CENTER);
        try {
            URL imgUrl = getClass().getResource("/default_profile.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image image = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(image));
            } else {
                profilePicLabel.setText("No Image");
            }
        } catch (Exception e) {
            profilePicLabel.setText("No Image");
        }

        topProfileSection.add(profilePicLabel, "span 1 2, aligny top, gapright 15");

        JLabel fullNameDisplay = new JLabel("Instructor Name");
        fullNameDisplay.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fullNameDisplay.setForeground(TEXT_PRIMARY);
        topProfileSection.add(fullNameDisplay, "wrap");

        JLabel departmentDisplay = new JLabel("Department: Computer Science");
        departmentDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        departmentDisplay.setForeground(TEXT_SECONDARY);
        topProfileSection.add(departmentDisplay, "wrap");

        panel.add(topProfileSection, "span 2, growx, wrap, gaptop 10, gapbottom 30");

        JTextField emailField = new JTextField("instructor@example.com");
        JTextField contactField = new JTextField("+91 9876543210");

        JTextField[] editableFields = {emailField, contactField};

        panel.add(new JLabel("Email Address:"));
        panel.add(emailField, "growx, h 35!");
        panel.add(new JLabel("Contact No.:"));
        panel.add(contactField, "growx, h 35!");

        JButton editButton = new JButton("Edit Profile");
        editButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editButton.setBackground(ACCENT);
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editButton.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                editButton.setBackground(ACCENT);
            }
        });

        editButton.addActionListener(e -> {
            boolean isEditable = !emailField.isEditable();
            String buttonText = isEditable ? "Save Changes" : "Edit Profile";
            editButton.setText(buttonText);

            for (JTextField field : editableFields) {
                field.setEditable(isEditable);
                field.setBackground(isEditable ? BACKGROUND : SURFACE);
            }

            if (!isEditable) {
                System.out.println("Saving changes to profile...");
            }
        });

        for (JTextField field : editableFields) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            field.setEditable(false);
            field.setBackground(SURFACE);
        }

        panel.add(editButton, "span 2, gaptop 20, align right");

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap, insets 40, fillx", "[grow, fill]"));
        panel.setBackground(BACKGROUND);

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, "wrap, gapbottom 20");

        panel.add(createSettingsCard(
                        "Password and Security",
                        "Change or update your account password.",
                        createPasswordCardContent()),
                "wrap, gaptop 10, growx"
        );

        panel.add(createSettingsCard(
                        "Notifications",
                        "Manage your notification preferences.",
                        createNotificationsCardContent()),
                "wrap, gaptop 10, growx"
        );

        panel.add(createSettingsCard(
                        "Appearance",
                        "Customize the look and feel of the application.",
                        createAppearanceCardContent()),
                "wrap, gaptop 10, growx"
        );

        panel.add(createSettingsCard(
                        "Language",
                        "Choose your preferred language.",
                        createLanguageCardContent()),
                "wrap, gaptop 10, growx"
        );

        return panel;
    }

    private JPanel createSettingsCard(String cardTitle, String description, JPanel content) {
        JPanel cardPanel = new JPanel(new MigLayout("wrap, insets 15", "[grow, fill]", "[]0[]5[]"));
        cardPanel.setBackground(SURFACE);
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel titleLabel = new JLabel(cardTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        cardPanel.add(titleLabel, "growx");

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        cardPanel.add(descLabel, "growx, wrap");

        cardPanel.add(content, "growx, gaptop 5");

        return cardPanel;
    }

    private JPanel createPasswordCardContent() {
        JPanel content = new JPanel(new MigLayout("insets 0", "[grow, fill]", "[]"));
        content.setOpaque(false);
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePasswordButton.setBackground(ACCENT);
        changePasswordButton.setForeground(Color.WHITE);
        changePasswordButton.setFocusPainted(false);
        changePasswordButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        changePasswordButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                changePasswordButton.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                changePasswordButton.setBackground(ACCENT);
            }
        });
        changePasswordButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ChangePasswordDialog dialog = new ChangePasswordDialog(parentFrame, userSession);
            dialog.setVisible(true);
        });
        content.add(changePasswordButton, "align right");
        return content;
    }

    private JPanel createNotificationsCardContent() {
        JPanel content = new JPanel(new MigLayout("insets 0", "[grow, fill]", "[]"));
        content.setOpaque(false);
        JCheckBox muteNotifications = new JCheckBox("Mute all notifications");
        muteNotifications.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        muteNotifications.setOpaque(false);
        content.add(muteNotifications, "align left");
        return content;
    }

    private JPanel createAppearanceCardContent() {
        JPanel content = new JPanel(new MigLayout("insets 0", "[80!][grow, fill]", "[]"));
        content.setOpaque(false);
        content.add(new JLabel("Theme:"));
        JComboBox<String> themeComboBox = new JComboBox<>(new String[]{"Light Mode", "Dark Mode (coming soon)"});
        themeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.add(themeComboBox, "growx");
        return content;
    }

    private JPanel createLanguageCardContent() {
        JPanel content = new JPanel(new MigLayout("insets 0", "[80!][grow, fill]", "[]"));
        content.setOpaque(false);
        content.add(new JLabel("Language:"));
        JComboBox<String> langComboBox = new JComboBox<>(new String[]{"English (US)", "English (UK)", "Hindi (coming soon)"});
        langComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.add(langComboBox, "growx");
        return content;
    }

    private JPanel createErrorPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(220, 53, 69));
        panel.add(label);
        return panel;
    }
}