package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.*;
import edu.univ.erp.dao.SectionDAO;
import edu.univ.erp.dao.EnrollmentDAO;
import edu.univ.erp.dao.StudentDAO;
import edu.univ.erp.dao.AssessmentDAO;
import edu.univ.erp.dao.GradeDAO;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import edu.univ.erp.access.AccessControl;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.table.TableColumn;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GradebookPanel extends JPanel {

    private final Instructor currentInstructor;
    private Section currentSection;
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final StudentDAO studentDAO;
    private final AssessmentDAO assessmentDAO;
    private final GradeDAO gradeDAO;

    private JComboBox<Section> sectionComboBox;
    private JTable gradebookTable;
    private DefaultTableModel gradebookModel;
    private JButton addAssessmentButton;
    private JButton deleteAssessmentButton;
    private JButton saveButton;
    private JButton refreshButton;

    private List<Student> enrolledStudents = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();
    private List<Assessment> assessments = new ArrayList<>();
    final Map<String, Grade> gradeMap = new HashMap<>();
    private final Map<Integer, Assessment> columnToAssessmentMap = new HashMap<>();

    public GradebookPanel(Instructor instructor) {
        this.currentInstructor = instructor;
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentDAO = new StudentDAO();
        this.assessmentDAO = new AssessmentDAO();
        this.gradeDAO = new GradeDAO();

        initializeUI();
        loadSections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        add(createTopPanel(), BorderLayout.NORTH);
        add(createGradebookPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel instructorLabel = new JLabel(
                String.format("Instructor: %s | Department: %s",
                        currentInstructor.getFullName(),
                        currentInstructor.getDepartment())
        );
        instructorLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sectionPanel.setBackground(Color.WHITE);

        JLabel sectionLabel = new JLabel("Select Section:");
        sectionLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        sectionComboBox = new JComboBox<>();
        sectionComboBox.addActionListener(e -> onSectionChanged());

        sectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Section section) {
                    setText(section.toString());
                } else {
                    setText("No sections available");
                }
                return this;
            }
        });

        sectionPanel.add(sectionLabel);
        sectionPanel.add(sectionComboBox);

        panel.add(instructorLabel, BorderLayout.WEST);
        panel.add(sectionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGradebookPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)), "Gradebook",
                0, 0, new Font("Arial", Font.BOLD, 12), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);

        gradebookModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 2;
            }
        };

        gradebookModel.addColumn("Name");
        gradebookModel.addColumn("Roll No");
        gradebookModel.addColumn("Email");

        gradebookTable = new JTable(gradebookModel);

        gradebookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradebookTable.setRowHeight(25);
        gradebookTable.getTableHeader().setBackground(new Color(100, 150, 200));
        gradebookTable.getTableHeader().setForeground(Color.WHITE);
        gradebookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        gradebookTable.getTableHeader().setReorderingAllowed(true);

        JScrollPane scrollPane = new JScrollPane(gradebookTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);

        addAssessmentButton = new JButton("+ Add Assessment");
        addAssessmentButton.setBackground(new Color(0, 150, 200));
        addAssessmentButton.setForeground(Color.WHITE);
        addAssessmentButton.setFont(new Font("Arial", Font.BOLD, 11));
        addAssessmentButton.addActionListener(e -> onAddAssessment());

        deleteAssessmentButton = new JButton("- Delete Assessment");
        deleteAssessmentButton.setBackground(new Color(200, 100, 0));
        deleteAssessmentButton.setForeground(Color.WHITE);
        deleteAssessmentButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteAssessmentButton.addActionListener(e -> onDeleteAssessment());

        saveButton = new JButton("💾 Save Grades");
        saveButton.setBackground(new Color(50, 150, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 11));
        saveButton.addActionListener(e -> onSaveGrades());

        JButton finalGradesButton = new JButton("📊 Add Final Grades");
        finalGradesButton.setBackground(new Color(156, 39, 176));
        finalGradesButton.setForeground(Color.WHITE);
        finalGradesButton.setFont(new Font("Arial", Font.BOLD, 11));
        finalGradesButton.addActionListener(e -> onAddFinalGrades());

        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBackground(new Color(100, 100, 100));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 11));
        refreshButton.addActionListener(e -> onRefresh());

        JButton importButton = new JButton("📥 Import CSV");
        importButton.setBackground(new Color(76, 175, 80));
        importButton.setForeground(Color.WHITE);
        importButton.setFont(new Font("Arial", Font.BOLD, 11));
        importButton.addActionListener(e -> onImportFromCSV());

        JButton exportButton = new JButton("📤 Export Data");
        exportButton.setBackground(new Color(33, 150, 243));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("Arial", Font.BOLD, 11));
        exportButton.addActionListener(e -> onExportToCSV());

        setButtonsEnabled(false);

        panel.add(addAssessmentButton);
        panel.add(deleteAssessmentButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 20));
        }});
        panel.add(saveButton);
        panel.add(finalGradesButton);
        panel.add(refreshButton);
        panel.add(importButton);
        panel.add(exportButton);

        return panel;
    }

    private void setButtonsEnabled(boolean enabled) {
        addAssessmentButton.setEnabled(enabled);
        deleteAssessmentButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    private void loadSections() {
        try {
            List<Section> sections = sectionDAO.getSectionsByInstructor(currentInstructor.getUserId());
            sectionComboBox.removeAllItems();
            if (sections.isEmpty()) {
                sectionComboBox.addItem(new Section());
                sectionComboBox.setEnabled(false);
                setButtonsEnabled(false);
            } else {
                for (Section section : sections) {
                    sectionComboBox.addItem(section);
                }
                sectionComboBox.setEnabled(true);
                setButtonsEnabled(true);
            }
        } catch (SQLException e) {
            System.err.println("Error loading sections: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load sections: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSectionChanged() {
        Object selectedItem = sectionComboBox.getSelectedItem();
        if (selectedItem instanceof Section) {
            currentSection = (Section) selectedItem;
            if (currentSection.getSectionId() == 0) {
                gradebookModel.setRowCount(0);
                gradebookModel.setColumnCount(3);
                setButtonsEnabled(false);
                return;
            }
            setButtonsEnabled(true);
            loadGradebookData();
        }
    }

    private void loadGradebookData() {
        if (currentSection == null || currentSection.getSectionId() == 0) return;
        try {
            enrolledStudents = studentDAO.getEnrolledStudents(currentSection.getSectionId());
            enrollments = enrollmentDAO.getEnrollmentsBySection(currentSection.getSectionId());
            assessments = assessmentDAO.getAssessmentsForSection(currentSection.getSectionId());
            List<Grade> grades = gradeDAO.getGradesForSection(currentSection.getSectionId());
            gradeMap.clear();
            for (Grade grade : grades) {
                String key = grade.getEnrollmentId() + "_" + grade.getAssessmentId();
                gradeMap.put(key, grade);
            }
            Map<Integer, Integer> studentToEnrollmentMap = new HashMap<>();
            for (Enrollment e : enrollments) {
                studentToEnrollmentMap.put(e.getStudentId(), e.getEnrollmentId());
            }
            gradebookModel.setColumnCount(0);
            gradebookModel.addColumn("Name");
            gradebookModel.addColumn("Roll No");
            gradebookModel.addColumn("Email");
            System.out.println("Loading " + assessments.size() + " assessments:");
            columnToAssessmentMap.clear();
            int colIndex = 3;
            for (Assessment assessment : assessments) {
                System.out.println("  - " + assessment.getName() + " (ID: " + assessment.getAssessmentId() + ")");
                gradebookModel.addColumn(assessment.getName() + " (/" + assessment.getMaxScore() + ")");
                TableColumn column = gradebookTable.getColumnModel().getColumn(gradebookTable.getColumnCount() - 1);
                column.setIdentifier(assessment);
                columnToAssessmentMap.put(colIndex, assessment);
                colIndex++;
            }
            gradebookModel.setRowCount(0);
            for (Student student : enrolledStudents) {
                Integer enrollmentId = studentToEnrollmentMap.get(student.getUserId());
                if (enrollmentId == null) continue;
                Vector<Object> row = new Vector<>();
                row.add(student.getFullName());
                row.add(student.getRollNo());
                row.add(student.getEmail());
                for (Assessment assessment : assessments) {
                    String key = enrollmentId + "_" + assessment.getAssessmentId();
                    Grade grade = gradeMap.get(key);
                    if (grade != null) {
                        row.add(grade.getScore());
                    } else {
                        row.add(null);
                    }
                }
                gradebookModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load gradebook data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddAssessment() {
        if (!AccessControl.isWriteAllowed("Instructor")) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode.\nCannot add assessments.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentSection == null || currentSection.getSectionId() == 0) return;

        JTextField nameField = new JTextField();
        JTextField maxScoreField = new JTextField("100");
        JTextField weightField = new JTextField("0");

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]"));
        panel.add(new JLabel("Assessment Name:"));
        panel.add(nameField, "w 200!");
        panel.add(new JLabel("Max Score:"));
        panel.add(maxScoreField, "w 200!");
        panel.add(new JLabel("Weight (%):"));
        panel.add(weightField, "w 200!");

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Assessment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new Exception("Assessment name required.");

                BigDecimal maxScore = new BigDecimal(maxScoreField.getText());
                BigDecimal weight = new BigDecimal(weightField.getText());

                Assessment newAssessment = new Assessment(
                        currentSection.getSectionId(), name, maxScore, weight);

                assessmentDAO.create(newAssessment);
                loadGradebookData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteAssessment() {
        if (!AccessControl.isWriteAllowed("Instructor")) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode.\nCannot delete assessments.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (assessments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assessments to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] assessmentNames = assessments.stream()
                .map(Assessment::getName)
                .toArray(String[]::new);

        String toDelete = (String) JOptionPane.showInputDialog(this,
                "Select assessment to delete:",
                "Delete Assessment",
                JOptionPane.WARNING_MESSAGE,
                null, assessmentNames, assessmentNames[0]);

        if (toDelete != null) {
            Assessment selectedAssessment = null;
            for (Assessment a : assessments) {
                if (a.getName().equals(toDelete)) {
                    selectedAssessment = a;
                    break;
                }
            }

            if (selectedAssessment != null) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Delete '" + selectedAssessment.getName() + "'?\n" +
                                "This will delete all student scores.",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        assessmentDAO.delete(selectedAssessment.getAssessmentId());
                        loadGradebookData();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this,
                                "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void onSaveGrades() {
        if (!AccessControl.isWriteAllowed("Instructor")) {
            JOptionPane.showMessageDialog(this,
                    "MAINTENANCE MODE ACTIVE\n\n" +
                            "The system is currently in maintenance mode.\n" +
                            "Grade modifications are temporarily disabled.\n\n" +
                            "Please contact the administrator.",
                    "Maintenance Mode - Read Only",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gradebookTable.isEditing()) {
            gradebookTable.getCellEditor().stopCellEditing();
        }

        try {
            Map<Integer, Integer> studentToEnrollmentMap = new HashMap<>();
            for (Enrollment e : enrollments) {
                studentToEnrollmentMap.put(e.getStudentId(), e.getEnrollmentId());
            }

            int savedCount = 0;

            for (int row = 0; row < gradebookModel.getRowCount(); row++) {
                Student student = enrolledStudents.get(row);
                Integer enrollmentId = studentToEnrollmentMap.get(student.getUserId());

                if (enrollmentId == null) {
                    System.err.println("WARNING: Student " + student.getFullName() + " has no enrollment. Skipping.");
                    continue;
                }

                for (int col = 3; col < gradebookModel.getColumnCount(); col++) {
                    TableColumn tableColumn = gradebookTable.getColumnModel().getColumn(col);
                    Object identifier = tableColumn.getIdentifier();
                    Assessment assessment = null;

                    if (identifier instanceof Assessment) {
                        assessment = (Assessment) identifier;
                    }

                    if (assessment == null) {
                        assessment = columnToAssessmentMap.get(col);
                    }

                    if (assessment == null) {
                        System.err.println("ERROR: Could not identify Assessment for column " + col);
                        continue;
                    }

                    Object cellValue = gradebookModel.getValueAt(row, col);

                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                        try {
                            BigDecimal score = new BigDecimal(cellValue.toString().trim());

                            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(assessment.getMaxScore()) > 0) {
                                JOptionPane.showMessageDialog(this,
                                        "Invalid score for: " + student.getFullName() + "\n" +
                                                "Assessment: " + assessment.getName() + "\n" +
                                                "Score must be 0-" + assessment.getMaxScore(),
                                        "Invalid Score",
                                        JOptionPane.WARNING_MESSAGE);
                                gradebookTable.changeSelection(row, col, false, false);
                                return;
                            }

                            Grade grade = new Grade(enrollmentId, assessment.getAssessmentId(), score);
                            gradeDAO.saveOrUpdate(grade);
                            savedCount++;

                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Invalid number for: " + student.getFullName(),
                                    "Format Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }

            if (savedCount > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Saved " + savedCount + " grades successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadGradebookData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No changes to save.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRefresh() {
        loadGradebookData();
        JOptionPane.showMessageDialog(this,
                "Gradebook refreshed!",
                "Refresh",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAddFinalGrades() {
        if (currentSection == null || currentSection.getSectionId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section first",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        FinalGradeDialog dialog = new FinalGradeDialog(topFrame, currentSection.getSectionId());
        dialog.setVisible(true);
        loadGradebookData();
    }

    private void onImportFromCSV() {
        // Check maintenance mode
        if (!AccessControl.isWriteAllowed("Instructor")) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ MAINTENANCE MODE ACTIVE\n\n" +
                            "Cannot import grades during maintenance mode.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if section selected
        if (currentSection == null || currentSection.getSectionId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section first.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // File chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File to Import");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        try {
            // Read CSV file using BufferedReader (no external library needed)
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            List<String[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                // Simple CSV parsing
                String[] values = line.split(",");
                // Trim whitespace from each value
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim().replace("\"", ""); // Remove quotes if any
                }
                rows.add(values);
            }
            reader.close();

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "CSV file is empty.",
                        "Empty File",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // First row should be headers: Roll No, Assessment1, Assessment2, ...
            String[] headers = rows.get(0);

            if (headers.length < 2) {
                JOptionPane.showMessageDialog(this,
                        "Invalid CSV format.\n\n" +
                                "Expected format:\n" +
                                "Roll No, Assessment1, Assessment2, ...\n" +
                                "2021001, 85, 90, ...",
                        "Invalid Format",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirm before import
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Import grades from:\n" + selectedFile.getName() + "\n\n" +
                            "Found " + (rows.size() - 1) + " students\n" +
                            "Found " + (headers.length - 1) + " assessments\n\n" +
                            "This will overwrite existing grades.\n" +
                            "Continue?",
                    "Confirm Import",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Process import
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errors = new StringBuilder();

            // Build map of rollNo -> studentId
            Map<String, Integer> rollNoToStudentId = new HashMap<>();
            for (Student student : enrolledStudents) {
                rollNoToStudentId.put(student.getRollNo(), student.getUserId());
            }

            // Build map of assessment names to assessment IDs
            Map<String, Integer> assessmentNameToId = new HashMap<>();
            for (Assessment assessment : assessments) {
                assessmentNameToId.put(assessment.getName(), assessment.getAssessmentId());
            }

            // Build map of studentId -> enrollmentId
            Map<Integer, Integer> studentToEnrollmentMap = new HashMap<>();
            for (Enrollment enr : enrollments) {
                studentToEnrollmentMap.put(enr.getStudentId(), enr.getEnrollmentId());
            }

            // Process each row (skip header)
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                if (row.length < 2) continue;

                String rollNo = row[0].trim();
                Integer studentId = rollNoToStudentId.get(rollNo);

                if (studentId == null) {
                    errorCount++;
                    errors.append("Row ").append(i + 1).append(": Student ").append(rollNo).append(" not found\n");
                    continue;
                }

                // Get enrollment ID for this student
                Integer enrollmentId = studentToEnrollmentMap.get(studentId);

                if (enrollmentId == null) {
                    errorCount++;
                    errors.append("Row ").append(i + 1).append(": ").append(rollNo).append(" not enrolled\n");
                    continue;
                }

                // Import each assessment score
                for (int col = 1; col < row.length && col < headers.length; col++) {
                    String assessmentName = headers[col].trim();
                    String scoreStr = row[col].trim();

                    if (scoreStr.isEmpty()) continue;

                    Integer assessmentId = assessmentNameToId.get(assessmentName);
                    if (assessmentId == null) {
                        // Assessment doesn't exist - skip
                        continue;
                    }

                    try {
                        BigDecimal score = new BigDecimal(scoreStr);

                        // Save grade
                        Grade grade = new Grade();
                        grade.setEnrollmentId(enrollmentId);
                        grade.setAssessmentId(assessmentId);
                        grade.setScore(score);

                        gradeDAO.saveOrUpdate(grade);
                        successCount++;

                    } catch (NumberFormatException e) {
                        errorCount++;
                        errors.append("Row ").append(i + 1).append(", ").append(assessmentName)
                                .append(": Invalid number '").append(scoreStr).append("'\n");
                    }
                }
            }

            // Show results
            String message = "Import Complete!\n\n" +
                    "✅ Successfully imported: " + successCount + " grades\n" +
                    (errorCount > 0 ? "❌ Errors: " + errorCount + "\n\n" + errors.toString() : "");

            JOptionPane.showMessageDialog(this,
                    message,
                    "Import Results",
                    errorCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

            // Refresh the gradebook
            loadGradebookData();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error reading CSV file:\n" + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error during import:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportToCSV() {
        if (currentSection == null || currentSection.getSectionId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section first",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gradebookModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data to export",
                    "Empty Table",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Gradebook as CSV");
        String defaultFileName = String.format("Gradebook_Section_%d_%s.csv",
                currentSection.getSectionId(),
                new java.text.SimpleDateFormat("yyyyMMdd_HHmms").format(new java.util.Date()));
        fileChooser.setSelectedFile(new File(defaultFileName));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                StringBuilder header = new StringBuilder();
                for (int col = 0; col < gradebookModel.getColumnCount(); col++) {
                    if (col > 0) header.append(",");
                    header.append("\"").append(gradebookModel.getColumnName(col)).append("\"");
                }
                writer.println(header.toString());

                for (int row = 0; row < gradebookModel.getRowCount(); row++) {
                    StringBuilder line = new StringBuilder();
                    for (int col = 0; col < gradebookModel.getColumnCount(); col++) {
                        if (col > 0) line.append(",");
                        Object value = gradebookModel.getValueAt(row, col);
                        if (value != null) {
                            String cellValue = value.toString();
                            if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                                cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                            }
                            line.append(cellValue);
                        }
                    }
                    writer.println(line.toString());
                }

                JOptionPane.showMessageDialog(this,
                        "Gradebook data exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error exporting data: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
