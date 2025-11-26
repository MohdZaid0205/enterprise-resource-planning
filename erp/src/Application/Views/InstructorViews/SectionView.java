package Application.Views.InstructorViews;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Components.StyledField;
import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import Domain.Concretes.Student;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("My Sections Management");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);
        header.add(title);

        StyledButton refreshBtn = new StyledButton("Refresh List", StyleConstants.SECONDARY_COLOR);
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.addActionListener(e -> loadSections());
        header.add(Box.createHorizontalStrut(20));
        header.add(refreshBtn);

        // --- Content Scroll Area ---
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

        List<String> mySections = getAssignedSections();

        if (mySections.isEmpty()) {
            JLabel empty = new JLabel("You have no assigned sections.");
            empty.setFont(StyleConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        } else {
            for (String secId : mySections) {
                try {
                    Section section = new Section(secId);
                    SectionPanel panel = new SectionPanel(section);
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

    private List<String> getAssignedSections() {
        try {
            List<String> sections = new ArrayList<>();
            String sql = "SELECT section_id FROM teaching WHERE instructor_id = ?";
            try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, instructor.getId());
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) sections.add(rs.getString("section_id"));
            }
            return sections;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // --- Inner Class for Section Item ---
    private class SectionPanel extends JPanel {
        private final Section section;
        private final JPanel contentPanel;
        private boolean isExpanded = false;
        private final StyledButton toggleBtn;

        public SectionPanel(Section section) {
            this.section = section;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));

            // Top Bar (Always Visible)
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            String titleText = "<html><b style='font-size:16px; color:#2c3e50'>" + section.getId() + "</b>" +
                    " <span style='color:#7f8c8d'> | " + section.getSemester() + "</span></html>";
            JLabel title = new JLabel(titleText);

            // Buttons Container
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonsPanel.setOpaque(false);

            // Existing Buttons
//            StyledButton viewPolicyBtn = new StyledButton("View Policy", StyleConstants.SECONDARY_COLOR);
//            viewPolicyBtn.setPreferredSize(new Dimension(100, 35));
//            viewPolicyBtn.addActionListener(e -> showViewPolicyDialog());

            StyledButton editWeightsBtn = new StyledButton("Edit Weights", StyleConstants.SECONDARY_COLOR);
            editWeightsBtn.setPreferredSize(new Dimension(100, 35));
            editWeightsBtn.addActionListener(e -> showEditWeightsDialog());

            // New Button: Edit Slabs
            StyledButton editSlabsBtn = new StyledButton("Edit Slabs", StyleConstants.SECONDARY_COLOR);
            editSlabsBtn.setPreferredSize(new Dimension(100, 35));
            editSlabsBtn.addActionListener(e -> showEditSlabsDialog());

            toggleBtn = new StyledButton("View Students", StyleConstants.PRIMARY_COLOR);
            toggleBtn.setPreferredSize(new Dimension(120, 35));
            toggleBtn.addActionListener(e -> toggleExpansion());

//            buttonsPanel.add(viewPolicyBtn);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(editWeightsBtn);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(editSlabsBtn); // Added here
            buttonsPanel.add(Box.createHorizontalStrut(10));
            buttonsPanel.add(toggleBtn);

            topBar.add(title, BorderLayout.WEST);
            topBar.add(buttonsPanel, BorderLayout.EAST);

            // Content Panel (Hidden by default)
            contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setVisible(false);
            contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

            add(topBar, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void toggleExpansion() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                toggleBtn.setText("Hide Students");
                loadStudentTable();
            } else {
                toggleBtn.setText("View Students");
                contentPanel.removeAll();
            }
            contentPanel.setVisible(isExpanded);
            revalidate();
            repaint();
        }

        private void loadStudentTable() {
            contentPanel.removeAll();

            List<StudentGradeRow> rows = fetchStudentRows();

            if (rows.isEmpty()) {
                JLabel empty = new JLabel("No students enrolled in this section.");
                empty.setHorizontalAlignment(SwingConstants.CENTER);
                empty.setForeground(Color.GRAY);
                contentPanel.add(empty, BorderLayout.CENTER);
                return;
            }

            String[] columns = {"ID", "Name", "Lab", "Quiz", "Mid", "End", "Asgn", "Proj", "Bonus", "Total", "Action"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 10;
                }
            };

            for (StudentGradeRow r : rows) {
                float total = r.lab + r.quiz + r.mid + r.end + r.asgn + r.proj + r.bonus;
                model.addRow(new Object[]{
                        r.studentId, r.studentName,
                        r.lab, r.quiz, r.mid, r.end, r.asgn, r.proj, r.bonus,
                        String.format("%.2f", total),
                        "Edit"
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(35);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setBackground(new Color(245, 245, 245));
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setSelectionBackground(new Color(232, 240, 254));

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 2; i <= 9; i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            // Fixed Button Renderer
            table.getColumnModel().getColumn(10).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(10).setCellEditor(new ButtonEditor(new JCheckBox(), rows, section.getId(), this));

            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.setPreferredSize(new Dimension(0, 300));
            contentPanel.add(tableScroll, BorderLayout.CENTER);
        }

        private List<StudentGradeRow> fetchStudentRows() {
            List<StudentGradeRow> list = new ArrayList<>();
            String sql = "SELECT student_id FROM enrollments WHERE section_id = ?";

            try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, section.getId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String sId = rs.getString("student_id");
                    try {
                        Student s = new Student(sId);
                        Section.StudentGradeProxy grades = section.getStudentGradeRecord(sId, instructor.permission);

                        list.add(new StudentGradeRow(
                                sId, s.getName(),
                                grades.getLabs(), grades.getQuiz(), grades.getMidExams(),
                                grades.getEndExams(), grades.getAssignments(), grades.getProjects(), grades.getBonus()
                        ));
                    } catch (Exception ex) {
                        System.err.println("Error loading student " + sId + ": " + ex.getMessage());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        // --- Dialogs ---

        private void showViewPolicyDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Grading Policy - " + section.getId(), true);
            d.setSize(400, 400);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(20, 20, 20, 20));
            p.setBackground(Color.WHITE);

            p.add(new JLabel("<html><h3>Assessment Weights (Max)</h3></html>"));
            p.add(new JLabel("Labs: " + section.getLabs()));
            p.add(new JLabel("Quiz: " + section.getQuiz()));
            p.add(new JLabel("Mid Exam: " + section.getMidExams()));
            p.add(new JLabel("End Exam: " + section.getEndExams()));
            p.add(new JLabel("Assignments: " + section.getAssignments()));
            p.add(new JLabel("Projects: " + section.getProjects()));
            p.add(new JLabel("Bonus: " + section.getBonus()));

            p.add(Box.createVerticalStrut(20));
            p.add(new JLabel("<html><h3>Grading Slabs (Minimums)</h3></html>"));
            p.add(new JLabel("A: " + section.getA() + " | A-: " + section.getA_()));
            p.add(new JLabel("B: " + section.getB() + " | B-: " + section.getB_()));
            p.add(new JLabel("C: " + section.getC() + " | C-: " + section.getC_()));
            p.add(new JLabel("D: " + section.getD() + " | F: " + section.getF()));

            d.add(p, BorderLayout.CENTER);
            d.setVisible(true);
        }

        private void showEditWeightsDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Weights - " + section.getId(), true);
            d.setSize(350, 450);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
            form.setBorder(new EmptyBorder(20, 20, 20, 20));
            form.setBackground(Color.WHITE);

            StyledField lF = new StyledField(String.valueOf(section.getLabs()));
            StyledField qF = new StyledField(String.valueOf(section.getQuiz()));
            StyledField mF = new StyledField(String.valueOf(section.getMidExams()));
            StyledField eF = new StyledField(String.valueOf(section.getEndExams()));
            StyledField aF = new StyledField(String.valueOf(section.getAssignments()));
            StyledField pF = new StyledField(String.valueOf(section.getProjects()));
            StyledField bF = new StyledField(String.valueOf(section.getBonus()));

            form.add(new JLabel("Labs:")); form.add(lF);
            form.add(new JLabel("Quiz:")); form.add(qF);
            form.add(new JLabel("Mid Exam:")); form.add(mF);
            form.add(new JLabel("End Exam:")); form.add(eF);
            form.add(new JLabel("Assignments:")); form.add(aF);
            form.add(new JLabel("Projects:")); form.add(pF);
            form.add(new JLabel("Bonus:")); form.add(bF);

            StyledButton save = new StyledButton("Save Policy", StyleConstants.PRIMARY_COLOR);
            save.addActionListener(ev -> {
                try {
                    float l = Float.parseFloat(lF.getText());
                    float q = Float.parseFloat(qF.getText());
                    float m = Float.parseFloat(mF.getText());
                    float en = Float.parseFloat(eF.getText());
                    float a = Float.parseFloat(aF.getText());
                    float p = Float.parseFloat(pF.getText());
                    float b = Float.parseFloat(bF.getText());

                    if (l < 0 || q < 0 || m < 0 || en < 0 || a < 0 || p < 0 || b < 0) {
                        throw new Exception("Weights cannot be negative.");
                    }

                    float sum = l + q + m + en + a + p + b;
                    if (sum < 100 || sum > 120) {
                        throw new Exception("Total weightage must be between 100 and 120. Current: " + sum);
                    }

                    section.setLabs(l);
                    section.setQuiz(q);
                    section.setMidExams(m);
                    section.setEndExams(en);
                    section.setAssignments(a);
                    section.setProjects(p);
                    section.setBonus(b);

                    section.onPresistenceSave();
                    JOptionPane.showMessageDialog(d, "Weights updated.");
                    d.dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(d, "Invalid numbers entered.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel btnP = new JPanel();
            btnP.setBackground(Color.WHITE);
            btnP.add(save);

            d.add(form, BorderLayout.CENTER);
            d.add(btnP, BorderLayout.SOUTH);
            d.setVisible(true);
        }

        private void showEditSlabsDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Grading Slabs - " + section.getId(), true);
            d.setSize(350, 480);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
            form.setBorder(new EmptyBorder(20, 20, 20, 20));
            form.setBackground(Color.WHITE);

            StyledField aF = new StyledField(String.valueOf(section.getA()));
            StyledField amF = new StyledField(String.valueOf(section.getA_()));
            StyledField bF = new StyledField(String.valueOf(section.getB()));
            StyledField bmF = new StyledField(String.valueOf(section.getB_()));
            StyledField cF = new StyledField(String.valueOf(section.getC()));
            StyledField cmF = new StyledField(String.valueOf(section.getC_()));
            StyledField dF = new StyledField(String.valueOf(section.getD()));
            StyledField fF = new StyledField(String.valueOf(section.getF()));

            form.add(new JLabel("A (>=):")); form.add(aF);
            form.add(new JLabel("A- (>=):")); form.add(amF);
            form.add(new JLabel("B (>=):")); form.add(bF);
            form.add(new JLabel("B- (>=):")); form.add(bmF);
            form.add(new JLabel("C (>=):")); form.add(cF);
            form.add(new JLabel("C- (>=):")); form.add(cmF);
            form.add(new JLabel("D (>=):")); form.add(dF);
            form.add(new JLabel("F (>=):")); form.add(fF);

            StyledButton save = new StyledButton("Save Slabs", StyleConstants.PRIMARY_COLOR);
            save.addActionListener(ev -> {
                try {
                    float a = Float.parseFloat(aF.getText());
                    float am = Float.parseFloat(amF.getText());
                    float b = Float.parseFloat(bF.getText());
                    float bm = Float.parseFloat(bmF.getText());
                    float c = Float.parseFloat(cF.getText());
                    float cm = Float.parseFloat(cmF.getText());
                    float dd = Float.parseFloat(dF.getText());
                    float f = Float.parseFloat(fF.getText());

                    // LOGICAL VALIDATION
                    if (a > 100) throw new Exception("Grade A cannot require > 100.");
                    if (a <= am) throw new Exception("A must be greater than A-.");
                    if (am <= b) throw new Exception("A- must be greater than B.");
                    if (b <= bm) throw new Exception("B must be greater than B-.");
                    if (bm <= c) throw new Exception("B- must be greater than C.");
                    if (c <= cm) throw new Exception("C must be greater than C-.");
                    if (cm <= dd) throw new Exception("C- must be greater than D.");
                    if (dd <= f) throw new Exception("D must be greater than F.");
                    if (f < 0) throw new Exception("F cannot be negative.");

                    section.setA(a);
                    section.setA_(am);
                    section.setB(b);
                    section.setB_(bm);
                    section.setC(c);
                    section.setC_(cm);
                    section.setD(dd);
                    section.setF(f);

                    section.onPresistenceSave();
                    JOptionPane.showMessageDialog(d, "Slabs updated.");
                    d.dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(d, "Invalid numbers entered.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel btnP = new JPanel();
            btnP.setBackground(Color.WHITE);
            btnP.add(save);

            d.add(form, BorderLayout.CENTER);
            d.add(btnP, BorderLayout.SOUTH);
            d.setVisible(true);
        }
    }

    // --- Data Holder ---
    private static class StudentGradeRow {
        String studentId, studentName;
        float lab, quiz, mid, end, asgn, proj, bonus;

        public StudentGradeRow(String id, String name, float l, float q, float m, float e, float a, float p, float b) {
            this.studentId = id; this.studentName = name;
            this.lab = l; this.quiz = q; this.mid = m; this.end = e; this.asgn = a; this.proj = p; this.bonus = b;
        }
    }

    // --- Table Button Renderer ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(StyleConstants.ACCENT_COLOR);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(true);
            setBorder(new EmptyBorder(5, 10, 5, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Edit" : value.toString());
            setBackground(StyleConstants.ACCENT_COLOR);
            return this;
        }
    }

    // --- Table Button Editor ---
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private StudentGradeRow currentRowData;
        private String sectionId;
        private SectionPanel parentPanel;
        private List<StudentGradeRow> rows;
        private JTable table; // Added field to capture table

        public ButtonEditor(JCheckBox checkBox, List<StudentGradeRow> rows, String sectionId, SectionPanel parentPanel) {
            super(checkBox);
            this.rows = rows;
            this.sectionId = sectionId;
            this.parentPanel = parentPanel;
            button = new JButton();
            button.setOpaque(true);

            // Apply EXACT styling of ButtonRenderer to avoid visual glitch
            button.setBackground(StyleConstants.ACCENT_COLOR);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(true);
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                fireEditingStopped(); // Stop editing immediately on click

                // Clear selection to fix the visual "highlight" bug
                if (table != null) {
                    table.clearSelection();
                }

                // Invoke Dialog on a later swing event loop
                SwingUtilities.invokeLater(() -> {
                    if (currentRowData != null) {
                        showEditDialog(currentRowData, sectionId, parentPanel.section);
                    }
                });
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table; // Capture table
            label = (value == null) ? "Edit" : value.toString();
            button.setText(label);

            if (rows != null && row >= 0 && row < rows.size()) {
                currentRowData = rows.get(row);
            }
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    // --- Edit Dialog ---
    private void showEditDialog(StudentGradeRow data, String sectionId, Section section) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Marks: " + data.studentName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        StyledField labF = new StyledField(String.valueOf(data.lab));
        StyledField quizF = new StyledField(String.valueOf(data.quiz));
        StyledField midF = new StyledField(String.valueOf(data.mid));
        StyledField endF = new StyledField(String.valueOf(data.end));
        StyledField asgnF = new StyledField(String.valueOf(data.asgn));
        StyledField projF = new StyledField(String.valueOf(data.proj));
        StyledField bonusF = new StyledField(String.valueOf(data.bonus));

        form.add(new JLabel("Labs (Max: " + section.getLabs() + "):")); form.add(labF);
        form.add(new JLabel("Quiz (Max: " + section.getQuiz() + "):")); form.add(quizF);
        form.add(new JLabel("Mid Exam (Max: " + section.getMidExams() + "):")); form.add(midF);
        form.add(new JLabel("End Exam (Max: " + section.getEndExams() + "):")); form.add(endF);
        form.add(new JLabel("Assignments (Max: " + section.getAssignments() + "):")); form.add(asgnF);
        form.add(new JLabel("Projects (Max: " + section.getProjects() + "):")); form.add(projF);
        form.add(new JLabel("Bonus (Max: " + section.getBonus() + "):")); form.add(bonusF);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        StyledButton saveBtn = new StyledButton("Save Changes", StyleConstants.GREEN);
        saveBtn.setPreferredSize(new Dimension(150, 40));

        saveBtn.addActionListener(e -> {
            try {
                float l = Float.parseFloat(labF.getText());
                float q = Float.parseFloat(quizF.getText());
                float m = Float.parseFloat(midF.getText());
                float en = Float.parseFloat(endF.getText());
                float a = Float.parseFloat(asgnF.getText());
                float p = Float.parseFloat(projF.getText());
                float b = Float.parseFloat(bonusF.getText());

                // VALIDATION LOGIC
                validateMark(l, section.getLabs(), "Labs");
                validateMark(q, section.getQuiz(), "Quiz");
                validateMark(m, section.getMidExams(), "Mid Exams");
                validateMark(en, section.getEndExams(), "End Exams");
                validateMark(a, section.getAssignments(), "Assignments");
                validateMark(p, section.getProjects(), "Projects");
                validateMark(b, section.getBonus(), "Bonus");

                instructor.enterMarks(sectionId, data.studentId, l, q, m, en, a, p, b);

                JOptionPane.showMessageDialog(dialog, "Marks updated successfully.");
                dialog.dispose();
                loadSections();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void validateMark(float obtained, float max, String name) throws Exception {
        if (obtained > max) {
            throw new Exception(name + " marks (" + obtained + ") cannot exceed maximum weightage (" + max + ").");
        }
        if (obtained < 0) {
            throw new Exception(name + " marks cannot be negative.");
        }
    }
}