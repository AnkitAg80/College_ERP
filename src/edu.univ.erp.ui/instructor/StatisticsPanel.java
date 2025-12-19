package edu.univ.erp.ui.instructor;

import edu.univ.erp.dao.*;
import edu.univ.erp.domain.*;
import net.miginfocom.swing.MigLayout;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.TreeMap;

public class StatisticsPanel extends JPanel {

    private Instructor currentInstructor;
    private SectionDAO sectionDAO;
    private AssessmentDAO assessmentDAO;
    private GradeDAO gradeDAO;
    private EnrollmentDAO enrollmentDAO;

    private JComboBox<Section> sectionComboBox;
    private JTable assessmentStatsTable;
    private DefaultTableModel assessmentStatsModel;
    private JTable gradeDistTable;
    private DefaultTableModel gradeDistModel;

    private JPanel chartsPanel;

    public StatisticsPanel(Instructor instructor) {
        this.currentInstructor = instructor;
        this.sectionDAO = new SectionDAO();
        this.assessmentDAO = new AssessmentDAO();
        this.gradeDAO = new GradeDAO();
        this.enrollmentDAO = new EnrollmentDAO();

        initializeUI();
        loadSections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new MigLayout("insets 20", "[][grow]", "[]"));
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Class Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel sectionLabel = new JLabel("Select Section:");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        sectionComboBox = new JComboBox<>();
        sectionComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sectionComboBox.addActionListener(e -> calculateAndShowStats());

        sectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Section) {
                    setText(((Section) value).toString());
                }
                return this;
            }
        });

        JButton exportButton = new JButton("Export Data");
        exportButton.setBackground(new Color(33, 150, 243));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportToPDF());

        topPanel.add(titleLabel, "span 2, wrap, gapbottom 20");
        topPanel.add(sectionLabel, "gapright 10");
        topPanel.add(sectionComboBox, "width 300!");
        topPanel.add(exportButton, "width 150!, gapright 20");

        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 20, wrap 2", "[grow][grow]", "[][grow]"));
        contentPanel.setBackground(Color.WHITE);

        JLabel table1Label = new JLabel("Assessment Performance (Mean, Median, Range)");
        table1Label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        table1Label.setForeground(new Color(44, 62, 80));

        assessmentStatsModel = new DefaultTableModel(new Object[]{"Assessment", "Mean", "Median", "Lowest", "Highest"}, 0);
        assessmentStatsTable = new JTable(assessmentStatsModel);
        styleTable(assessmentStatsTable);
        JScrollPane scrollPane1 = new JScrollPane(assessmentStatsTable);
        scrollPane1.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel table2Label = new JLabel("Final Grade Distribution");
        table2Label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        table2Label.setForeground(new Color(44, 62, 80));

        gradeDistModel = new DefaultTableModel(new Object[]{"Grade", "Count", "Percentage"}, 0);
        gradeDistTable = new JTable(gradeDistModel);
        styleTable(gradeDistTable);
        JScrollPane scrollPane2 = new JScrollPane(gradeDistTable);
        scrollPane2.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        contentPanel.add(table1Label, "span 2, wrap");
        contentPanel.add(scrollPane1, "span 2, growx, h 250!, wrap, gapbottom 20");

        contentPanel.add(table2Label, "span 2, wrap");
        contentPanel.add(scrollPane2, "span 2, growx, h 200!");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void loadSections() {
        try {
            List<Section> sections = sectionDAO.getSectionsByInstructor(currentInstructor.getUserId());
            sectionComboBox.removeAllItems();
            if (sections.isEmpty()) {
                sectionComboBox.addItem(new Section() {
                    public String toString() { return "No Sections Found"; }
                });
                sectionComboBox.setEnabled(false);
            } else {
                for (Section s : sections) {
                    sectionComboBox.addItem(s);
                }
                sectionComboBox.setEnabled(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndShowStats() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null || selectedSection.getSectionId() == 0) return;

        int sectionId = selectedSection.getSectionId();

        assessmentStatsModel.setRowCount(0);
        gradeDistModel.setRowCount(0);

        try {
            List<Assessment> assessments = assessmentDAO.getAssessmentsForSection(sectionId);
            List<Grade> allGrades = gradeDAO.getGradesForSection(sectionId);

            for (Assessment asm : assessments) {
                List<Double> scores = allGrades.stream()
                        .filter(g -> g.getAssessmentId() == asm.getAssessmentId() && g.getScore() != null)
                        .map(g -> g.getScore().doubleValue())
                        .sorted()
                        .collect(Collectors.toList());

                if (scores.isEmpty()) {
                    assessmentStatsModel.addRow(new Object[]{asm.getName(), "-", "-", "-", "-"});
                } else {
                    double sum = 0;
                    for (double s : scores) sum += s;
                    double mean = sum / scores.size();

                    double median;
                    int size = scores.size();
                    if (size % 2 == 0) {
                        median = (scores.get(size/2 - 1) + scores.get(size/2)) / 2.0;
                    } else {
                        median = scores.get(size/2);
                    }

                    double min = scores.get(0);
                    double max = scores.get(size - 1);

                    assessmentStatsModel.addRow(new Object[]{
                            asm.getName(),
                            String.format("%.2f", mean),
                            String.format("%.2f", median),
                            String.format("%.2f", min),
                            String.format("%.2f", max)
                    });
                }
            }

            List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
            Map<String, Integer> gradeCounts = new TreeMap<>();
            int totalGraded = 0;

            for (Enrollment e : enrollments) {
                String g = e.getFinalGrade();
                if (g != null && !g.trim().isEmpty()) {
                    gradeCounts.put(g, gradeCounts.getOrDefault(g, 0) + 1);
                    totalGraded++;
                }
            }

            String[] gradeOrder = {"A+", "A", "A-", "B", "B-", "C", "C-", "D", "F"};

            for (String grade : gradeOrder) {
                if (gradeCounts.containsKey(grade)) {
                    int count = gradeCounts.get(grade);
                    double percentage = (double) count / totalGraded * 100;
                    gradeDistModel.addRow(new Object[]{
                            grade,
                            count,
                            String.format("%.1f%%", percentage)
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching statistics: " + e.getMessage());
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
    }

    private void exportToPDF() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null || selectedSection.getSectionId() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "No Section Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (assessmentStatsModel.getRowCount() == 0 && gradeDistModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDFont boldFont = PDType1Font.HELVETICA_BOLD;
            PDFont regularFont = PDType1Font.HELVETICA;

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float pageWidth = page.getMediaBox().getWidth();

            contentStream.beginText();
            contentStream.setFont(boldFont, 20);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Class Statistics Report");
            contentStream.endText();
            yPosition -= 30;

            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Section: " + selectedSection.toString());
            contentStream.endText();
            yPosition -= 20;

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            contentStream.beginText();
            contentStream.setFont(regularFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Generated on: " + currentDate);
            contentStream.endText();
            yPosition -= 30;

            if (assessmentStatsModel.getRowCount() > 0) {
                contentStream.beginText();
                contentStream.setFont(boldFont, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Assessment Performance");
                contentStream.endText();
                yPosition -= 20;

                String[] headers1 = {"Assessment", "Mean", "Median", "Lowest", "Highest"};
                float[] columnWidths1 = {150, 70, 70, 70, 70};

                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                float xOffset = margin;
                contentStream.newLineAtOffset(xOffset, yPosition);
                for (int i = 0; i < headers1.length; i++) {
                    contentStream.showText(headers1[i]);
                    xOffset += columnWidths1[i];
                    contentStream.newLineAtOffset(columnWidths1[i], 0);
                }
                contentStream.endText();
                yPosition -= 15;

                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                yPosition -= 15;

                contentStream.setFont(regularFont, 10);
                for (int row = 0; row < assessmentStatsModel.getRowCount(); row++) {
                    if (yPosition < margin + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }

                    xOffset = margin;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xOffset, yPosition);
                    for (int col = 0; col < assessmentStatsModel.getColumnCount(); col++) {
                        Object value = assessmentStatsModel.getValueAt(row, col);
                        String text = value != null ? value.toString() : "-";
                        contentStream.showText(text);
                        xOffset += columnWidths1[col];
                        contentStream.newLineAtOffset(columnWidths1[col], 0);
                    }
                    contentStream.endText();
                    yPosition -= 15;
                }
                yPosition -= 20;
            }

            if (gradeDistModel.getRowCount() > 0) {
                if (yPosition < margin + 150) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                contentStream.beginText();
                contentStream.setFont(boldFont, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Final Grade Distribution");
                contentStream.endText();
                yPosition -= 20;

                String[] headers2 = {"Grade", "Count", "Percentage"};
                float[] columnWidths2 = {100, 100, 100};

                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                float xOffset = margin;
                contentStream.newLineAtOffset(xOffset, yPosition);
                for (int i = 0; i < headers2.length; i++) {
                    contentStream.showText(headers2[i]);
                    xOffset += columnWidths2[i];
                    contentStream.newLineAtOffset(columnWidths2[i], 0);
                }
                contentStream.endText();
                yPosition -= 15;

                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + columnWidths2[0] + columnWidths2[1] + columnWidths2[2], yPosition);
                contentStream.stroke();
                yPosition -= 15;

                contentStream.setFont(regularFont, 10);
                for (int row = 0; row < gradeDistModel.getRowCount(); row++) {
                    if (yPosition < margin + 50) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }

                    xOffset = margin;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xOffset, yPosition);
                    for (int col = 0; col < gradeDistModel.getColumnCount(); col++) {
                        Object value = gradeDistModel.getValueAt(row, col);
                        String text = value != null ? value.toString() : "-";
                        contentStream.showText(text);
                        xOffset += columnWidths2[col];
                        contentStream.newLineAtOffset(columnWidths2[col], 0);
                    }
                    contentStream.endText();
                    yPosition -= 15;
                }
            }

            contentStream.close();

            String fileName = "Statistics_" + selectedSection.getSectionId() + "_" +
                            System.currentTimeMillis() + ".pdf";
            File file = new File(fileName);
            document.save(file);
            document.close();

            JOptionPane.showMessageDialog(this,
                "Statistics exported successfully!\nFile: " + file.getAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error exporting to PDF: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}