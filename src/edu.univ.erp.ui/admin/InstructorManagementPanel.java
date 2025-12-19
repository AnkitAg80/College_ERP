package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;

public class InstructorManagementPanel extends JPanel {
    private AdminService adminService;
    private JTable instructorTable;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignButton;
    private DefaultTableModel tableModel;

    public InstructorManagementPanel() {
        this.adminService = new AdminService();

        initializeUI();
        loadInstructorData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Instructor Management");
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
            new LineBorder(new Color(200, 200, 200)), "Instructors",
            0, 0, new Font("Arial", Font.BOLD, 12), Color.BLACK
        ));
        panel.setBackground(Color.WHITE);

        String[] columns = {"User ID", "Name", "Email", "Department"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        instructorTable = new JTable(tableModel);
        instructorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instructorTable.setRowHeight(25);
        instructorTable.getTableHeader().setBackground(new Color(255, 200, 200));
        instructorTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(instructorTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);

        addButton = new JButton("➕ Add Instructor");
        addButton.setBackground(new Color(0, 150, 0));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 11));
        addButton.addActionListener(e -> onAddInstructor());

        editButton = new JButton("✏️ Edit Instructor");
        editButton.setBackground(new Color(0, 100, 200));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Arial", Font.BOLD, 11));
        editButton.addActionListener(e -> onEditInstructor());

        deleteButton = new JButton("🗑️ Delete Instructor");
        deleteButton.setBackground(new Color(200, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteButton.addActionListener(e -> onDeleteInstructor());

        assignButton = new JButton("📌 Assign Section");
        assignButton.setBackground(new Color(200, 150, 0));
        assignButton.setForeground(Color.WHITE);
        assignButton.setFont(new Font("Arial", Font.BOLD, 11));
        assignButton.addActionListener(e -> onAssignSection());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(assignButton);

        return panel;
    }

    private void loadInstructorData() {
    }

    private void onAddInstructor() {
        JOptionPane.showMessageDialog(this,
            "Add Instructor dialog would open here",
            "Add Instructor",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onEditInstructor() {
        int selectedRow = instructorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an instructor to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Edit Instructor dialog would open here",
            "Edit Instructor",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeleteInstructor() {
        int selectedRow = instructorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an instructor to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this instructor?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Instructor deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onAssignSection() {
        int selectedRow = instructorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an instructor.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Assign Section dialog would open here",
            "Assign Section",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
