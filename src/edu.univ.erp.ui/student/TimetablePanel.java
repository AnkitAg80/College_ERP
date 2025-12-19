package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.DatabaseConnection;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class TimetablePanel extends JPanel {

    private UserSession userSession;
    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color SELECTED_COLOR = new Color(52, 152, 219);
    private static final Color FAINT_COLOR = new Color(200, 200, 200);

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIME_SLOTS = {
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
            "16:00-17:00", "17:00-18:00"
    };

    private JCheckBox[][] checkboxes;
    private JLabel[][] courseLabels;

    public TimetablePanel(UserSession userSession) {
        this.userSession = userSession;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        loadTimetable();
    }

    private void loadTimetable() {
        JLabel titleLabel = new JLabel("My Personalized Timetable");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(HEADER_COLOR);

        JLabel subtitleLabel = new JLabel("✓ Check boxes to add courses to your schedule");
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

        Map<String, String> masterTimetable = loadMasterTimetable();

        Set<String> selectedSlots = loadStudentPreferences();

        checkboxes = new JCheckBox[TIME_SLOTS.length][DAYS.length];
        courseLabels = new JLabel[TIME_SLOTS.length][DAYS.length];

        for (int timeIdx = 0; timeIdx < TIME_SLOTS.length; timeIdx++) {
            JLabel timeLabel = new JLabel(TIME_SLOTS[timeIdx]);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setBorder(new LineBorder(Color.LIGHT_GRAY));
            timeLabel.setOpaque(true);
            timeLabel.setBackground(new Color(245, 245, 245));
            gridPanel.add(timeLabel);

            for (int dayIdx = 0; dayIdx < DAYS.length; dayIdx++) {
                String key = DAYS[dayIdx] + "_" + TIME_SLOTS[timeIdx];
                String courseCode = masterTimetable.get(key);

                JPanel cell = new JPanel(new MigLayout("wrap, insets 5, fill"));
                cell.setBackground(Color.WHITE);
                cell.setBorder(new LineBorder(Color.LIGHT_GRAY));

                if (courseCode != null && !courseCode.isEmpty()) {
                    JCheckBox checkbox = new JCheckBox();
                    checkbox.setOpaque(false);
                    checkbox.setSelected(selectedSlots.contains(key));

                    JLabel courseLabel = new JLabel(courseCode);
                    courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    updateCellAppearance(checkbox, courseLabel);

                    int finalTimeIdx = timeIdx;
                    int finalDayIdx = dayIdx;
                    checkbox.addActionListener(e -> {
                        updateCellAppearance(checkbox, courseLabel);
                        saveStudentPreference(DAYS[finalDayIdx], TIME_SLOTS[finalTimeIdx], checkbox.isSelected());
                    });

                    checkboxes[timeIdx][dayIdx] = checkbox;
                    courseLabels[timeIdx][dayIdx] = courseLabel;

                    cell.add(checkbox, "align right");
                    cell.add(courseLabel, "align center");
                } else {
                    cell.add(new JLabel(""));
                }

                gridPanel.add(cell, dayIdx == DAYS.length - 1 ? "wrap" : "");
            }
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateCellAppearance(JCheckBox checkbox, JLabel label) {
        if (checkbox.isSelected()) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(SELECTED_COLOR);
        } else {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(FAINT_COLOR);
        }
    }

    private Map<String, String> loadMasterTimetable() {
        Map<String, String> timetable = new HashMap<>();
        String sql = "SELECT day, time_slot, course_code FROM master_timetable";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String key = rs.getString("day") + "_" + rs.getString("time_slot");
                timetable.put(key, rs.getString("course_code"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading master timetable: " + e.getMessage());
        }

        return timetable;
    }

    private Set<String> loadStudentPreferences() {
        Set<String> selected = new HashSet<>();
        String sql = "SELECT day, time_slot FROM student_timetable_preferences WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userSession.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String key = rs.getString("day") + "_" + rs.getString("time_slot");
                selected.add(key);
            }
        } catch (SQLException e) {
            System.err.println("Preferences table doesn't exist yet: " + e.getMessage());
        }

        return selected;
    }

    private void saveStudentPreference(String day, String timeSlot, boolean selected) {
        try (Connection conn = DatabaseConnection.getInstance().getErpConnection()) {
            String createTable = "CREATE TABLE IF NOT EXISTS student_timetable_preferences (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id INT NOT NULL, " +
                    "day VARCHAR(20) NOT NULL, " +
                    "time_slot VARCHAR(20) NOT NULL, " +
                    "UNIQUE KEY unique_pref (student_id, day, time_slot))";
            conn.createStatement().execute(createTable);

            if (selected) {
                String sql = "INSERT IGNORE INTO student_timetable_preferences (student_id, day, time_slot) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userSession.getUserId());
                stmt.setString(2, day);
                stmt.setString(3, timeSlot);
                stmt.executeUpdate();
            } else {
                String sql = "DELETE FROM student_timetable_preferences WHERE student_id = ? AND day = ? AND time_slot = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userSession.getUserId());
                stmt.setString(2, day);
                stmt.setString(3, timeSlot);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}