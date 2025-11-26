package Application.Views.StudentViews;

import Application.Components.StyleConstants;
import Application.Components.StyledComboBox; // Import added
import Domain.Concretes.Course;
import Domain.Concretes.Section;
import Domain.Concretes.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ItemEvent; // Import added
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableView extends JPanel {

    private final Student student;
    private String currentSemester; // Removed final to allow updates
    private final JPanel scheduleGrid;
    private final StyledComboBox<String> semesterCombo; // Added dropdown

    private static final int START_HOUR = 8;
    private static final int END_HOUR = 18;
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    public TimetableView(Student student, String currentSemester) {
        this.student = student;
        this.currentSemester = currentSemester;

        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Weekly Class Schedule");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        // --- Semester Selector Logic ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);

        JLabel semLabel = new JLabel("Semester: ");
        semLabel.setFont(StyleConstants.NORMAL_FONT);
        semLabel.setForeground(new Color(200, 200, 255));

        String[] sems = {"Fall 2025", "Spring 2025"};
        semesterCombo = new StyledComboBox<>(sems);
        semesterCombo.setPreferredSize(new Dimension(150, 35));

        // Set initial selection based on passed semester (simple logic)
        if(currentSemester.toUpperCase().contains("SPRING")) semesterCombo.setSelectedItem("Spring 2025");
        else semesterCombo.setSelectedItem("Fall 2025");

        semesterCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                refresh();
            }
        });

        controlPanel.add(semLabel);
        controlPanel.add(semesterCombo);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        scheduleGrid = new JPanel(new GridBagLayout());
        scheduleGrid.setBackground(StyleConstants.WHITE);

        JScrollPane scroll = new JScrollPane(scheduleGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        // Update current semester based on selection
        String selected = (String) semesterCombo.getSelectedItem();
        if(selected != null) {
            this.currentSemester = selected.toUpperCase().replace(" ", "_");
        }

        scheduleGrid.removeAll();
        buildGrid();
        loadData();
        scheduleGrid.revalidate();
        scheduleGrid.repaint();
    }

    private void buildGrid() {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        for (int hour = START_HOUR; hour < END_HOUR; hour++) {
            gbc.gridy = hour - START_HOUR + 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            gbc.weightx = 0.0;

            JLabel timeLabel = new JLabel(String.format("%02d:00", hour));
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            timeLabel.setForeground(StyleConstants.GRAY);
            timeLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            timeLabel.setPreferredSize(new Dimension(60, 60));

            scheduleGrid.add(timeLabel, gbc);
        }

        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;

        for (int i = 0; i < DAYS.length; i++) {
            gbc.gridx = i + 1;
            JLabel dayLabel = new JLabel(DAYS[i]);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayLabel.setFont(StyleConstants.BUTTON_FONT);
            dayLabel.setForeground(StyleConstants.PRIMARY_COLOR);
            dayLabel.setOpaque(true);
            dayLabel.setBackground(new Color(240, 245, 255));
            dayLabel.setBorder(new MatteBorder(0, 0, 2, 0, StyleConstants.ACCENT_COLOR));
            dayLabel.setPreferredSize(new Dimension(150, 40));

            scheduleGrid.add(dayLabel, gbc);
        }

        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < DAYS.length; i++) {
            for (int hour = START_HOUR; hour < END_HOUR; hour++) {
                gbc.gridx = i + 1;
                gbc.gridy = hour - START_HOUR + 1;

                JPanel cell = new JPanel();
                cell.setBackground(StyleConstants.WHITE);
                cell.setBorder(new MatteBorder(1, 0, 0, 1, new Color(240, 240, 240)));
                scheduleGrid.add(cell, gbc);
            }
        }
    }

    private void loadData() {
        // Use the updated currentSemester for fetching data
        Map<String, List<Section.TimeSlot>> schedule = student.getWeeklySchedule(currentSemester);
        Map<String, String> courseNameCache = new HashMap<>();

        for (Map.Entry<String, List<Section.TimeSlot>> entry : schedule.entrySet()) {
            String sectionId = entry.getKey();
            List<Section.TimeSlot> slots = entry.getValue();

            String courseName = courseNameCache.computeIfAbsent(sectionId, k -> {
                try {
                    Section s = new Section(k);
                    if (s.getCourseId() != null) {
                        return new Course(s.getCourseId()).getName();
                    }
                    return s.getName();
                } catch (Exception e) { return k; }
            });

            for (Section.TimeSlot slot : slots) {
                addBlockToGrid(slot, courseName, sectionId);
            }
        }
    }

    private void addBlockToGrid(Section.TimeSlot slot, String courseName, String sectionId) {
        int colIndex = -1;
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(slot.day)) {
                colIndex = i + 1;
                break;
            }
        }
        if (colIndex == -1) return;

        int startH = 0;
        try {
            startH = Integer.parseInt(slot.startTime.split(":")[0]);
        } catch (Exception e) { return; }

        if (startH < START_HOUR || startH >= END_HOUR) return;

        int rowIndex = startH - START_HOUR + 1;

        JPanel block = new JPanel(new BorderLayout());
        block.setBackground(new Color(230, 240, 255));
        block.setBorder(new MatteBorder(0, 4, 0, 0, StyleConstants.ACCENT_COLOR));
        block.setToolTipText(courseName + " (" + sectionId + ")");

        JLabel nameLbl = new JLabel("<html><b>" + courseName + "</b><br>" +
                "<span style='font-size:9px'>" + slot.room + "</span></html>");
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(StyleConstants.TERTIARY_COLOR);
        nameLbl.setBorder(new EmptyBorder(2, 5, 2, 2));

        block.add(nameLbl, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = colIndex;
        gbc.gridy = rowIndex;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        int rowSpan = (int) Math.ceil(slot.durationMins / 60.0);
        gbc.gridheight = Math.max(1, rowSpan);

        scheduleGrid.add(block, gbc);
        scheduleGrid.setComponentZOrder(block, 0);
    }
}