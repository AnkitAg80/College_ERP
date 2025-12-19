package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.dao.*;
import edu.univ.erp.domain.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class MyCoursesPanel extends JPanel {

    private UserSession userSession;
    private Student currentStudent;
    private EnrollmentDAO enrollmentDAO;
    private SectionDAO sectionDAO;
    private CourseDAO courseDAO;

    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color CARD_BG = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color MANDATORY_COLOR = new Color(231, 76, 60);
    private static final Color ELECTIVE_COLOR = new Color(52, 152, 219);

    // Store all data for filtering
    private Map<Integer, List<Pair<Course, Enrollment>>> allSemesterData;
    private JPanel semestersContainer;
    private JTextField searchField;

    public MyCoursesPanel(UserSession userSession) {
        this.userSession = userSession;
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
        this.courseDAO = new CourseDAO();

        loadDataAndInitUI();
    }

    private void loadDataAndInitUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        try {
            // 1. Load Student
            StudentDAO studentDAO = new StudentDAO();
            this.currentStudent = studentDAO.read(userSession.getUserId());

            // Header with search
            add(createHeaderPanel(), BorderLayout.NORTH);

            // 2. Load ALL Enrollments, Sections, and Courses
            List<Enrollment> allEnrollments = enrollmentDAO.getEnrollmentsByStudent(userSession.getUserId());

            // Map to store organized data: Semester -> List of Pairs<Course, Enrollment>
            allSemesterData = new HashMap<>();

            for (Enrollment e : allEnrollments) {
                if (!"enrolled".equalsIgnoreCase(e.getStatus())) continue;

                Section section = sectionDAO.read(e.getSectionId());
                if (section == null) continue;

                Course course = courseDAO.read(section.getCourseId());
                if (course == null) continue;

                // Use the Section's semester to organize
                int sem = (section.getSemester() != null) ? section.getSemester() : 0;

                allSemesterData.computeIfAbsent(sem, k -> new ArrayList<>())
                        .add(new Pair<>(course, e));
            }

            // 3. Create UI Cards Container
            semestersContainer = new JPanel(new MigLayout("wrap, fillx, insets 0, gap 15", "[grow, fill]"));
            semestersContainer.setBackground(Color.WHITE);

            // Initial display - show all courses
            updateDisplay(null);

            JScrollPane scrollPane = new JScrollPane(semestersContainer);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException e) {
            e.printStackTrace();
            add(new JLabel("Error loading data: " + e.getMessage()));
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]"));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(HEADER_COLOR);
        headerPanel.add(titleLabel);

        if (currentStudent != null) {
            JLabel infoLabel = new JLabel(String.format("Current Semester: %d | Branch: %s",
                    currentStudent.getYear(), currentStudent.getBranch()));
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            infoLabel.setForeground(Color.GRAY);
            headerPanel.add(infoLabel, "wrap");
        }

        // Search Bar
        JPanel searchPanel = new JPanel(new MigLayout("fillx, insets 10 0 10 0", "[grow][]"));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        searchField.setToolTipText("Search by course code or title");

        // Add placeholder text
        searchField.setForeground(Color.GRAY);
        searchField.setText("Search courses...");
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search courses...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Search courses...");
                }
            }
        });

        // Real-time search as user types
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });

        JButton searchButton = new JButton("🔍 Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setPreferredSize(new Dimension(120, 35));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearButton.setBackground(Color.WHITE);
        clearButton.setForeground(Color.GRAY);
        clearButton.setBorder(new LineBorder(BORDER_COLOR, 1));
        clearButton.setPreferredSize(new Dimension(80, 35));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            searchField.setForeground(Color.GRAY);
            searchField.setText("Search courses...");
            updateDisplay(null);
        });

        searchPanel.add(searchField, "growx");
        searchPanel.add(searchButton, "gapleft 10");
        searchPanel.add(clearButton, "gapleft 5");

        headerPanel.add(searchPanel, "newline, span, growx, gaptop 15");

        return headerPanel;
    }

    private void performSearch() {
        String query = searchField.getText().trim();

        // Ignore placeholder text
        if (query.isEmpty() || query.equals("Search courses...")) {
            updateDisplay(null);
            return;
        }

        updateDisplay(query.toLowerCase());
    }

    private void updateDisplay(String searchQuery) {
        semestersContainer.removeAll();

        // Loop 1 to 8 Semesters
        for (int sem = 1; sem <= 8; sem++) {
            List<Pair<Course, Enrollment>> courses = allSemesterData.getOrDefault(sem, new ArrayList<>());

            // Filter courses if search query exists
            if (searchQuery != null && !searchQuery.isEmpty()) {
                List<Pair<Course, Enrollment>> filtered = new ArrayList<>();
                for (Pair<Course, Enrollment> pair : courses) {
                    Course c = pair.getKey();
                    if (c.getCode().toLowerCase().contains(searchQuery) ||
                            c.getTitle().toLowerCase().contains(searchQuery)) {
                        filtered.add(pair);
                    }
                }
                courses = filtered;
            }

            // Only add semester panel if it has courses (when searching) or always show all semesters
            if (searchQuery == null || !courses.isEmpty()) {
                semestersContainer.add(createSemesterPanel(sem, courses), "growx");
            }
        }

        semestersContainer.revalidate();
        semestersContainer.repaint();
    }

    private JPanel createSemesterPanel(int semester, List<Pair<Course, Enrollment>> courses) {
        JPanel panel = new JPanel(new MigLayout("wrap, fillx, insets 15", "[grow, fill]"));

        // Highlight current semester
        boolean isCurrent = (currentStudent != null && currentStudent.getYear() == semester);
        panel.setBackground(isCurrent ? new Color(230, 240, 255) : CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(isCurrent ? new Color(52, 152, 219) : BORDER_COLOR, isCurrent ? 2 : 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel semTitle = new JLabel("Semester " + semester);
        semTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        semTitle.setForeground(HEADER_COLOR);
        panel.add(semTitle, "wrap, gapbottom 10");

        if (courses.isEmpty()) {
            JLabel noCourses = new JLabel("No courses enrolled");
            noCourses.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noCourses.setForeground(Color.GRAY);
            panel.add(noCourses);
        } else {
            for (Pair<Course, Enrollment> pair : courses) {
                panel.add(createCourseCard(pair.getKey(), pair.getValue()), "growx, gapbottom 5");
            }

            // Calculate Total Credits
            int totalCredits = courses.stream().mapToInt(p -> p.getKey().getCredits()).sum();
            JLabel creditsLabel = new JLabel("Total Credits: " + totalCredits);
            creditsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            creditsLabel.setForeground(new Color(41, 128, 185));
            panel.add(creditsLabel, "gaptop 10");
        }
        return panel;
    }

    private JPanel createCourseCard(Course course, Enrollment enrollment) {
        JPanel card = new JPanel(new MigLayout("fillx, insets 10", "[][grow][]"));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(BORDER_COLOR, 1));

        // Color bar (Mandatory vs Elective) - defaults to Blue if not set
        JPanel colorBar = new JPanel();
        colorBar.setPreferredSize(new Dimension(4, 30));
        colorBar.setBackground(course.isMandatory() ? MANDATORY_COLOR : ELECTIVE_COLOR);
        card.add(colorBar, "spany 2, gapright 10");

        // Course Code
        JLabel codeLabel = new JLabel(course.getCode());
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        codeLabel.setForeground(HEADER_COLOR);
        card.add(codeLabel);

        // Credits
        JLabel creditsLabel = new JLabel(course.getCredits() + " CR");
        creditsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        creditsLabel.setForeground(Color.WHITE);
        creditsLabel.setOpaque(true);
        creditsLabel.setBackground(new Color(52, 152, 219));
        creditsLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        card.add(creditsLabel);

        // --- DISPLAY FINAL GRADE ---
        if (enrollment.getFinalGrade() != null && !enrollment.getFinalGrade().isEmpty()) {
            JLabel gradeLabel = new JLabel(enrollment.getFinalGrade());
            gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            gradeLabel.setForeground(Color.WHITE);
            gradeLabel.setOpaque(true);
            gradeLabel.setBackground(getGradeColor(enrollment.getFinalGrade()));
            gradeLabel.setBorder(new EmptyBorder(3, 10, 3, 10));
            card.add(gradeLabel);

            if (enrollment.getCgpa() != null) {
                JLabel cgpaLabel = new JLabel(String.format("GPA: %.1f", enrollment.getCgpa()));
                cgpaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                cgpaLabel.setForeground(Color.GRAY);
                card.add(cgpaLabel, "gapleft 5");
            }
        }

        card.add(new JLabel(), "wrap");

        // Title
        JLabel titleLabel = new JLabel(course.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, "skip 1, span");

        return card;
    }

    private Color getGradeColor(String grade) {
        if (grade == null) return Color.GRAY;
        switch (grade.toUpperCase()) {
            case "A+": case "A": return new Color(46, 125, 50); // Dark Green
            case "A-": case "B+": return new Color(102, 187, 106); // Light Green
            case "B": case "B-": return new Color(255, 193, 7); // Amber
            case "C+": case "C": case "C-": return new Color(255, 152, 0); // Orange
            case "F": return new Color(220, 53, 69); // Red
            default: return Color.GRAY;
        }
    }

    // Helper class for Pair
    private static class Pair<K, V> {
        private K key; private V value;
        public Pair(K key, V value) { this.key = key; this.value = value; }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}