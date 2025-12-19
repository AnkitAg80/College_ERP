package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DatabaseConnection;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BackupRestorePanel extends JPanel {
    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private File backupDirectory;
    private JTextArea logArea;
    private JList<String> backupList;
    private DefaultListModel<String> backupListModel;

    public BackupRestorePanel() {
        String userHome = System.getProperty("user.home");
        backupDirectory = new File(userHome, "ERP_Backups");
        if (!backupDirectory.exists()) backupDirectory.mkdirs();
        initializeUI();
        loadBackupList();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("Database Backup & Restore");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_COLOR);
        JLabel subtitleLabel = new JLabel("Backup location: " + backupDirectory.getAbsolutePath());
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        JPanel headerPanel = new JPanel(new MigLayout("wrap"));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel(new MigLayout("wrap, fillx", "[grow, fill]"));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createBackupSection(), "growx, gapbottom 20");
        mainPanel.add(createRestoreSection(), "growx, gapbottom 20");
        mainPanel.add(createLogSection(), "growx, growy");
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createBackupSection() {
        JPanel panel = new JPanel(new MigLayout("wrap, fillx, insets 15", "[grow]"));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel title = new JLabel("\ud83d\udcbe Create Backup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(HEADER_COLOR);
        JLabel description = new JLabel("<html>Create a complete backup of both <b>erpdb</b> and <b>authdb</b> databases.</html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton backupButton = new JButton("Create Backup Now");
        backupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backupButton.setBackground(SUCCESS_COLOR);
        backupButton.setForeground(Color.WHITE);
        backupButton.setFocusPainted(false);
        backupButton.setBorderPainted(false);
        backupButton.setPreferredSize(new Dimension(200, 40));
        backupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backupButton.addActionListener(e -> performBackup());
        panel.add(title);
        panel.add(description, "gapbottom 10");
        panel.add(backupButton);
        return panel;
    }

    private JPanel createRestoreSection() {
        JPanel panel = new JPanel(new MigLayout("wrap, fillx, insets 15", "[grow][grow]"));
        panel.setBackground(new Color(255, 243, 205));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel title = new JLabel("\u26a0\ufe0f Restore from Backup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(HEADER_COLOR);
        JLabel warningLabel = new JLabel("<html><b>Warning:</b> This will OVERWRITE all current data!</html>");
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        warningLabel.setForeground(DANGER_COLOR);
        JLabel description = new JLabel("<html>Select a backup file from the list below or choose a custom file.</html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backupListModel = new DefaultListModel<>();
        backupList = new JList<>(backupListModel);
        backupList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(backupList);
        listScroll.setPreferredSize(new Dimension(0, 100));
        JButton restoreButton = new JButton("Restore Selected");
        restoreButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        restoreButton.setBackground(DANGER_COLOR);
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setFocusPainted(false);
        restoreButton.setBorderPainted(false);
        restoreButton.setPreferredSize(new Dimension(180, 35));
        restoreButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restoreButton.addActionListener(e -> restoreSelectedBackup());
        JButton chooseFileButton = new JButton("Choose Custom File");
        chooseFileButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chooseFileButton.setBackground(new Color(108, 117, 125));
        chooseFileButton.setForeground(Color.WHITE);
        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setBorderPainted(false);
        chooseFileButton.setPreferredSize(new Dimension(180, 35));
        chooseFileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseFileButton.addActionListener(e -> restoreCustomFile());
        panel.add(title, "span 2");
        panel.add(warningLabel, "span 2");
        panel.add(description, "span 2, gapbottom 10");
        panel.add(new JLabel("Available Backups:"), "span 2");
        panel.add(listScroll, "span 2, growx, gapbottom 10");
        panel.add(restoreButton);
        panel.add(chooseFileButton);
        return panel;
    }

    private JPanel createLogSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel title = new JLabel("\ud83d\udccb Activity Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(HEADER_COLOR);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 250));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadBackupList() {
        backupListModel.clear();
        File[] files = backupDirectory.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files != null && files.length > 0) {
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (File file : files) backupListModel.addElement(file.getName());
        } else backupListModel.addElement("(No backups found)");
    }

    private void performBackup() {
        JPasswordField passwordField = new JPasswordField(20);
        int option = JOptionPane.showConfirmDialog(this, new Object[]{"Enter MySQL root password:", passwordField}, "MySQL Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) { log("\u274c Backup cancelled by user."); return; }
        String password = new String(passwordField.getPassword());
        if (password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(backupDirectory, "backup_" + timestamp + ".sql");
        log("\ud83d\udd04 Starting backup...");
        log("Target file: " + backupFile.getName());
        new SwingWorker<Boolean, String>() {
            protected Boolean doInBackground() {
                try {
                    publish("Backing up erpdb...");
                    String command = String.format("mysqldump -u root -p%s --databases erpdb authdb", password);
                    Process process = Runtime.getRuntime().exec(command);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {
                        String line; while ((line = reader.readLine()) != null) writer.println(line);
                    }
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine; while ((errorLine = errorReader.readLine()) != null) publish("ERROR: " + errorLine);
                    }
                    int exitCode = process.waitFor();
                    if (exitCode == 0) { publish("\u2705 Backup completed successfully!"); return true; }
                    else { publish("\u274c Backup failed with exit code: " + exitCode); return false; }
                } catch (Exception e) { publish("\u274c Error: " + e.getMessage()); e.printStackTrace(); return false; }
            }
            protected void process(List<String> chunks) { for (String m : chunks) log(m); }
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(BackupRestorePanel.this, "\u2705 Backup created successfully!\n\nFile: " + backupFile.getName() + "\nLocation: " + backupDirectory.getAbsolutePath(), "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
                        loadBackupList();
                    } else {
                        JOptionPane.showMessageDialog(BackupRestorePanel.this, "\u274c Backup failed!\n\nCheck the log for details.", "Backup Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void restoreSelectedBackup() {
        String selected = backupList.getSelectedValue();
        if (selected == null || selected.equals("(No backups found)")) {
            JOptionPane.showMessageDialog(this, "Please select a backup file to restore.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        File backupFile = new File(backupDirectory, selected);
        performRestore(backupFile);
    }

    private void restoreCustomFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL Files (*.sql)", "sql"));
        fileChooser.setCurrentDirectory(backupDirectory);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) performRestore(fileChooser.getSelectedFile());
    }

    private void performRestore(File backupFile) {
        int confirm = JOptionPane.showConfirmDialog(this, "\u26a0\ufe0f WARNING \u26a0\ufe0f\n\nThis will PERMANENTLY DELETE all current data and replace it with:\n" + backupFile.getName() + "\n\nThis action CANNOT be undone!\n\nAre you absolutely sure?", "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) { log("\u274c Restore cancelled by user."); return; }
        JPasswordField passwordField = new JPasswordField(20);
        int option = JOptionPane.showConfirmDialog(this, new Object[]{"Enter MySQL root password:", passwordField}, "MySQL Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) { log("\u274c Restore cancelled."); return; }
        String password = new String(passwordField.getPassword());
        log("\ud83d\udd04 Starting restore from: " + backupFile.getName());
        new SwingWorker<Boolean, String>() {
            protected Boolean doInBackground() {
                try {
                    String restoreCommand = String.format("mysql -u root -p%s", password);
                    Process process = Runtime.getRuntime().exec(restoreCommand);
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream())); BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
                        String line; while ((line = reader.readLine()) != null) { writer.write(line); writer.newLine(); }
                    }
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine; while ((errorLine = errorReader.readLine()) != null) publish("ERROR: " + errorLine);
                    }
                    int exitCode = process.waitFor();
                    if (exitCode == 0) { publish("\u2705 Restore completed successfully!"); return true; }
                    else { publish("\u274c Restore failed with exit code: " + exitCode); return false; }
                } catch (Exception e) { publish("\u274c Error: " + e.getMessage()); e.printStackTrace(); return false; }
            }
            protected void process(List<String> chunks) { for (String m : chunks) log(m); }
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(BackupRestorePanel.this, "\u2705 Database restored successfully!\n\n\u26a0\ufe0f Please restart the application for changes to take effect.", "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(BackupRestorePanel.this, "\u274c Restore failed!\n\nCheck the log for details.", "Restore Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}