package edu.univ.erp.ui.student;
import edu.univ.erp.dao.AnnouncementDAO;
import edu.univ.erp.domain.Announcement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnnouncementViewDialog extends JDialog {

    private AnnouncementDAO announcementDAO;
    private JPanel announcementsListPanel;
    private static final Color HEADER_COLOR = new Color(44, 62, 80);
    private static final Color CARD_BG = new Color(248, 249, 250);

    public AnnouncementViewDialog(JFrame parent) {
        super(parent, "All Announcements", true);
        this.announcementDAO = new AnnouncementDAO();

        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("📢 All Announcements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        announcementsListPanel = new JPanel(new MigLayout("wrap, fillx, insets 10", "[grow, fill]"));
        announcementsListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(announcementsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        announcementsListPanel.removeAll();

        try {
            List<Announcement> announcements = announcementDAO.getAllAnnouncements();

            if (announcements.isEmpty()) {
                JLabel noAnnouncementsLabel = new JLabel("No announcements available at this time.");
                noAnnouncementsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noAnnouncementsLabel.setForeground(Color.GRAY);
                noAnnouncementsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                announcementsListPanel.add(noAnnouncementsLabel, "grow, push");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");

                for (Announcement announcement : announcements) {
                    JPanel announcementCard = createAnnouncementCard(announcement, formatter);
                    announcementsListPanel.add(announcementCard, "growx, gapbottom 10");
                }
            }

            announcementsListPanel.revalidate();
            announcementsListPanel.repaint();

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading announcements: " + e.getMessage());
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            errorLabel.setForeground(Color.RED);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            announcementsListPanel.add(errorLabel, "grow, push");
            e.printStackTrace();
        }
    }

    private JPanel createAnnouncementCard(Announcement announcement, DateTimeFormatter formatter) {
        JPanel card = new JPanel(new MigLayout("wrap, fillx, insets 15", "[grow]"));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(5, 5, 5, 5)
        ));

        JLabel titleLabel = new JLabel(announcement.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(HEADER_COLOR);
        card.add(titleLabel, "wrap");

        JLabel metaLabel = new JLabel(String.format("Posted by %s on %s",
                announcement.getCreatedByName(),
                announcement.getCreatedAt().format(formatter)));
        metaLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        metaLabel.setForeground(Color.GRAY);
        card.add(metaLabel, "wrap, gapbottom 10");

        JTextArea contentArea = new JTextArea(announcement.getMessage());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(null);
        card.add(contentArea, "growx");

        return card;
    }
}
