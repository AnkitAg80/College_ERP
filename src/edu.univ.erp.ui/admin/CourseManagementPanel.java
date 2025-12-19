package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.SettingsService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManagementPanel extends JPanel {

    private AdminService adminService;
    private SettingsService settingsService;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;

    public CourseManagementPanel() {
        this.adminService = new AdminService();
        this.settingsService = new SettingsService();

        initializeUI();
        loadCourseData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)), "All Courses",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Course Code", "Title", "Credits", "Branches", "Semesters", "Sections"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseTable.getTableHeader().setBackground(new Color(200, 220, 255));
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        addButton = new JButton("➕ Add Course");
        addButton.setBackground(new Color(46, 125, 50));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> onAddCourse());

        editButton = new JButton("✏️ Edit Course");
        editButton.setBackground(new Color(0, 123, 255));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setPreferredSize(new Dimension(150, 40));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> onEditCourse());

        deleteButton = new JButton("🗑️ Delete Course");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> onDeleteCourse());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Add/Drop Deadline Section with clear format instructions
        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        deadlinePanel.setBackground(new Color(255, 248, 220)); // Light yellow
        deadlinePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
                        "⏰ Add/Drop Deadline",
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(180, 130, 0)
                ),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel deadlineLabel = new JLabel("Deadline:");
        deadlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

// Fetch current deadline from settings
        String currentDeadline = settingsService.getSetting("registration_deadline");
        if (currentDeadline == null || currentDeadline.isEmpty()) {
            currentDeadline = "2025-12-31"; // Default
        }

// Create a panel to hold the text field and format label
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        inputPanel.setOpaque(false);

        JTextField deadlineField = new JTextField(currentDeadline, 12);
        deadlineField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deadlineField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel formatLabel = new JLabel("(Format: YYYY-MM-DD)");
        formatLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        formatLabel.setForeground(Color.GRAY);

        inputPanel.add(deadlineField);
        inputPanel.add(formatLabel);

        JButton updateDeadlineButton = new JButton("Update Deadline");
        updateDeadlineButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateDeadlineButton.setBackground(new Color(255, 193, 7));
        updateDeadlineButton.setForeground(Color.WHITE);
        updateDeadlineButton.setFocusPainted(false);
        updateDeadlineButton.setBorderPainted(false);
        updateDeadlineButton.setPreferredSize(new Dimension(160, 35));
        updateDeadlineButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateDeadlineButton.addActionListener(e -> {
            String dateStr = deadlineField.getText().trim();

            // Validate format with clear error message
            try {
                java.time.LocalDate.parse(dateStr); // This validates YYYY-MM-DD format
                settingsService.setSetting("registration_deadline", dateStr);
                JOptionPane.showMessageDialog(this,
                        "✅ Add/Drop deadline updated to:\n" + dateStr +
                                "\n\nFormat: YYYY-MM-DD (Example: 2025-12-31)" +
                                "\n\nStudents cannot register or drop courses after this date.",
                        "Deadline Updated",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Invalid date format!\n\n" +
                                "Required format: YYYY-MM-DD\n\n" +
                                "Examples:\n" +
                                "• 2025-12-31 ✓\n" +
                                "• 2025-06-15 ✓\n" +
                                "• 12/31/2025 ✗ (wrong)\n" +
                                "• 31-12-2025 ✗ (wrong)\n\n" +
                                "Your input: " + dateStr,
                        "Invalid Date Format",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel infoLabel = new JLabel("<html><i>Students will be blocked from add/drop after this date</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);

        deadlinePanel.add(deadlineLabel);
        deadlinePanel.add(inputPanel); // Changed from deadlineField to inputPanel
        deadlinePanel.add(updateDeadlineButton);
        deadlinePanel.add(infoLabel);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(deadlinePanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCourseData() {
        tableModel.setRowCount(0);
        List<Course> courses = adminService.getAllCourses();

        for (Course course : courses) {
            String branches = course.getEligibleBranches().isEmpty() ? "All" :
                    String.join(", ", course.getEligibleBranches());

            String semesters = course.getEligibleSemesters().isEmpty() ? "N/A" :
                    course.getEligibleSemesters().toString().replaceAll("[\\[\\]]", "");

            int sectionCount = adminService.getSectionsForCourse(course.getCourseId()).size();

            tableModel.addRow(new Object[]{
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits(),
                    branches,
                    semesters,
                    sectionCount
            });
        }
    }

    private void onAddCourse() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CourseDialog dialog = new CourseDialog(parentFrame, adminService, null, this::loadCourseData);
        dialog.setVisible(true);
    }

    private void onEditCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Edit functionality will be implemented soon.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeleteCourse() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DeleteCourseDialog dialog = new DeleteCourseDialog(parentFrame, adminService, this::loadCourseData);
        dialog.setVisible(true);
    }
}