package edu.univ.erp.ui.admin;

import net.miginfocom.swing.MigLayout;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Branch;
import edu.univ.erp.domain.Announcement;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.univ.erp.auth.UserSession;

public class AdminPanel extends JPanel {
    private static final Color BACKGROUND = new Color(255, 255, 255);
    private static final Color SURFACE = new Color(248, 249, 250);
    private static final Color BORDER = new Color(218, 220, 224);
    private static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    private static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    private static final Color ACCENT = new Color(26, 115, 232);
    private static final Color ACCENT_HOVER = new Color(23, 78, 166);
    private static final Color SIDEBAR_BG = new Color(241, 243, 244);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private AdminService adminService;
    private UserSession userSession;
    private DefaultTableModel studentTableModel;
    private JTable studentTable;
    private DefaultTableModel instructorTableModel;
    private JTable instructorTable;
    private List<JList<Course>> semesterCourseLists = new ArrayList<>();
    private DefaultTableModel branchTableModel;
    private JTable branchTable;
    private DefaultTableModel announcementTableModel;
    private JTable announcementTable;

    public AdminPanel(UserSession userSession, ActionListener logoutAction) {
        this.userSession = userSession;
        this.adminService = new AdminService();
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        add(createSidebar(logoutAction), BorderLayout.WEST);
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createManageUsersPanel(), "Add User");
        contentPanel.add(createManageCoursesPanel(), "Manage Courses");
        contentPanel.add(createManageAnnouncementsPanel(), "Manage Announcements");
        contentPanel.add(new MaintenanceModePanel(), "Maintenance Mode");
        contentPanel.add(new BackupRestorePanel(), "Backup & Restore");
        contentPanel.add(new TimetableEditorPanel(), "TimeTable Editor");
        contentPanel.add(createProfilePanel(), "My Profile");
        add(contentPanel, BorderLayout.CENTER);
        refreshAllData();
    }

    private JPanel createSidebar(ActionListener logoutAction) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        JLabel logoLabel = new JLabel("University Admin", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(TEXT_PRIMARY);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(SIDEBAR_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        String[] buttons = {"Dashboard", "Add User", "Manage Courses", "Manage Announcements", "Maintenance Mode", "Backup & Restore", "TimeTable Editor", "My Profile"};
        for (String text : buttons) {
            JButton btn = createSidebarButton(text);
            btn.addActionListener(e -> cardLayout.show(contentPanel, text));
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(5));
        }
        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setBackground(SIDEBAR_BG);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        JButton logoutBtn = createSidebarButton("Logout");
        logoutBtn.setForeground(DANGER);
        logoutBtn.addActionListener(logoutAction);
        logoutPanel.add(logoutBtn, BorderLayout.NORTH);
        sidebar.add(logoLabel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(new Color(232, 234, 237)); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(SIDEBAR_BG); }
        });
        return btn;
    }

    public static class DashboardStats {
        public final int studentCount;
        public final int instructorCount;
        public final int courseCount;
        public final int branchCount;
        public DashboardStats(int students, int instructors, int courses, int branches) {
            this.studentCount = students;
            this.instructorCount = instructors;
            this.courseCount = courses;
            this.branchCount = branches;
        }
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new MigLayout("insets 30 40 30 40, fillx", "[grow]", "[]20[]20[]20[]"));
        dashboardPanel.setBackground(BACKGROUND);
        JLabel welcomeLabel = new JLabel("Admin Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        dashboardPanel.add(welcomeLabel, "wrap");
        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardPanel.setBackground(BACKGROUND);
        DashboardStats stats = adminService.getDashboardStats();
        cardPanel.add(createStatCard("Students", String.valueOf(stats.studentCount), ACCENT));
        cardPanel.add(createStatCard("Instructors", String.valueOf(stats.instructorCount), SUCCESS));
        cardPanel.add(createStatCard("Courses", String.valueOf(stats.courseCount), new Color(255, 193, 7)));
        cardPanel.add(createStatCard("Branches", String.valueOf(stats.branchCount), new Color(155, 89, 182)));
        dashboardPanel.add(cardPanel, "growx, h 120!, wrap");
        JLabel qaTitle = new JLabel("Quick Actions");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        qaTitle.setForeground(TEXT_PRIMARY);
        dashboardPanel.add(qaTitle, "wrap, gapbottom 10");
        JPanel actionPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        actionPanel.setBackground(BACKGROUND);
        actionPanel.add(createQuickActionButton("Add Student", "Add User"));
        actionPanel.add(createQuickActionButton("Add Course", "Manage Courses"));
        actionPanel.add(createQuickActionButton("Announcements", "Manage Announcements"));
        actionPanel.add(createQuickActionButton("Timetable", "TimeTable Editor"));
        dashboardPanel.add(actionPanel, "growx, h 50!, wrap");
        JPanel systemPanel = new JPanel(new MigLayout("fillx, insets 20", "[]30[]30[]", "[]"));
        systemPanel.setBackground(SURFACE);
        systemPanel.setBorder(new LineBorder(BORDER));
        JLabel statusTitle = new JLabel("System Status");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        systemPanel.add(statusTitle, "wrap, span, gapbottom 10");
        systemPanel.add(createStatusIndicator("Database Connection", true));
        systemPanel.add(createStatusIndicator("Server Status", true));
        systemPanel.add(createStatusIndicator("Security Protocols", true));
        dashboardPanel.add(systemPanel, "growx");
        return dashboardPanel;
    }

    private JPanel createStatCard(String title, String value, Color iconColor) {
        JPanel card = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][]"));
        card.setBackground(SURFACE);
        card.setBorder(new LineBorder(BORDER));
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valLabel.setForeground(TEXT_PRIMARY);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        JPanel bar = new JPanel();
        bar.setBackground(iconColor);
        bar.setPreferredSize(new Dimension(4, 40));
        card.add(valLabel, "split 2");
        card.add(bar, "dock west, gapright 15");
        card.add(titleLabel, "newline");
        return card;
    }

    private JButton createQuickActionButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if(text.equals("Add Course")) {
            btn.addActionListener(e -> {
                CourseDialog dialog = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), adminService, null, this::refreshAllData);
                dialog.setVisible(true);
            });
        } else {
            btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        }
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private JLabel createStatusIndicator(String label, boolean active) {
        JLabel l = new JLabel("\u25cf " + label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(active ? SUCCESS : DANGER);
        return l;
    }

    private JPanel createManageUsersPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 30 40 30 40, fill", "[grow]", "[][grow]"));
        panel.setBackground(BACKGROUND);
        JLabel title = new JLabel("Manage Users");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, "wrap, gapbottom 20");
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(BACKGROUND);
        tabbedPane.addTab("Students", createStudentListPanel());
        tabbedPane.addTab("Instructors", createInstructorListPanel());
        panel.add(tabbedPane, "grow");
        return panel;
    }

    private JPanel createStudentListPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow]"));
        p.setBackground(BACKGROUND);
        JButton addBtn = createActionButton("+ Add New Student");
        addBtn.addActionListener(e -> openCreateStudentDialog());
        p.add(addBtn, "wrap, gapbottom 15");
        String[] cols = {"ID", "Name", "Roll No", "Program", "Branch", "Sem"};
        studentTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        studentTable = createStyledTable(studentTableModel);
        studentTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = studentTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        studentTable.setRowSelectionInterval(row, row);
                        int id = (int) studentTableModel.getValueAt(row, 0);
                        String name = (String) studentTableModel.getValueAt(row, 1);
                        showUserContextMenu(e, "Student", id, name, () -> refreshStudentTable());
                    }
                }
            }
        });
        p.add(new JScrollPane(studentTable), "grow");
        return p;
    }

    private JPanel createInstructorListPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow]"));
        p.setBackground(BACKGROUND);
        JButton addBtn = createActionButton("+ Add New Instructor");
        addBtn.addActionListener(e -> openCreateInstructorDialog());
        p.add(addBtn, "wrap, gapbottom 15");
        String[] cols = {"ID", "Name", "Email", "Department"};
        instructorTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        instructorTable = createStyledTable(instructorTableModel);
        instructorTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = instructorTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        instructorTable.setRowSelectionInterval(row, row);
                        int id = (int) instructorTableModel.getValueAt(row, 0);
                        String name = (String) instructorTableModel.getValueAt(row, 1);
                        showUserContextMenu(e, "Instructor", id, name, () -> refreshInstructorTable());
                    }
                }
            }
        });
        p.add(new JScrollPane(instructorTable), "grow");
        return p;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(SURFACE);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return table;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private void showUserContextMenu(MouseEvent e, String role, int userId, String name, Runnable refreshCallback) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem delete = new JMenuItem("Delete " + role);
        delete.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        delete.setForeground(DANGER);
        delete.addActionListener(ae -> {
            int ch = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ch == JOptionPane.YES_OPTION) {
                String msg = role.equals("Student") ? adminService.deleteStudent(userId) : adminService.deleteInstructor(userId);
                if (msg.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this, msg);
                    refreshCallback.run();
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(delete);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 30 40 30 40, fill", "[grow]", "[][grow]"));
        panel.setBackground(BACKGROUND);
        JLabel title = new JLabel("Manage Courses");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, "wrap, gapbottom 20");
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Courses by Semester", createCoursesBySemesterPanel());
        tabbedPane.addTab("Manage Branches", createManageBranchesPanel());
        tabbedPane.addTab("Registration Settings", createRegistrationSettingsPanel());
        panel.add(tabbedPane, "grow");
        return panel;
    }

    private JPanel createRegistrationSettingsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap, insets 20, fillx", "[grow, fill]"));
        panel.setBackground(BACKGROUND);
        JLabel header = new JLabel("Registration Settings");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(header, "wrap, gapbottom 10");
        JPanel card = new JPanel(new MigLayout("fillx, insets 20", "[300!][250!][]"));
        card.setBackground(SURFACE);
        card.setBorder(new LineBorder(BORDER));
        JLabel lbl = new JLabel("Add/Drop Deadline: Format(YYYY-MM-DD) ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        SettingsService ss = new SettingsService();
        ss.loadAllSettings();
        String current = ss.getRegistrationDeadline();
        JTextField dateField = new JTextField(current != null ? current : "2025-12-31");
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton save = new JButton("Save Date");
        save.setBackground(SUCCESS);
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> {
            try {
                java.time.LocalDate.parse(dateField.getText());
                ss.setSetting("registration_deadline", dateField.getText());
                JOptionPane.showMessageDialog(panel, "Saved!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Format (YYYY-MM-DD)");
            }
        });
        card.add(lbl);
        card.add(dateField, "growx");
        card.add(save);
        panel.add(card, "growx");
        return panel;
    }

    private JPanel createCoursesBySemesterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow]"));
        panel.setBackground(BACKGROUND);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setBackground(BACKGROUND);
        JButton addBtn = createActionButton("+ Add New Course");
        addBtn.addActionListener(e -> {
            CourseDialog d = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), adminService, null, this::refreshAllData);
            d.setVisible(true);
        });
        btnPanel.add(addBtn);
        JButton delBtn = new JButton("Delete Course");
        delBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        delBtn.setForeground(DANGER);
        delBtn.setContentAreaFilled(false);
        delBtn.setBorder(new EmptyBorder(0, 20, 0, 0));
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            DeleteCourseDialog d = new DeleteCourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), adminService, this::refreshAllData);
            d.setVisible(true);
        });
        btnPanel.add(delBtn);
        panel.add(btnPanel, "wrap, gapbottom 15");
        JPanel grid = new JPanel(new MigLayout("wrap 4, fill, insets 0, gap 15", "[grow, fill]", "[grow, fill]"));
        grid.setOpaque(false);
        semesterCourseLists.clear();
        for (int i = 1; i <= 8; i++) grid.add(createSemesterCard(i), "grow");
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, "grow");
        return panel;
    }

    private JPanel createSemesterCard(int semester) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(SURFACE);
        card.setBorder(new LineBorder(BORDER));
        JLabel title = new JLabel("Semester " + semester, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);
        DefaultListModel<Course> model = new DefaultListModel<>();
        JList<Course> list = new JList<>(model);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setBackground(SURFACE);
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course c = (Course) value;
                    if (c.getCourseId() == 0) setText(c.getTitle());
                    else {
                        String color = c.isMandatory() ? "#C0392B" : "#2C3E50";
                        setText("<html><font color='" + color + "'><b>" + c.getCode() + ":</b> " + c.getTitle() + "</font></html>");
                    }
                }
                setOpaque(true);
                setBackground(isSelected ? new Color(232, 240, 254) : SURFACE);
                return this;
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(index);
                    Course c = list.getSelectedValue();
                    if (c != null && c.getCourseId() != 0) showCourseContextMenu(e, c, semester);
                }
            }
        });
        semesterCourseLists.add(list);
        card.add(new JScrollPane(list), BorderLayout.CENTER);
        return card;
    }

    private void showCourseContextMenu(MouseEvent e, Course c, int sem) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem remove = new JMenuItem("Remove from Sem " + sem);
        remove.setForeground(DANGER);
        remove.addActionListener(ae -> {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Remove course?")) {
                adminService.removeCourseFromSemester(c.getCourseId(), sem);
                refreshAllSemesterCourseLists();
            }
        });
        m.add(remove);
        m.show(e.getComponent(), e.getX(), e.getY());
    }

    private JPanel createManageBranchesPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow]"));
        p.setBackground(BACKGROUND);
        JPanel form = new JPanel(new MigLayout("insets 0, fillx, gap 10", "[][grow][][grow][]"));
        form.setBackground(BACKGROUND);
        JTextField codeF = new JTextField();
        JTextField nameF = new JTextField();
        JButton add = createActionButton("Add Branch");
        form.add(new JLabel("Code:")); form.add(codeF, "w 100!");
        form.add(new JLabel("Name:")); form.add(nameF, "growx");
        form.add(add, "h 35!");
        p.add(form, "growx, wrap, gapbottom 15");
        String[] cols = {"ID", "Code", "Name"};
        branchTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        branchTable = createStyledTable(branchTableModel);
        branchTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = branchTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        branchTable.setRowSelectionInterval(row, row);
                        int id = (int) branchTableModel.getValueAt(row, 0);
                        String code = (String) branchTableModel.getValueAt(row, 1);
                        showBranchContextMenu(e, id, code);
                    }
                }
            }
        });
        add.addActionListener(e -> {
            String msg = adminService.createNewBranch(codeF.getText(), nameF.getText());
            if (msg.startsWith("Success")) { refreshAllData(); codeF.setText(""); nameF.setText(""); }
            else JOptionPane.showMessageDialog(p, msg);
        });
        p.add(new JScrollPane(branchTable), "grow");
        return p;
    }

    private void showBranchContextMenu(MouseEvent e, int id, String code) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem del = new JMenuItem("Delete " + code);
        del.setForeground(DANGER);
        del.addActionListener(ae -> {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Delete Branch?")) {
                String msg = adminService.deleteBranch(id);
                if (msg.startsWith("Success")) refreshAllData();
                else JOptionPane.showMessageDialog(this, msg);
            }
        });
        m.add(del);
        m.show(e.getComponent(), e.getX(), e.getY());
    }

    private JPanel createManageAnnouncementsPanel() {
        JPanel p = new JPanel(new MigLayout("insets 30 40 30 40, fill", "[grow]", "[][grow]"));
        p.setBackground(BACKGROUND);
        JLabel t = new JLabel("Announcements");
        t.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        p.add(t, "wrap, gapbottom 20");
        JButton create = createActionButton("+ Create New");
        create.addActionListener(e -> {
            new AnnouncementDialog((JFrame) SwingUtilities.getWindowAncestor(this), null).setVisible(true);
            try { refreshAnnouncementsTable(announcementTableModel); } catch (SQLException ex) {}
        });
        p.add(create, "wrap, gapbottom 15");
        String[] cols = {"ID", "Title", "Message", "By", "Date"};
        announcementTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        announcementTable = createStyledTable(announcementTableModel);
        announcementTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = announcementTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        announcementTable.setRowSelectionInterval(row, row);
                        int id = (int) announcementTableModel.getValueAt(row, 0);
                        String title = (String) announcementTableModel.getValueAt(row, 1);
                        showAnnouncementContextMenu(e, id, title);
                    }
                }
            }
        });
        p.add(new JScrollPane(announcementTable), "grow");
        return p;
    }

    private void showAnnouncementContextMenu(MouseEvent e, int id, String title) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Edit");
        edit.addActionListener(ae -> {
            Announcement a = adminService.getAnnouncementById(id);
            new AnnouncementDialog((JFrame) SwingUtilities.getWindowAncestor(this), a).setVisible(true);
            try { refreshAnnouncementsTable(announcementTableModel); } catch (Exception x) {}
        });
        JMenuItem del = new JMenuItem("Delete");
        del.setForeground(DANGER);
        del.addActionListener(ae -> {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Delete " + title + "?")) {
                adminService.deleteAnnouncement(id);
                try { refreshAnnouncementsTable(announcementTableModel); } catch (Exception x) {}
            }
        });
        m.add(edit); m.add(del);
        m.show(e.getComponent(), e.getX(), e.getY());
    }

    private void openCreateStudentDialog() {
        JPanel p = new JPanel(new MigLayout("wrap 2, insets 20, fillx, gapy 15", "[100!][300!, fill]"));
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JTextField name = new JTextField();
        JTextField roll = new JTextField();
        JComboBox<String> prog = new JComboBox<>(new String[]{"BTECH", "MTECH", "PHD"});
        JComboBox<Branch> branch = new JComboBox<>();
        List<Branch> bList = adminService.getAllBranches();
        if (bList.isEmpty()) branch.addItem(new Branch(0, "", "No Branches"));
        else for (Branch b : bList) branch.addItem(b);
        branch.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Branch) setText(((Branch) value).getBranchName());
                return this;
            }
        });
        JComboBox<Integer> sem = new JComboBox<>(new Integer[]{1,2,3,4,5,6,7,8});
        Component[] comps = {user, pass, name, roll, prog, branch, sem};
        String[] labels = {"Username", "Password", "Full Name", "Roll No", "Program", "Branch", "Semester"};
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]); l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            p.add(l);
            p.add(comps[i], "h 35!");
        }
        int res = JOptionPane.showConfirmDialog(this, p, "New Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Branch selB = (Branch) branch.getSelectedItem();
            if (selB == null || selB.getBranchId() == 0) { JOptionPane.showMessageDialog(this, "Select a valid branch."); return; }
            String msg = adminService.createNewStudent(user.getText(), new String(pass.getPassword()), name.getText(), roll.getText(), (String) prog.getSelectedItem(), selB.getBranchCode(), (Integer) sem.getSelectedItem());
            if (msg.startsWith("Success")) refreshStudentTable(); else JOptionPane.showMessageDialog(this, msg);
        }
    }

    private void openCreateInstructorDialog() {
        JPanel p = new JPanel(new MigLayout("wrap 2, insets 20, fillx, gapy 15", "[100!][300!, fill]"));
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField dept = new JTextField();
        Component[] comps = {user, pass, name, email, dept};
        String[] labels = {"Username", "Password", "Name", "Email", "Dept"};
        for (int i = 0; i < labels.length; i++) {
            p.add(new JLabel(labels[i]));
            p.add(comps[i], "h 35!");
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, p, "New Instructor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            String msg = adminService.createNewInstructor(user.getText(), new String(pass.getPassword()), name.getText(), dept.getText(), email.getText());
            if (msg.startsWith("Success")) refreshInstructorTable(); else JOptionPane.showMessageDialog(this, msg);
        }
    }

    private class AnnouncementDialog extends JDialog {
        JTextField titleF; JTextArea msgF; Announcement ann;
        AnnouncementDialog(JFrame owner, Announcement a) {
            super(owner, a == null ? "New Announcement" : "Edit Announcement", true);
            this.ann = a;
            setSize(500, 400); setLocationRelativeTo(owner);
            JPanel p = new JPanel(new MigLayout("wrap, insets 20, fill", "[grow]", "[]10[]10[]10[grow]20[]"));
            p.setBackground(BACKGROUND);
            p.add(new JLabel("Title:"));
            titleF = new JTextField(a != null ? a.getTitle() : "");
            p.add(titleF, "growx, h 35!");
            p.add(new JLabel("Message:"));
            msgF = new JTextArea(a != null ? a.getMessage() : "");
            msgF.setLineWrap(true); msgF.setWrapStyleWord(true);
            msgF.setBorder(new LineBorder(BORDER));
            p.add(new JScrollPane(msgF), "grow");
            JButton save = new JButton("Save"); save.setBackground(ACCENT); save.setForeground(Color.WHITE);
            save.addActionListener(e -> {
                String t = titleF.getText(), m = msgF.getText();
                if (t.isEmpty() || m.isEmpty()) return;
                String res = (ann == null) ? adminService.createNewAnnouncement(t, m, userSession.getUserId()) : adminService.updateAnnouncement(ann.getAnnouncementId(), t, m);
                if (res.startsWith("Success")) dispose(); else JOptionPane.showMessageDialog(this, res);
            });
            p.add(save, "align right, w 100!, h 35!");
            add(p);
        }
    }

    private void refreshAllData() {
        refreshStudentTable();
        refreshInstructorTable();
        refreshAllSemesterCourseLists();
        refreshBranchTable();
        try { refreshAnnouncementsTable(announcementTableModel); } catch (SQLException e) {}
    }
    private void refreshStudentTable() {
        if (studentTableModel == null) return;
        studentTableModel.setRowCount(0);
        for (Student s : adminService.getAllStudents()) studentTableModel.addRow(new Object[]{s.getUserId(), s.getFullName(), s.getRollNo(), s.getProgram(), s.getBranch(), s.getYear()});
    }
    private void refreshInstructorTable() {
        if (instructorTableModel == null) return;
        instructorTableModel.setRowCount(0);
        for (Instructor i : adminService.getAllInstructors()) instructorTableModel.addRow(new Object[]{i.getUserId(), i.getFullName(), i.getEmail(), i.getDepartment()});
    }
    private void refreshBranchTable() {
        if (branchTableModel == null) return;
        branchTableModel.setRowCount(0);
        for (Branch b : adminService.getAllBranches()) branchTableModel.addRow(new Object[]{b.getBranchId(), b.getBranchCode(), b.getBranchName()});
    }
    private void refreshAllSemesterCourseLists() {
        if (semesterCourseLists.isEmpty()) return;
        for (int i = 0; i < semesterCourseLists.size(); i++) {
            DefaultListModel<Course> m = (DefaultListModel<Course>) semesterCourseLists.get(i).getModel();
            m.clear();
            List<Course> list = adminService.getCoursesBySemester(i + 1);
            if (list.isEmpty()) { Course c = new Course(); c.setCourseId(0); c.setTitle("No courses"); m.addElement(c); } else list.forEach(m::addElement);
        }
    }
    private void refreshAnnouncementsTable(DefaultTableModel m) throws SQLException {
        m.setRowCount(0);
        for (Announcement a : adminService.getAllAnnouncements()) m.addRow(new Object[]{a.getAnnouncementId(), a.getTitle(), a.getMessage(), a.getCreatedByName(), a.getCreatedAt()});
    }

    private JPanel createProfilePanel() { return createBlankPanel("My Profile"); }
    private JPanel createBlankPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(BACKGROUND);
        JLabel l = new JLabel(title); l.setFont(new Font("Segoe UI", Font.BOLD, 24)); l.setForeground(BORDER);
        p.add(l); return p;
    }
}