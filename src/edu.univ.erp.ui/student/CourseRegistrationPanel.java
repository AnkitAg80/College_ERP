package edu.univ.erp.ui.student;

import edu.univ.erp.domain.*;
import edu.univ.erp.service.CourseRegistrationService;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.dao.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * CourseRegistrationPanel - Shows sections with instructors and real-time capacity
 */
public class CourseRegistrationPanel extends JPanel {

    private Student currentStudent;
    private CourseRegistrationService registrationService;
    private CourseDAO courseDAO;
    private SectionDAO sectionDAO;
    private EnrollmentDAO enrollmentDAO;
    private InstructorDAO instructorDAO;
    private AdminDAO adminDAO;

    private JTable availableSectionsTable;
    private JTable enrolledCoursesTable;
    private DefaultTableModel availableSectionsModel;
    private DefaultTableModel enrolledCoursesModel;
    private JLabel creditInfoLabel;
    private JLabel creditLimitLabel;
    private JButton registerButton;
    private JButton dropButton;

    private List<Course> enrolledCourses = new ArrayList<>();
    private List<Section> availableSections = new ArrayList<>();

    public CourseRegistrationPanel(UserSession userSession) {
        try {
            StudentDAO studentDAO = new StudentDAO();
            this.currentStudent = studentDAO.read(userSession.getUserId());

            if (this.currentStudent == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Could not load student data.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        this.registrationService = new CourseRegistrationService();
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.instructorDAO = new InstructorDAO();
        this.adminDAO = new AdminDAO();

        initializeUI();
        loadCourseData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel studentInfoLabel = new JLabel(
                String.format("Student: %s | Roll No: %s | Branch: %s | Sem: %d",
                        currentStudent.getFullName(),
                        currentStudent.getRollNo(),
                        currentStudent.getBranch(),
                        currentStudent.getYear())
        );
        studentInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel creditPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        creditPanel.setBackground(Color.WHITE);

        int maxCredits = registrationService.getMaxCreditLimit(currentStudent);
        creditLimitLabel = new JLabel(String.format("Credit Limit: %d", maxCredits));
        creditLimitLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        creditInfoLabel = new JLabel("Current Credits: 0");
        creditInfoLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        creditPanel.add(creditInfoLabel);
        creditPanel.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 20));
        }});
        creditPanel.add(creditLimitLabel);

        panel.add(studentInfoLabel, BorderLayout.WEST);
        panel.add(creditPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);

        panel.add(createAvailableSectionsPanel());
        panel.add(createEnrolledCoursesPanel());

        return panel;
    }

    private JPanel createAvailableSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)), "Available Sections",
                0, 0, new Font("Arial", Font.BOLD, 12), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Code", "Title", "Credits", "Instructor", "Seats (Avail/Total)", "Type"};
        availableSectionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        availableSectionsTable = new JTable(availableSectionsModel);
        availableSectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableSectionsTable.setRowHeight(25);
        availableSectionsTable.getTableHeader().setBackground(new Color(230, 230, 250));
        availableSectionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(availableSectionsTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnrolledCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)), "Registered Courses",
                0, 0, new Font("Arial", Font.BOLD, 12), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Code", "Title", "Credits", "Instructor"};
        enrolledCoursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enrolledCoursesTable = new JTable(enrolledCoursesModel);
        enrolledCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledCoursesTable.setRowHeight(25);
        enrolledCoursesTable.getTableHeader().setBackground(new Color(200, 255, 200));
        enrolledCoursesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(enrolledCoursesTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);

        registerButton = new JButton("Register Section");
        registerButton.setBackground(new Color(50, 150, 50));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 12));
        registerButton.setPreferredSize(new Dimension(150, 35));
        registerButton.addActionListener(e -> onRegisterSection());

        dropButton = new JButton("Drop Course");
        dropButton.setBackground(new Color(200, 50, 50));
        dropButton.setForeground(Color.WHITE);
        dropButton.setFont(new Font("Arial", Font.BOLD, 12));
        dropButton.setPreferredSize(new Dimension(150, 35));
        dropButton.addActionListener(e -> onDropCourse());

        panel.add(registerButton);
        panel.add(dropButton);

        return panel;
    }

    private void loadCourseData() {
        loadEnrolledCourses();
        loadAvailableSections();
        updateCreditInfo();
    }

    private void loadEnrolledCourses() {
        enrolledCoursesModel.setRowCount(0);
        enrolledCourses.clear();

        try {
            List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(currentStudent.getUserId());

            for (Enrollment enrollment : enrollments) {
                if (!"enrolled".equals(enrollment.getStatus())) {
                    continue;
                }

                Section section = sectionDAO.read(enrollment.getSectionId());
                if (section == null) continue;

                Course course = courseDAO.read(section.getCourseId());
                if (course == null) continue;

                enrolledCourses.add(course);

                String instructorName = "N/A";
                if (section.getInstructorId() != null) {
                    Instructor instructor = instructorDAO.read(section.getInstructorId());
                    instructorName = (instructor != null) ? instructor.getFullName() : "N/A";
                }

                enrolledCoursesModel.addRow(new Object[]{
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits(),
                        instructorName
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading enrolled courses: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAvailableSections() {
        availableSectionsModel.setRowCount(0);
        availableSections.clear();

        try {
            List<Section> allSections = sectionDAO.getAllSections();

            for (Section section : allSections) {
                boolean alreadyEnrolled = enrollmentDAO.isStudentEnrolledInSection(
                        currentStudent.getUserId(),
                        section.getSectionId()
                );

                if (alreadyEnrolled) {
                    continue;
                }

                Course course = courseDAO.read(section.getCourseId());
                if (course == null) continue;

                String instructorName = "N/A";
                if (section.getInstructorId() != null) {
                    Instructor instructor = instructorDAO.read(section.getInstructorId());
                    instructorName = (instructor != null) ? instructor.getFullName() : "N/A";
                }

                int currentCount = enrollmentDAO.getEnrollmentCountForSection(section.getSectionId());
                int capacity = section.getCapacity() != null ? section.getCapacity() : 60;
                String seatsInfo = String.format("%d/%d", currentCount, capacity);


                availableSections.add(section);

                availableSectionsModel.addRow(new Object[]{
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits(),
                        instructorName,
                        seatsInfo
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading available sections: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isAfterDeadline() {
        try {
            SettingsService settingsService = new SettingsService();
            return settingsService.isAfterRegistrationDeadline();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void onRegisterSection() {
        // Check maintenance mode
        try {
            if (adminDAO.isMaintenanceModeOn()) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ MAINTENANCE MODE ACTIVE\n\n" +
                                "The system is currently in maintenance mode.\n" +
                                "Course registration is temporarily disabled.\n\n" +
                                "Please try again later.",
                        "Maintenance Mode Active",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Check deadline
        if (isAfterDeadline()) {
            JOptionPane.showMessageDialog(this,
                    "⏰ REGISTRATION DEADLINE PASSED\n\n" +
                            "The add/drop deadline has passed.\n" +
                            "You can no longer register for courses.\n\n" +
                            "Please contact the administration office if you need assistance.",
                    "Deadline Passed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = availableSectionsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section to register.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Section targetSection = availableSections.get(selectedRow);
            Course selectedCourse = courseDAO.read(targetSection.getCourseId());

            String courseCode = (String) availableSectionsModel.getValueAt(selectedRow, 0);
            String courseTitle = (String) availableSectionsModel.getValueAt(selectedRow, 1);
            String instructorName = (String) availableSectionsModel.getValueAt(selectedRow, 3);
            int courseCredits = (int) availableSectionsModel.getValueAt(selectedRow, 2);

            // Check if student is already enrolled in this COURSE (any section)
            if (registrationService.isStudentEnrolledInCourse(currentStudent.getUserId(), selectedCourse.getCourseId())) {
                String existingProfessor = registrationService.getEnrolledSectionInfo(currentStudent.getUserId(), selectedCourse.getCourseId());
                JOptionPane.showMessageDialog(this,
                        "You are already enrolled in " + courseCode + " (" + courseTitle + ")\n" +
                                "Existing enrollment: " + existingProfessor + "\n\n" +
                                "You cannot register for the same course with a different professor.",
                        "Already Enrolled in This Course",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (enrollmentDAO.isStudentEnrolledInSection(currentStudent.getUserId(), targetSection.getSectionId())) {
                JOptionPane.showMessageDialog(this,
                        "You are already enrolled in this section!",
                        "Already Enrolled",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int currentCount = enrollmentDAO.getEnrollmentCountForSection(targetSection.getSectionId());
            int capacity = targetSection.getCapacity() != null ? targetSection.getCapacity() : 60;

            if (currentCount >= capacity) {
                JOptionPane.showMessageDialog(this,
                        "Sorry, this section is full!",
                        "Section Full",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int currentCredits = registrationService.calculateTotalCredits(enrolledCourses);
            int maxCredits = registrationService.getMaxCreditLimit(currentStudent);

            if (currentCredits + courseCredits > maxCredits) {
                JOptionPane.showMessageDialog(this,
                        String.format("Adding this section would exceed your credit limit!\nCurrent: %d, Limit: %d, Course: %d",
                                currentCredits, maxCredits, courseCredits),
                        "Credit Limit Exceeded",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Enrollment newEnrollment = new Enrollment();
            newEnrollment.setStudentId(currentStudent.getUserId());
            newEnrollment.setSectionId(targetSection.getSectionId());
            newEnrollment.setStatus("enrolled");

            enrollmentDAO.create(newEnrollment);

            System.out.println("✓ Enrollment created successfully!");

            loadCourseData();

            JOptionPane.showMessageDialog(this,
                    String.format("Successfully registered for:\n%s - %s\nInstructor: %s\n\nYour new total: %d/%d credits",
                            courseCode, courseTitle, instructorName, currentCredits + courseCredits, maxCredits),
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error during registration: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDropCourse() {
        // Check maintenance mode
        try {
            if (adminDAO.isMaintenanceModeOn()) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ MAINTENANCE MODE ACTIVE\n\n" +
                                "The system is currently in maintenance mode.\n" +
                                "Dropping courses is temporarily disabled.\n\n" +
                                "Please try again later.",
                        "Maintenance Mode Active",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Check deadline
        if (isAfterDeadline()) {
            JOptionPane.showMessageDialog(this,
                    "⏰ REGISTRATION DEADLINE PASSED\n\n" +
                            "The add/drop deadline has passed.\n" +
                            "You can no longer drop courses.\n\n" +
                            "Please contact the administration office if you need assistance.",
                    "Deadline Passed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = enrolledCoursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to drop.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String courseCode = (String) enrolledCoursesModel.getValueAt(selectedRow, 0);
            String courseTitle = (String) enrolledCoursesModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Are you sure you want to drop:\n%s - %s?", courseCode, courseTitle),
                    "Confirm Drop",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            Course courseToDrop = null;
            for (Course c : enrolledCourses) {
                if (c.getCode().equals(courseCode)) {
                    courseToDrop = c;
                    break;
                }
            }

            if (courseToDrop == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Course not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(currentStudent.getUserId());
            Enrollment enrollmentToDrop = null;

            for (Enrollment e : enrollments) {
                Section section = sectionDAO.read(e.getSectionId());
                if (section != null && section.getCourseId() == courseToDrop.getCourseId()) {
                    enrollmentToDrop = e;
                    break;
                }
            }

            if (enrollmentToDrop == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Enrollment record not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            enrollmentDAO.delete(enrollmentToDrop.getEnrollmentId());

            System.out.println("✓ Course dropped successfully!");

            loadCourseData();

            JOptionPane.showMessageDialog(this,
                    String.format("Successfully dropped %s - %s", courseCode, courseTitle),
                    "Drop Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error during drop: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCreditInfo() {
        int totalCredits = registrationService.calculateTotalCredits(enrolledCourses);
        int maxCredits = registrationService.getMaxCreditLimit(currentStudent);

        creditInfoLabel.setText(String.format("Current Credits: %d/%d", totalCredits, maxCredits));

        if (totalCredits >= maxCredits) {
            creditInfoLabel.setForeground(Color.RED);
            registerButton.setEnabled(false);
        } else {
            creditInfoLabel.setForeground(Color.BLACK);
            registerButton.setEnabled(true);
        }
    }
}
