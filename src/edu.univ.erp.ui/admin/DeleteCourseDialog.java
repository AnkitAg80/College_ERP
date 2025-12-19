package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class DeleteCourseDialog extends JDialog {
    private AdminService adminService;
    private Runnable onDeleteSuccess;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);

    public DeleteCourseDialog(JFrame owner, AdminService adminService, Runnable onDeleteSuccess) {
        super(owner, "Delete Course", true);
        this.adminService = adminService;
        this.onDeleteSuccess = onDeleteSuccess;
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel headerLabel = new JLabel("Select a Course to Delete");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(HEADER_COLOR);
        JLabel warningLabel = new JLabel("<html><b>Warning:</b> This will permanently delete the course and all associated data:<br>• All sections taught by instructors<br>• All student enrollments<br>• All assessments and grades<br>This action cannot be undone!</html>");
        warningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warningLabel.setForeground(DANGER_COLOR);
        warningLabel.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 1, 0, DANGER_COLOR), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(warningLabel, BorderLayout.CENTER);
        JPanel searchPanel = new JPanel(new MigLayout("insets 10, fillx", "[grow][]"));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() { public void keyReleased(java.awt.event.KeyEvent evt) { filterTable(); } });
        searchPanel.add(searchLabel);
        searchPanel.add(searchField, "growx");
        String[] columnNames = {"Course ID", "Code", "Title", "Credits", "Branches", "Semesters"};
        tableModel = new DefaultTableModel(columnNames, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        courseTable = new JTable(tableModel);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        courseTable.setRowHeight(30);
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        courseTable.getTableHeader().setBackground(HEADER_COLOR);
        courseTable.getTableHeader().setForeground(Color.WHITE);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setShowGrid(true);
        courseTable.setGridColor(BORDER_COLOR);
        courseTable.getColumnModel().getColumn(0).setMinWidth(0);
        courseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        courseTable.getColumnModel().getColumn(0).setWidth(0);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        loadCourses();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.addActionListener(e -> dispose());
        JButton deleteButton = new JButton("Delete Course");
        deleteButton.setBackground(DANGER_COLOR);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteCourse());
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses = adminService.getAllCourses();
        for (Course course : courses) {
            String branches = course.getEligibleBranches().isEmpty() ? "All" :
                String.join(", ", course.getEligibleBranches());
            String semesters = course.getEligibleSemesters().isEmpty() ? "N/A" :
                course.getEligibleSemesters().toString();

            tableModel.addRow(new Object[]{
                course.getCourseId(),
                course.getCode(),
                course.getTitle(),
                course.getCredits(),
                branches,
                semesters
            });
        }
    }

    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        courseTable.setRowSorter(sorter);

        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a course to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = courseTable.convertRowIndexToModel(selectedRow);
        int courseId = (int) tableModel.getValueAt(modelRow, 0);
        String courseCode = (String) tableModel.getValueAt(modelRow, 1);
        String courseTitle = (String) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Are you absolutely sure you want to delete this course?</b><br><br>" +
            "Course: " + courseCode + " - " + courseTitle + "<br><br>" +
            "<font color='red'>This will permanently delete:</font><br>" +
            "• All sections of this course<br>" +
            "• All student enrollments<br>" +
            "• All assessments and grades<br>" +
            "• All instructor assignments<br><br>" +
            "<b>This action CANNOT be undone!</b></html>",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String typedText = JOptionPane.showInputDialog(this,
            "<html>To confirm, please type: <b>DELETE " + courseCode + "</b></html>",
            "Final Confirmation",
            JOptionPane.WARNING_MESSAGE);

        if (typedText == null || !typedText.equals("DELETE " + courseCode)) {
            JOptionPane.showMessageDialog(this, "Deletion cancelled. Text did not match.", "Cancelled", JOptionPane.INFORMATION_MESSAGE); return; }
        String result = adminService.deleteCourse(courseId);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
            if (onDeleteSuccess != null) onDeleteSuccess.run();
            dispose();
        } else JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
