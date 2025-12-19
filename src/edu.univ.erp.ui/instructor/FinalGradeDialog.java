package edu.univ.erp.ui.instructor;

import edu.univ.erp.dao.*;
import edu.univ.erp.domain.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import edu.univ.erp.access.AccessControl;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class FinalGradeDialog extends JDialog {

    private int sectionId;
    
    private static final String[] GRADE_LETTERS = {"A+", "A", "A-", "B", "B-", "C", "C-", "D", "F"};
    private static final double[] GRADE_CGPA = {10.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 2.0};
    
    private Map<String, JTextField> thresholdFields = new LinkedHashMap<>();
    
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final StudentDAO studentDAO;
    private final AssessmentDAO assessmentDAO;

    public FinalGradeDialog(JFrame parent, int sectionId) {
        super(parent, "Publish Final Grades", true);
        this.sectionId = sectionId;
        
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.studentDAO = new StudentDAO();
        this.assessmentDAO = new AssessmentDAO(); 

        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, insets 20", "[grow, fill]"));
        mainPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Calculate Final Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(title, "align center, gapbottom 10");

        mainPanel.add(new JLabel("<html>Enter the minimum <b>Weighted Percentage</b> for each grade.<br>" +
                "The system will calculate: <i>(Score / MaxScore) * Weight</i> for every assessment.</html>"), "gapbottom 15");

        JPanel grid = new JPanel(new MigLayout("wrap 3", "[right][80!][left]"));
        grid.setBackground(Color.WHITE);
        
        JLabel header1 = new JLabel("Grade");
        header1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        grid.add(header1);

        JLabel header2 = new JLabel("Min %");
        header2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        grid.add(header2);

        JLabel header3 = new JLabel("CGPA");
        header3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        grid.add(header3, "wrap");

        grid.add(new JSeparator(), "span 3, growx, wrap");

        double[] defaults = {97, 93, 90, 87, 83, 80, 77, 70, 0};

        for (int i = 0; i < GRADE_LETTERS.length; i++) {
            String letter = GRADE_LETTERS[i];
            
            grid.add(new JLabel(letter));
            JTextField field = new JTextField(String.valueOf(defaults[i]));
            thresholdFields.put(letter, field);
            grid.add(field);
            grid.add(new JLabel(String.valueOf(GRADE_CGPA[i])));
        }
        mainPanel.add(grid, "align center");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        JButton calcBtn = new JButton("Calculate & Publish");
        calcBtn.setBackground(new Color(46, 125, 50));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.addActionListener(e -> calculateAndSave());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(calcBtn);
        btnPanel.add(cancelBtn);
        mainPanel.add(btnPanel);

        add(mainPanel);
    }

    private void calculateAndSave() {
        if (!AccessControl.isWriteAllowed("Instructor")) {
            JOptionPane.showMessageDialog(this,
                    "MAINTENANCE MODE ACTIVE\n\n" +
                            "Cannot publish grades during maintenance mode.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Map<String, Double> thresholds = new LinkedHashMap<>();
            for (String key : thresholdFields.keySet()) {
                thresholds.put(key, Double.parseDouble(thresholdFields.get(key).getText()));
            }

            List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
            List<Assessment> assessments = assessmentDAO.getAssessmentsForSection(sectionId);
            List<Grade> grades = gradeDAO.getGradesForSection(sectionId);

            if (enrollments.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students enrolled.");
                return;
            }

            Map<String, Double> gradeMap = new HashMap<>();
            for (Grade g : grades) {
                if (g.getScore() != null) {
                    gradeMap.put(g.getEnrollmentId() + "_" + g.getAssessmentId(),
                            g.getScore().doubleValue());
                }
            }

            StringBuilder log = new StringBuilder("Grading Summary:\n----------------\n");
            int count = 0;

            for (Enrollment enr : enrollments) {
                double totalWeighted = 0.0;

                for (Assessment asm : assessments) {
                    double max = asm.getMaxScore().doubleValue();
                    double weight = asm.getWeight().doubleValue();

                    String key = enr.getEnrollmentId() + "_" + asm.getAssessmentId();
                    if (gradeMap.containsKey(key)) {
                        double score = gradeMap.get(key);
                        totalWeighted += (score / max) * weight;
                    }
                }

                String letter = "F";
                for (String gradeKey : GRADE_LETTERS) {
                    if (totalWeighted >= thresholds.get(gradeKey)) {
                        letter = gradeKey;
                        break;
                    }
                }

                double cgpa = getCgpa(letter);
                enrollmentDAO.updateFinalGrade(enr.getEnrollmentId(), letter, cgpa);

                Student s = studentDAO.read(enr.getStudentId());
                String name = (s != null) ? s.getFullName() : "Unknown";
                log.append(String.format("%-20s : %.2f%% -> %s\n", name, totalWeighted, letter));
                count++;
            }

            JTextArea ta = new JTextArea(log.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                    "✅ Grades Published", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private double getCgpa(String grade) {
        for(int i=0; i<GRADE_LETTERS.length; i++) {
            if(GRADE_LETTERS[i].equals(grade)) return GRADE_CGPA[i];
        }
        return 0.0;
    }
}
