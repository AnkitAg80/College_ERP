package edu.univ.erp.ui.admin;

import edu.univ.erp.dao.CourseDAO;
import edu.univ.erp.domain.Course;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import edu.univ.erp.data.DatabaseConnection;

public class TimetableEditorPanel extends JPanel {

    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIME_SLOTS = {
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
            "16:00-17:00", "17:00-18:00"
    };

    private JComboBox<String>[][] slotSelectors;
    private CourseDAO courseDAO;
    private List<Course> allCourses;

    public TimetableEditorPanel() {
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        ensureTableExists();

        initializeUI();
        loadExistingTimetable();
    }

    private void ensureTableExists() {
        String createTable = "CREATE TABLE IF NOT EXISTS master_timetable (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "day VARCHAR(20) NOT NULL, " +
                "time_slot VARCHAR(20) NOT NULL, " +
                "course_code VARCHAR(20), " +
                "UNIQUE KEY unique_slot (day, time_slot))";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
            System.out.println("master_timetable table ready");
        } catch (SQLException e) {
            System.err.println("Error creating master_timetable: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Database Error: Could not create timetable table.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        JLabel titleLabel = new JLabel("Master Timetable Editor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(HEADER_COLOR);

        JLabel subtitleLabel = new JLabel("Set the default course schedule visible to all students");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel(new MigLayout("wrap, insets 0"));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel, "gapbottom 20");

        add(headerPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new MigLayout("fill, insets 20",
                "[100!][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[40!][grow, fill]"));
        gridPanel.setBackground(Color.WHITE);

        JLabel cornerLabel = new JLabel("Time");
        cornerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cornerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cornerLabel.setOpaque(true);
        cornerLabel.setBackground(new Color(230, 230, 230));
        cornerLabel.setBorder(new LineBorder(Color.GRAY));
        gridPanel.add(cornerLabel);

        for (String day : DAYS) {
            JLabel dayLabel = new JLabel(day);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayLabel.setOpaque(true);
            dayLabel.setBackground(new Color(230, 230, 230));
            dayLabel.setBorder(new LineBorder(Color.GRAY));
            gridPanel.add(dayLabel, day.equals("Friday") ? "wrap" : "");
        }

        try {
            allCourses = courseDAO.getAllCourses();
            System.out.println("Loaded " + allCourses.size() + " courses");
        } catch (SQLException e) {
            System.err.println("Error loading courses: " + e.getMessage());
            e.printStackTrace();
            allCourses = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Could not load courses from database.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        String[] courseOptions = new String[allCourses.size() + 1];
        courseOptions[0] = "-- No Class --";
        for (int i = 0; i < allCourses.size(); i++) {
            Course c = allCourses.get(i);
            courseOptions[i + 1] = c.getCode() + " - " + c.getTitle();
        }

        slotSelectors = new JComboBox[TIME_SLOTS.length][DAYS.length];

        for (int timeIdx = 0; timeIdx < TIME_SLOTS.length; timeIdx++) {
            JLabel timeLabel = new JLabel(TIME_SLOTS[timeIdx]);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setBorder(new LineBorder(Color.LIGHT_GRAY));
            timeLabel.setOpaque(true);
            timeLabel.setBackground(new Color(245, 245, 245));
            gridPanel.add(timeLabel);

            for (int dayIdx = 0; dayIdx < DAYS.length; dayIdx++) {
                JComboBox<String> selector = new JComboBox<>(courseOptions);
                selector.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                slotSelectors[timeIdx][dayIdx] = selector;
                gridPanel.add(selector, dayIdx == DAYS.length - 1 ? "wrap" : "");
            }
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("💾 Save Timetable");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveTimetable());

        JButton clearButton = new JButton("🗑️ Clear All");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setBackground(new Color(220, 53, 69));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        clearButton.setPreferredSize(new Dimension(150, 40));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearTimetable());

        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadExistingTimetable() {
        String sql = "SELECT day, time_slot, course_code FROM master_timetable";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int loadedCount = 0;
            while (rs.next()) {
                String day = rs.getString("day");
                String timeSlot = rs.getString("time_slot");
                String courseCode = rs.getString("course_code");

                int dayIdx = Arrays.asList(DAYS).indexOf(day);
                int timeIdx = Arrays.asList(TIME_SLOTS).indexOf(timeSlot);

                if (dayIdx >= 0 && timeIdx >= 0) {
                    for (int i = 1; i < slotSelectors[timeIdx][dayIdx].getItemCount(); i++) {
                        String item = slotSelectors[timeIdx][dayIdx].getItemAt(i);
                        if (item.startsWith(courseCode)) {
                            slotSelectors[timeIdx][dayIdx].setSelectedIndex(i);
                            loadedCount++;
                            break;
                        }
                    }
                }
            }
            System.out.println("Loaded " + loadedCount + " timetable slots");
        } catch (SQLException e) {
            System.err.println("Could not load existing timetable: " + e.getMessage());
        }
    }

    private void saveTimetable() {
        try (Connection conn = DatabaseConnection.getInstance().getErpConnection()) {

            conn.createStatement().execute("DELETE FROM master_timetable");

            String insertSQL = "INSERT INTO master_timetable (day, time_slot, course_code) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            int savedCount = 0;
            for (int timeIdx = 0; timeIdx < TIME_SLOTS.length; timeIdx++) {
                for (int dayIdx = 0; dayIdx < DAYS.length; dayIdx++) {
                    String selected = (String) slotSelectors[timeIdx][dayIdx].getSelectedItem();

                    if (selected != null && !selected.startsWith("--")) {
                        String courseCode = selected.split(" - ")[0];

                        pstmt.setString(1, DAYS[dayIdx]);
                        pstmt.setString(2, TIME_SLOTS[timeIdx]);
                        pstmt.setString(3, courseCode);
                        pstmt.executeUpdate();
                        savedCount++;
                    }
                }
            }

            System.out.println("Saved " + savedCount + " timetable slots");

            JOptionPane.showMessageDialog(this,
                    "Timetable saved successfully!\n\n" +
                            savedCount + " time slots configured.\n" +
                            "Students can now view and customize their schedules.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving timetable:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearTimetable() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the entire timetable?\n\n" +
                        "This will reset all time slots to 'No Class'.\n" +
                        "This action cannot be undone.",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = 0; i < TIME_SLOTS.length; i++) {
                for (int j = 0; j < DAYS.length; j++) {
                    slotSelectors[i][j].setSelectedIndex(0);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Timetable cleared.\n\nClick 'Save Timetable' to apply changes to database.",
                    "Cleared",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}