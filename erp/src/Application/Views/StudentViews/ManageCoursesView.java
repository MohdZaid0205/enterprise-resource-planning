package Application.Views.StudentViews;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Components.StyledComboBox;
import Application.Components.StyledField;
import Domain.Concretes.Course;
import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import Domain.Concretes.Student;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageCoursesView extends JPanel {

    private final Student student;
    private final String currentSemester;
    private JPanel listContainer;
    private StyledField searchField;
    private StyledComboBox<String> semFilter;

    public ManageCoursesView(Student student, String currentSemester) {
        this.student = student;
        this.currentSemester = currentSemester;

        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Course Catalog");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        gbc.gridx = 0;
        gbc.weightx = 0;
        header.add(title, gbc);

        searchField = new StyledField("Search Course Code or Name...");
        searchField.setPreferredSize(new Dimension(100, 40));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadCourses(); }
            public void removeUpdate(DocumentEvent e) { loadCourses(); }
            public void changedUpdate(DocumentEvent e) { loadCourses(); }
        });

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        header.add(searchField, gbc);

        // Semester Filter Dropdown
        String[] sems = {"Fall 2025", "Spring 2025"};
        semFilter = new StyledComboBox<>(sems);
        semFilter.setPreferredSize(new Dimension(150, 40));
        semFilter.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) loadCourses();
        });

        gbc.gridx = 2;
        gbc.weightx = 0;
        header.add(semFilter, gbc);

        StyledButton refreshBtn = new StyledButton("Refresh", StyleConstants.PRIMARY_COLOR);
        refreshBtn.setPreferredSize(new Dimension(100, 40));
        refreshBtn.addActionListener(e -> loadCourses());

        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        header.add(refreshBtn, gbc);

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

        loadCourses();
    }

    private void loadCourses() {
        listContainer.removeAll();

        // Get selected semester from dropdown and format it for DB (e.g. "Fall 2025" -> "FALL_2025")
        String selectedSem = (String) semFilter.getSelectedItem();
        String dbSemester = selectedSem != null ? selectedSem.toUpperCase().replace(" ", "_") : "FALL_2025";

        // FIX: Fetch enrollments directly from DB instead of relying on WeeklySchedule
        // (which might filter out courses missing timetable slots)
        List<String> enrolledSectionIds = getEnrolledSectionsForStudent(student.getId(), dbSemester);

        List<String> courseIds = getAllCourseIds();

        for (String cId : courseIds) {
            try {
                Course course = new Course(cId);
                String searchText = searchField.getText().trim().toLowerCase();
                boolean matchesSearch = searchText.isEmpty() ||
                        course.getName().toLowerCase().contains(searchText) ||
                        cId.toLowerCase().contains(searchText);

                if (matchesSearch) {
                    CourseItemPanel panel = new CourseItemPanel(course, dbSemester, enrolledSectionIds);
                    listContainer.add(panel);
                    listContainer.add(Box.createVerticalStrut(15));
                }

            } catch (Exception e) { e.printStackTrace(); }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private List<String> getEnrolledSectionsForStudent(String studentId, String semester) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT section_id FROM enrollments WHERE student_id = ? AND semester = ?";
        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:enrollments.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, semester);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getString("section_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    private List<String> getAllCourseIds() {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM courses";
        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:courses.db");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    private class CourseItemPanel extends JPanel {
        private final JPanel sectionsContainer;
        private final JPanel topBar;
        private boolean isExpanded = false;
        private final StyledButton infoBtn;

        public CourseItemPanel(Course course, String semester, List<String> enrolledSectionIds) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(240, 240, 240)),
                    new EmptyBorder(10, 15, 10, 15)
            ));

            topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            JLabel nameLabel = new JLabel("<html><span style='color:gray; font-size:12px'>" + course.getId() + "</span>" +
                    "&nbsp;&nbsp;&nbsp;<b style='font-size:14px; color:black'>" + course.getName() + "</b></html>");
            nameLabel.setFont(StyleConstants.NORMAL_FONT);

            infoBtn = new StyledButton("Info", StyleConstants.ACCENT_COLOR);
            infoBtn.setPreferredSize(new Dimension(80, 30));
            infoBtn.addActionListener(e -> toggleExpansion());

            topBar.add(nameLabel, BorderLayout.CENTER);
            topBar.add(infoBtn, BorderLayout.EAST);

            sectionsContainer = new JPanel();
            sectionsContainer.setLayout(new BoxLayout(sectionsContainer, BoxLayout.Y_AXIS));
            sectionsContainer.setOpaque(false);
            sectionsContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
            sectionsContainer.setVisible(false);

            loadSections(course, semester, enrolledSectionIds);

            add(topBar, BorderLayout.NORTH);
            add(sectionsContainer, BorderLayout.CENTER);
        }

        private void toggleExpansion() {
            isExpanded = !isExpanded;
            sectionsContainer.setVisible(isExpanded);
            infoBtn.setText(isExpanded ? "Close" : "Info");

            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension superPref = super.getPreferredSize();
            if (isExpanded) {
                return superPref;
            } else {
                Insets insets = getInsets();
                int collapsedHeight = topBar.getPreferredSize().height + insets.top + insets.bottom;
                return new Dimension(superPref.width, collapsedHeight);
            }
        }

        @Override
        public Dimension getMaximumSize() {
            if (isExpanded) {
                return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
            } else {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        }

        public void refreshSize() {
            if (isExpanded) {
                revalidate();
                repaint();
            }
        }

        private void loadSections(Course course, String semester, List<String> enrolledSectionIds) {
            List<String> sectionIds = getSectionsForCourse(course.getId(), semester);

            if (sectionIds.isEmpty()) {
                JLabel emptyLbl = new JLabel("No sections available for " + semester);
                emptyLbl.setForeground(StyleConstants.DISABLED_COLOR);
                emptyLbl.setBorder(new EmptyBorder(10, 10, 10, 10));
                sectionsContainer.add(emptyLbl);
                return;
            }

            String currentlyEnrolledSectionId = null;
            for (String secId : sectionIds) {
                if (enrolledSectionIds.contains(secId)) {
                    currentlyEnrolledSectionId = secId;
                    break;
                }
            }

            for (String secId : sectionIds) {
                try {
                    Section section = new Section(secId);
                    SectionItemPanel sectionRow = new SectionItemPanel(section, course, currentlyEnrolledSectionId, this);
                    sectionsContainer.add(sectionRow);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        private List<String> getSectionsForCourse(String courseId, String semester) {
            List<String> ids = new ArrayList<>();
            String sql = "SELECT id FROM sections WHERE course_id = ? AND semester = ?";
            try (Connection conn = sqliteConnector.connect("jdbc:sqlite:sections.db");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, courseId);
                stmt.setString(2, semester);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) ids.add(rs.getString("id"));
            } catch (SQLException e) { e.printStackTrace(); }
            return ids;
        }
    }

    private class SectionItemPanel extends JPanel {
        private final JPanel policyPanel;
        private boolean isPolicyVisible = false;
        private final StyledButton policyBtn;
        private final CourseItemPanel parentContainer;

        public SectionItemPanel(Section section, Course course, String currentlyEnrolledSectionId, CourseItemPanel parent) {
            this.parentContainer = parent;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    new EmptyBorder(5, 10, 5, 10)
            ));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            String instructorName = "Unknown";
            String instructorContact = "N/A";
            try {
                Instructor instr = new Instructor(section.getInstructorId());
                instructorName = instr.getName();
                instructorContact = instr.getEmail();
            } catch (Exception ignored) {}

            int enrolledCount = section.getContains();
            int capacity = section.getCapacity();
            boolean isFull = enrolledCount >= capacity;
            String capacityColor = isFull ? "#e74c3c" : "#7f8c8d";

            String detailsHtml = "<html>" +
                    "<b style='color:" + StyleConstants.PRIMARY_COLOR.getRGB() + "'>" + section.getId() + "</b>" +
                    " &nbsp; <span style='color:gray'>(" + course.getCredits() + " Credits)</span>" +
                    " &nbsp; <span style='font-size:11px; color:" + capacityColor + "'><b>[" + enrolledCount + "/" + capacity + "]</b></span>" +
                    " - <span style='font-size:11px; color:gray'><b>Instr:</b> " + instructorName + " (" + instructorContact + ")</span>" +
                    "</html>";

            JLabel details = new JLabel(detailsHtml);
            details.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setOpaque(false);

            policyBtn = new StyledButton("Policy ▼", StyleConstants.SECONDARY_COLOR);
            policyBtn.setPreferredSize(new Dimension(100, 30));
            policyBtn.addActionListener(e -> togglePolicy());
            buttonPanel.add(policyBtn);

            boolean isThisTheEnrolledSection = section.getId().equals(currentlyEnrolledSectionId);
            boolean hasOtherEnrollmentInCourse = (currentlyEnrolledSectionId != null && !isThisTheEnrolledSection);

            StyledButton actionBtn;

            // 1. Enrolled in THIS section -> DROP (Always takes priority)
            if (isThisTheEnrolledSection) {
                actionBtn = new StyledButton("Drop", StyleConstants.RED);
                actionBtn.addActionListener(e -> handleDrop(section.getId()));
            }
            // 2. Enrolled in SIBLING section -> ENROLL (Disabled)
            else if (hasOtherEnrollmentInCourse) {
                actionBtn = new StyledButton("Enroll", StyleConstants.DISABLED_COLOR);
                actionBtn.setEnabled(false);
                actionBtn.setToolTipText("Enrolled in another section.");
            }
            // 3. Section is FULL -> FULL (Disabled)
            else if (isFull) {
                actionBtn = new StyledButton("Full", StyleConstants.DISABLED_COLOR);
                actionBtn.setEnabled(false);
                actionBtn.setToolTipText("Section is at maximum capacity.");
            }
            // 4. Available -> ENROLL (Enabled)
            else {
                actionBtn = new StyledButton("Enroll", StyleConstants.GREEN);
                actionBtn.addActionListener(e -> handleEnroll(section.getId()));
            }

            actionBtn.setPreferredSize(new Dimension(80, 30));
            buttonPanel.add(actionBtn);

            header.add(details, BorderLayout.CENTER);
            header.add(buttonPanel, BorderLayout.EAST);

            policyPanel = createPolicyPanel(section);
            policyPanel.setVisible(false);
            policyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(header);
            add(policyPanel);
        }

        private void togglePolicy() {
            isPolicyVisible = !isPolicyVisible;
            policyPanel.setVisible(isPolicyVisible);
            policyBtn.setText(isPolicyVisible ? "Policy ▲" : "Policy ▼");

            revalidate();
            parentContainer.refreshSize();
        }

        private JPanel createPolicyPanel(Section section) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(new Color(250, 250, 250));
            p.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel weights = new JPanel(new GridLayout(2, 4, 5, 5));
            weights.setOpaque(false);
            weights.setAlignmentX(Component.LEFT_ALIGNMENT);
            weights.add(createStatLabel("Labs", section.getLabs() + "%"));
            weights.add(createStatLabel("Quiz", section.getQuiz() + "%"));
            weights.add(createStatLabel("Mid", section.getMidExams() + "%"));
            weights.add(createStatLabel("End", section.getEndExams() + "%"));
            weights.add(createStatLabel("Assign", section.getAssignments() + "%"));
            weights.add(createStatLabel("Proj", section.getProjects() + "%"));
            weights.add(createStatLabel("Bonus", section.getBonus() + "%"));

            JLabel weightHeader = new JLabel("<html><b>Assessment Weights</b></html>");
            weightHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(weightHeader);
            p.add(Box.createVerticalStrut(5));
            p.add(weights);
            p.add(Box.createVerticalStrut(10));

            JPanel slabs = new JPanel(new GridLayout(2, 4, 5, 5));
            slabs.setOpaque(false);
            slabs.setAlignmentX(Component.LEFT_ALIGNMENT);
            slabs.add(createStatLabel("O", ">= " + section.getO()));
            slabs.add(createStatLabel("A", ">= " + section.getA()));
            slabs.add(createStatLabel("A-", ">= " + section.getA_()));
            slabs.add(createStatLabel("B", ">= " + section.getB()));
            slabs.add(createStatLabel("B-", ">= " + section.getB_()));
            slabs.add(createStatLabel("C", ">= " + section.getC()));
            slabs.add(createStatLabel("C-", ">= " + section.getC_()));
            slabs.add(createStatLabel("D", ">= " + section.getD()));

            JLabel slabHeader = new JLabel("<html><b>Grading Slabs</b></html>");
            slabHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(slabHeader);
            p.add(Box.createVerticalStrut(5));
            p.add(slabs);

            return p;
        }

        private JLabel createStatLabel(String title, String val) {
            return new JLabel("<html><div style='text-align:center; color:gray; font-size:9px'>" + title +
                    "</div><div style='text-align:center; font-weight:bold; color:black; font-size:11px'>" + val + "</div></html>", SwingConstants.CENTER);
        }

        private void handleEnroll(String secId) {
            try {
                student.enrollInCourse(secId);
                JOptionPane.showMessageDialog(this, "Enrolled in " + secId);
                loadCourses();
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        private void handleDrop(String secId) {
            try {
                student.dropFromCourse(secId);
                JOptionPane.showMessageDialog(this, "Dropped " + secId);
                loadCourses();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}