package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;

public class StudentManagementPanel extends JPanel {
    private AdminService adminService;
    private JTable studentTable;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton statusButton;
    private DefaultTableModel tableModel;

    public StudentManagementPanel() {
        this.adminService = new AdminService();
        initializeUI();
        loadStudentData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(200, 200, 200)), "Students",
            0, 0, new Font("Arial", Font.BOLD, 12), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);
        String[] columns = {"User ID", "Name", "Roll No", "Branch", "Program", "Year", "CGPA", "Admission Year"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setBackground(new Color(200, 200, 255));
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);

        addButton = new JButton("➕ Add Student");
        addButton.setBackground(new Color(0, 150, 0));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 11));
        addButton.addActionListener(e -> onAddStudent());

        editButton = new JButton("✏️ Edit Student");
        editButton.setBackground(new Color(0, 100, 200));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Arial", Font.BOLD, 11));
        editButton.addActionListener(e -> onEditStudent());

        deleteButton = new JButton("🗑️ Delete Student");
        deleteButton.setBackground(new Color(200, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteButton.addActionListener(e -> onDeleteStudent());

        statusButton = new JButton("🔄 Change Status");
        statusButton.setBackground(new Color(200, 100, 0));
        statusButton.setForeground(Color.WHITE);
        statusButton.setFont(new Font("Arial", Font.BOLD, 11));
        statusButton.addActionListener(e -> onChangeStatus());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(statusButton);

        return panel;
    }

    private void loadStudentData() {
        // TODO: Fetch all students from StudentDAO
        // TODO: Populate studentTable with data
    }

    private void onAddStudent() {
        JOptionPane.showMessageDialog(this, "Add Student dialog would open here", "Add Student", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onEditStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        JOptionPane.showMessageDialog(this, "Edit Student dialog would open here", "Edit Student", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Student deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onChangeStatus() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String[] options = {"Active", "Inactive", "Suspended"};
        int choice = JOptionPane.showOptionDialog(this, "Select new status:", "Change Status", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice >= 0) JOptionPane.showMessageDialog(this, "Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}