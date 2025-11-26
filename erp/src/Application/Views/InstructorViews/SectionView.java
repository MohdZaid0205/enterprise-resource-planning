package Application.Views.InstructorViews;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Domain.Concretes.Course;
import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SectionView extends JPanel {

    private final Instructor instructor;
    private JPanel listContainer;

    public SectionView(Instructor instructor) {
        this.instructor = instructor;

        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("My Sections Overview");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);
        header.add(title);

        StyledButton refreshBtn = new StyledButton("Refresh", StyleConstants.SECONDARY_COLOR);
        refreshBtn.setPreferredSize(new Dimension(100, 35));
        refreshBtn.addActionListener(e -> loadSections());
        header.add(Box.createHorizontalStrut(20));
        header.add(refreshBtn);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(StyleConstants.DIM_WHITE);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(StyleConstants.DIM_WHITE);
        scroll.getViewport().setBackground(StyleConstants.DIM_WHITE);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadSections();
    }

    private void loadSections() {
        listContainer.removeAll();

        List<String> mySectionIds = getAssignedSectionIds();

        if (mySectionIds.isEmpty()) {
            JLabel empty = new JLabel("No sections currently assigned.");
            empty.setFont(StyleConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        } else {
            for (String secId : mySectionIds) {
                try {
                    Section section = new Section(secId);
                    Course course = new Course(section.getCourseId());

                    SectionInfoCard panel = new SectionInfoCard(section, course);
                    listContainer.add(panel);
                    listContainer.add(Box.createVerticalStrut(15));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private List<String> getAssignedSectionIds() {
        List<String> sections = new ArrayList<>();
        String sql = "SELECT id FROM sections WHERE TRIM(instructor_id) = ? OR TRIM(instructor_id) = ?";

        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instructor.getId());
            stmt.setString(2, instructor.getName());

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) sections.add(rs.getString("id"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sections;
    }

    private class SectionInfoCard extends JPanel {

        public SectionInfoCard(Section section, Course course) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
                    new EmptyBorder(20, 25, 20, 25)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JPanel idPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            idPanel.setOpaque(false);

            JLabel nameLbl = new JLabel(course.getName());
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLbl.setForeground(StyleConstants.BLACK);

            JLabel codesLbl = new JLabel("<html><span style='color:#7f8c8d'>" + course.getId() + "</span> &nbsp;&bull;&nbsp; <span style='color:#2980b9; font-weight:bold'>" + section.getId() + "</span></html>");
            codesLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            idPanel.add(nameLbl);
            idPanel.add(codesLbl);

            JPanel statsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
            statsPanel.setOpaque(false);

            statsPanel.add(createStatBox("Semester", section.getSemester()));
            statsPanel.add(createStatBox("Credits", String.valueOf(course.getCredits())));

            String strength = section.getContains() + " / " + section.getCapacity();
            statsPanel.add(createStatBox("Strength", strength));

            String room = "TBD";
            if (!section.getTimetable().isEmpty()) {
                room = section.getTimetable().get(0).room;
            }
            statsPanel.add(createStatBox("Location", room));

            add(idPanel, BorderLayout.WEST);
            add(statsPanel, BorderLayout.EAST);
        }

        private JPanel createStatBox(String title, String value) {
            JPanel box = new JPanel(new BorderLayout());
            box.setOpaque(false);

            JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            valLbl.setForeground(StyleConstants.PRIMARY_COLOR);

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            titleLbl.setForeground(Color.GRAY);

            box.add(valLbl, BorderLayout.CENTER);
            box.add(titleLbl, BorderLayout.SOUTH);
            return box;
        }
    }
}