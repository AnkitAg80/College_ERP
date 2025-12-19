package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Branch;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDialog extends JDialog {
    private AdminService adminService;
    private Runnable onSaveSuccess;
    private Course existingCourse;
    private JComboBox<String> codePrefixCombo;
    private JTextField codeNumberField;
    private JTextField titleField;
    private JSpinner creditsSpinner;
    private Map<String, JCheckBox> branchCheckBoxes;
    private Map<Integer, JCheckBox> semesterSelectionCheckBoxes;
    private Map<Integer, JCheckBox> semesterMandatoryCheckBoxes;
    private Map<Instructor, JCheckBox> instructorCheckBoxes;
    private Map<Instructor, JSpinner> instructorCapacitySpinners;
    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);

    public CourseDialog(JFrame owner, AdminService adminService, Course course, Runnable onSaveSuccess) {
        super(owner, (course == null ? "Add New Course" : "Edit Course"), true);
        this.adminService = adminService;
        this.existingCourse = course;
        this.onSaveSuccess = onSaveSuccess;
        setSize(750, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, insets 20, fillx, gapy 15", "[150!][grow, fill]"));
        formPanel.setBackground(Color.WHITE);
        JLabel detailsHeader = createHeaderLabel("Course Details");
        formPanel.add(detailsHeader, "span 2, growx, gapbottom 10");
        formPanel.add(createFieldLabel("Course Code:"));
        JPanel codePanel = new JPanel(new MigLayout("insets 0, fillx", "[120!][grow, fill]"));
        codePanel.setOpaque(false);
        String[] prefixes = {"CSE", "ECE", "MTH", "BIO", "DES", "SSH", "CSAM", "EVE", "CSD", "CSSS", "CSEC", "OTH"};
        codePrefixCombo = new JComboBox<>(prefixes);
        codeNumberField = new JTextField();
        codePanel.add(codePrefixCombo, "growx, h 35!");
        codePanel.add(codeNumberField, "growx, h 35!");
        formPanel.add(codePanel, "growx");
        formPanel.add(createFieldLabel("Course Title:"));
        titleField = new JTextField();
        formPanel.add(titleField, "growx, h 35!");
        formPanel.add(createFieldLabel("Credits:"));
        creditsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 8, 1));
        formPanel.add(creditsSpinner, "h 35!, w 80!");
        formPanel.add(new JSeparator(), "span 2, growx, gaptop 10");
        JLabel rulesHeader = createHeaderLabel("Eligibility Rules");
        formPanel.add(rulesHeader, "span 2, growx, gapbottom 10, gaptop 10");
        formPanel.add(createFieldLabel("Target Branches:"), "aligny top");
        JPanel branchPanel = new JPanel(new MigLayout("wrap 4, insets 0, fillx"));
        branchPanel.setOpaque(false);
        branchCheckBoxes = new HashMap<>();
        List<Branch> allBranches = adminService.getAllBranches();
        if (allBranches.isEmpty()) branchPanel.add(new JLabel("No branches found."));
        else for (Branch branch : allBranches) {
            JCheckBox cb = new JCheckBox(branch.getBranchCode());
            cb.setOpaque(false);
            branchCheckBoxes.put(branch.getBranchCode(), cb);
            branchPanel.add(cb);
        }
        formPanel.add(branchPanel, "growx");
        formPanel.add(createFieldLabel("Target Semesters:"), "aligny top, span 2, wrap, gaptop 10");
        JPanel semesterPanel = new JPanel(new MigLayout("wrap 4, insets 0, fill, gap 10", "[grow, fill]"));
        semesterPanel.setOpaque(false);
        semesterSelectionCheckBoxes = new HashMap<>();
        semesterMandatoryCheckBoxes = new HashMap<>();
        for (int i = 1; i <= 8; i++) {
            JPanel semBox = new JPanel(new MigLayout("insets 8, fillx", "[grow]"));
            semBox.setOpaque(false);
            semBox.setBorder(BorderFactory.createEtchedBorder());
            JCheckBox semSelectCb = new JCheckBox("Semester " + i);
            semSelectCb.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JCheckBox semMandatoryCb = new JCheckBox("Mandatory");
            semMandatoryCb.setEnabled(false);
            semSelectCb.addActionListener(e -> {
                semMandatoryCb.setEnabled(semSelectCb.isSelected());
                if (!semSelectCb.isSelected()) semMandatoryCb.setSelected(false);
            });
            semesterSelectionCheckBoxes.put(i, semSelectCb);
            semesterMandatoryCheckBoxes.put(i, semMandatoryCb);
            semBox.add(semSelectCb, "wrap");
            semBox.add(semMandatoryCb, "gapleft 20");
            semesterPanel.add(semBox, "growx");
        }
        formPanel.add(semesterPanel, "span 2, growx, wrap");
        formPanel.add(new JSeparator(), "span 2, growx, gaptop 10");
        JLabel staffHeader = createHeaderLabel("Staffing & Capacity");
        formPanel.add(staffHeader, "span 2, growx, gapbottom 10, gaptop 10");
        formPanel.add(createFieldLabel("Assign Professors:"), "aligny top");
        List<Instructor> allInstructors = adminService.getAllInstructors();
        JPanel profPanel = new JPanel(new MigLayout("wrap 1, insets 0, fillx"));
        profPanel.setOpaque(false);
        instructorCheckBoxes = new HashMap<>();
        instructorCapacitySpinners = new HashMap<>();
        JScrollPane profScrollPane = new JScrollPane(profPanel);
        profScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        profScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        if (allInstructors.isEmpty()) {
            profScrollPane.setViewportView(new JLabel("  No instructors found."));
        } else {
            for (Instructor instructor : allInstructors) {
                JPanel row = new JPanel(new MigLayout("insets 5, fillx", "[grow][][80!]"));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
                JCheckBox cb = new JCheckBox(instructor.getFullName() + " (" + instructor.getDepartment() + ")");
                cb.setOpaque(false);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JLabel capLabel = new JLabel("Cap:");
                capLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 500, 1));
                capSpinner.setEnabled(false);
                cb.addActionListener(e -> capSpinner.setEnabled(cb.isSelected()));
                instructorCheckBoxes.put(instructor, cb);
                instructorCapacitySpinners.put(instructor, capSpinner);
                row.add(cb, "growx");
                row.add(capLabel, "gapright 5");
                row.add(capSpinner, "h 30!");
                profPanel.add(row, "growx");
            }
        }
        formPanel.add(profScrollPane, "grow, h 200!");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save Course");
        saveButton.setBackground(new Color(39, 174, 96));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> saveCourse());
        JScrollPane mainScrollPane = new JScrollPane(formPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        if (existingCourse != null) populateFields();
    }

    private void populateFields() {
        String code = existingCourse.getCode();
        codePrefixCombo.setSelectedItem(code.replaceAll("\\d*$", ""));
        codeNumberField.setText(code.replaceAll("[^\\d]*$", ""));
        titleField.setText(existingCourse.getTitle());
        creditsSpinner.setValue(existingCourse.getCredits());

        for (String branchCode : existingCourse.getEligibleBranches()) {
            if (branchCheckBoxes.containsKey(branchCode)) branchCheckBoxes.get(branchCode).setSelected(true);
        }
        for (Integer semester : existingCourse.getEligibleSemesters()) {
            if (semesterSelectionCheckBoxes.containsKey(semester)) {
                semesterSelectionCheckBoxes.get(semester).setSelected(true);
                semesterMandatoryCheckBoxes.get(semester).setEnabled(true);
            }
        }
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(HEADER_COLOR);
        label.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(HEADER_COLOR);
        return label;
    }

    private void saveCourse() {
        String code = codePrefixCombo.getSelectedItem() + codeNumberField.getText();
        String title = titleField.getText();
        int credits = (int) creditsSpinner.getValue();
        if (code.trim().isEmpty() || title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course code and title are required.", "Missing Information", JOptionPane.WARNING_MESSAGE); return; }
        List<String> targetBranches = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : branchCheckBoxes.entrySet()) if (entry.getValue().isSelected()) targetBranches.add(entry.getKey());
        if (targetBranches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one branch.", "No Branch Selected", JOptionPane.WARNING_MESSAGE); return; }
        Map<Integer, Boolean> semesterEligibility = new HashMap<>();
        for (Map.Entry<Integer, JCheckBox> entry : semesterSelectionCheckBoxes.entrySet()) if (entry.getValue().isSelected()) { int semester = entry.getKey(); boolean isMandatory = semesterMandatoryCheckBoxes.get(semester).isSelected(); semesterEligibility.put(semester, isMandatory); }
        if (semesterEligibility.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one semester.", "No Semester Selected", JOptionPane.WARNING_MESSAGE); return; }
        Map<Instructor, Integer> selectedInstructors = new HashMap<>();
        for (Map.Entry<Instructor, JCheckBox> entry : instructorCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                Instructor inst = entry.getKey();
                int capacity = (int) instructorCapacitySpinners.get(inst).getValue();
                if (capacity < 10 || capacity > 800) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid capacity for " + inst.getFullName() + "\n\n" +
                                    "Capacity must be between 10 and 800.\n" +
                                    "Current value: " + capacity,
                            "Invalid Capacity",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                selectedInstructors.put(inst, capacity);
            }
        }
        if (selectedInstructors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please assign at least one instructor to this course.",
                    "No Instructor Assigned",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message;
        if (existingCourse == null) {
            message = adminService.createNewCourse(code, title, credits, targetBranches, semesterEligibility, selectedInstructors);
        } else {
            message = "Edit functionality to be implemented.";
        }

        if (message.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            onSaveSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }}
