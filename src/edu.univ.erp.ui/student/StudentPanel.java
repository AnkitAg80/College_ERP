package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.auth.LoginFrame;
import edu.univ.erp.auth.ChangePasswordDialog;
import edu.univ.erp.dao.StudentDAO;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Announcement;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.TranscriptGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.sql.SQLException;

public class StudentPanel extends JPanel {

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
    private StudentService studentService;

    public StudentPanel(UserSession userSession, ActionListener logoutAction) {
        this.userSession = userSession;
        this.studentService = new StudentService();
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        add(createSidebar(logoutAction), BorderLayout.WEST);
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(new MyCoursesPanel(userSession), "My Courses");
        contentPanel.add(new CourseRegistrationPanel(userSession), "Register for Courses");
        contentPanel.add(new TimetablePanel(userSession), "View Timetable");
        contentPanel.add(createAllAnnouncementsPanel(), "Announcements");
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
        String[] menuItems = {
                "Dashboard",
                "My Courses",
                "Register for Courses",
                "View Timetable",
                "My Profile",
                "Settings"
        };
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
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(new Color(232, 234, 237));
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(BACKGROUND);
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        topSection.add(titleLabel, BorderLayout.WEST);
        panel.add(topSection, BorderLayout.NORTH);
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(BACKGROUND);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BACKGROUND);
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(BACKGROUND);
        statsPanel.setMaximumSize(new Dimension(700, 250));
        statsPanel.setPreferredSize(new Dimension(700, 250));
        statsPanel.add(createStatCard("Courses Enrolled", "4"));
        statsPanel.add(createStatCard("Current CGPA", "8.5"));
        statsPanel.add(createStatCard("Attendance", "92%"));
        statsPanel.add(createStatCard("Credits", "18"));
        leftPanel.add(statsPanel);
        leftPanel.add(Box.createVerticalStrut(25));
        JPanel quickActionsPanel = createQuickActionsPanel();
        leftPanel.add(quickActionsPanel);
        mainContent.add(leftPanel, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BACKGROUND);
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.add(createAnnouncementsWidget());
        mainContent.add(rightPanel, BorderLayout.EAST);
        panel.add(mainContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelLabel.setForeground(TEXT_SECONDARY);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(labelLabel);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(700, 180));
        JLabel titleLabel = new JLabel("Quick Actions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonsPanel.setBackground(SURFACE);
        buttonsPanel.setMaximumSize(new Dimension(700, 50));
        JButton viewCoursesBtn = createActionButton("View My Courses");
        viewCoursesBtn.addActionListener(e -> cardLayout.show(contentPanel, "My Courses"));
        JButton registerBtn = createActionButton("Register Courses");
        registerBtn.addActionListener(e -> cardLayout.show(contentPanel, "Register for Courses"));
        JButton transcriptBtn = createActionButton("Download Transcript");
        transcriptBtn.addActionListener(e -> downloadTranscript());
        buttonsPanel.add(viewCoursesBtn);
        buttonsPanel.add(registerBtn);
        buttonsPanel.add(transcriptBtn);
        panel.add(buttonsPanel);
        return panel;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(ACCENT);
            }
        });
        return btn;
    }

    private JPanel createAnnouncementsWidget() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JLabel titleLabel = new JLabel("Announcements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        List<Announcement> announcements = studentService.getAllAnnouncements();
        if (!announcements.isEmpty()) {
            Announcement latest = announcements.get(0);
            String announcementText = latest.getTitle();
            if (announcementText.length() > 50) {
                announcementText = announcementText.substring(0, 50) + "...";
            }
            JLabel announcementLabel = new JLabel("<html>" + announcementText + "</html>");
            announcementLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            announcementLabel.setForeground(TEXT_SECONDARY);
            announcementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(announcementLabel);
        } else {
            JLabel noAnnouncementsLabel = new JLabel("No announcements");
            noAnnouncementsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            noAnnouncementsLabel.setForeground(TEXT_SECONDARY);
            noAnnouncementsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noAnnouncementsLabel);
        }
        panel.add(Box.createVerticalStrut(10));
        JLabel viewAllLabel = new JLabel("<html><u>View all announcements</u></html>");
        viewAllLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllLabel.setForeground(ACCENT);
        viewAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(contentPanel, "Announcements");
            }
        });
        panel.add(viewAllLabel);
        return panel;
    }

    private JPanel createAllAnnouncementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JLabel titleLabel = new JLabel("All Announcements");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        List<Announcement> announcements = studentService.getAllAnnouncements();
        if (announcements.isEmpty()) {
            JLabel noAnnouncementsLabel = new JLabel("No announcements available");
            noAnnouncementsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noAnnouncementsLabel.setForeground(TEXT_SECONDARY);
            noAnnouncementsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noAnnouncementsLabel);
        } else {
            for (Announcement announcement : announcements) {
                panel.add(createAnnouncementCard(announcement));
                panel.add(Box.createVerticalStrut(10));
            }
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAnnouncementCard(Announcement announcement) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(800, 150));
        JLabel titleLabel = new JLabel(announcement.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel metaLabel = new JLabel("By: " + announcement.getCreatedByName() + " | " + announcement.getCreatedAt());
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        metaLabel.setForeground(TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel contentLabel = new JLabel("<html>" + announcement.getMessage() + "</html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentLabel.setForeground(TEXT_PRIMARY);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(metaLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(contentLabel);
        return card;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(createProfileField("Name", "Student Name"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProfileField("Roll Number", "2023001"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProfileField("Email", "student@university.edu"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProfileField("Program", "B.Tech Computer Science"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProfileField("Branch", "Computer Science"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProfileField("Year", "1"));
        return panel;
    }

    private JPanel createProfileField(String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout(10, 5));
        fieldPanel.setBackground(BACKGROUND);
        fieldPanel.setMaximumSize(new Dimension(600, 70));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelLabel.setForeground(TEXT_PRIMARY);
        JTextField valueField = new JTextField(value);
        valueField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueField.setEditable(false);
        valueField.setBackground(SURFACE);
        valueField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        fieldPanel.add(labelLabel, BorderLayout.NORTH);
        fieldPanel.add(valueField, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        JPanel passwordSection = createSettingsSection("Password and Security", "Change your account password");
        JButton changePasswordBtn = createSettingsButton("Change Password");
        changePasswordBtn.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ChangePasswordDialog dialog = new ChangePasswordDialog(parentFrame, userSession);
            dialog.setVisible(true);
        });
        passwordSection.add(changePasswordBtn);
        panel.add(passwordSection);
        panel.add(Box.createVerticalStrut(15));
        JPanel notificationsSection = createSettingsSection("Notifications", "Manage notification preferences");
        JCheckBox muteNotifications = new JCheckBox("Mute all notifications");
        muteNotifications.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        muteNotifications.setBackground(SURFACE);
        notificationsSection.add(muteNotifications);
        panel.add(notificationsSection);
        return panel;
    }

    private JPanel createSettingsSection(String title, String description) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        section.setMaximumSize(new Dimension(700, 150));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(5));
        section.add(descLabel);
        section.add(Box.createVerticalStrut(15));
        return section;
    }

    private JButton createSettingsButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(ACCENT);
            }
        });
        return btn;
    }

    private void downloadTranscript() {
        try {
            StudentDAO studentDAO = new StudentDAO();
            Student currentStudent = studentDAO.read(userSession.getUserId());
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Could not load student data.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            File transcriptFile = TranscriptGenerator.generateCSVTranscript(currentStudent);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(transcriptFile);
                JOptionPane.showMessageDialog(this,
                        "Transcript generated successfully!\nSaved as: " + transcriptFile.getName(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Transcript saved as: " + transcriptFile.getAbsolutePath(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error generating transcript: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}