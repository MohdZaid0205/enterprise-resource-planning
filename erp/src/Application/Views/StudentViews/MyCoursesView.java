package Application.Views.StudentViews;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Components.StyledTable;
import Domain.Concretes.Course;
import Domain.Concretes.Section;
import Domain.Concretes.Student;
import Domain.Database.sqliteConnector;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyCoursesView extends JPanel {

    private final Student student;
    private final DefaultTableModel detailModel;
    private final DefaultTableModel semesterModel;
    private final StyledTable detailTable;

    public MyCoursesView(Student student, String currentSemester) {
        this.student = student;
        setLayout(new BorderLayout());

        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);
        headerContainer.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel detailHeader = new JLabel("Course Breakdown");
        detailHeader.setFont(StyleConstants.HEADER_FONT);
        detailHeader.setForeground(StyleConstants.WHITE);

        StyledButton downloadBtn = new StyledButton("Export PDF", StyleConstants.GREEN);
        downloadBtn.setPreferredSize(new Dimension(150, 35));
        downloadBtn.addActionListener(e -> downloadTranscript());

        headerContainer.add(detailHeader, BorderLayout.WEST);
        headerContainer.add(downloadBtn, BorderLayout.EAST);

        String[] detailCols = {"Code", "Course Name", "Labs", "Quiz",
                "Mid", "End", "Assign", "Proj", "Total", "Grade"};

        Object[][] initialData = {
                {"", "Select a semester below to view details...", "", "", "", "", "", "", "", ""}
        };

        detailTable = new StyledTable(detailCols, initialData);
        detailModel = (DefaultTableModel) detailTable.getModel();

        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumnModel colModel = detailTable.getColumnModel();

        colModel.getColumn(0).setPreferredWidth(80);
        colModel.getColumn(0).setMinWidth(80);
        colModel.getColumn(1).setPreferredWidth(350);
        colModel.getColumn(1).setMinWidth(200);

        for (int i = 2; i < 10; i++) {
            colModel.getColumn(i).setPreferredWidth(70);
            colModel.getColumn(i).setMaxWidth(80);
            colModel.getColumn(i).setMinWidth(60);
        }

        detailTable.setEnabled(false);

        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());
        detailScroll.getViewport().setBackground(Color.WHITE);
        detailScroll.setPreferredSize(new Dimension(0, 300));

        topPanel.add(headerContainer, BorderLayout.NORTH);
        topPanel.add(detailScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel semHeader = new JLabel("Semester Performance Summary");
        semHeader.setFont(StyleConstants.NORMAL_FONT.deriveFont(Font.BOLD, 18f));
        semHeader.setForeground(StyleConstants.WHITE);
        semHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] semCols = {"Semester", "Credits Registered", "Credits Earned", "SGPA", "Status"};
        Object[][] emptyData = {};
        StyledTable semTable = new StyledTable(semCols, emptyData);
        semesterModel = (DefaultTableModel) semTable.getModel();

        JScrollPane semScroll = new JScrollPane(semTable);
        semScroll.setBorder(BorderFactory.createEmptyBorder());
        semScroll.getViewport().setBackground(Color.WHITE);

        semTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = semTable.getSelectedRow();
                if (row != -1) {
                    String selectedSem = (String) semesterModel.getValueAt(row, 0);
                    loadDetailedSubjectData(selectedSem);
                    detailHeader.setText("Result Details: " + selectedSem);
                }
            }
        });

        bottomPanel.add(semHeader, BorderLayout.NORTH);
        bottomPanel.add(semScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);

        add(splitPane, BorderLayout.CENTER);
        calculateAndLoadSemesters();
    }

    private void calculateAndLoadSemesters() {
        semesterModel.setRowCount(0);
        List<String> semesters = getSemestersFromDB();

        for (String sem : semesters) {
            double semGradePoints = 0;
            int semCredits = 0;
            List<String> sectionIds = getSectionIdsForSemester(sem);

            for (String secId : sectionIds) {
                try {
                    Section section = new Section(secId);
                    int credits = getCreditsForSection(secId);
                    Section.StudentGradeProxy grades = section.getStudentGradeRecord(student.getId(), student.permission);
                    float totalScore = sumMarks(grades);
                    double gradePoint = getGradePoint(totalScore, section);

                    semGradePoints += (gradePoint * credits);
                    semCredits += credits;
                } catch (Exception e) { e.printStackTrace(); }
            }

            double sgpa = (semCredits > 0) ? (semGradePoints / semCredits) : 0.0;
            semesterModel.addRow(new Object[]{
                    sem, semCredits, semCredits, String.format("%.2f", sgpa),
                    (sgpa > 4.0 ? "Good Standing" : "Academic Warning")
            });
        }
    }

    private void loadDetailedSubjectData(String semester) {
        detailModel.setRowCount(0);
        detailTable.setEnabled(true);

        List<String> sectionIds = getSectionIdsForSemester(semester);

        for (String secId : sectionIds) {
            try {
                Section section = new Section(secId);
                Section.StudentGradeProxy g = section.getStudentGradeRecord(student.getId(), student.permission);

                float total = sumMarks(g);
                String gradeLetter = getLetterGrade(total, section);

                String courseCode = secId;
                if (secId.contains("_") && secId.split("_").length >= 2) {
                    courseCode = secId.split("_")[1];
                }

                detailModel.addRow(new Object[]{
                        courseCode, section.getName(),
                        String.format("%.1f", g.getLabs()), String.format("%.1f", g.getQuiz()),
                        String.format("%.1f", g.getMidExams()), String.format("%.1f", g.getEndExams()),
                        String.format("%.1f", g.getAssignments()), String.format("%.1f", g.getProjects()),
                        String.format("%.1f", total), gradeLetter
                });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void downloadTranscript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript PDF");
        fileChooser.setSelectedFile(new File("Transcript_" + student.getId() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            try {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
                Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
                Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
                Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);

                Paragraph title = new Paragraph("Academic Transcript", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Student Name: " + student.getName(), headerFont));
                document.add(new Paragraph("Student ID: " + student.getId(), headerFont));
                document.add(new Paragraph("Date Generated: " + java.time.LocalDate.now(), normalFont));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));
                document.add(new Paragraph(" "));

                List<String> semesters = getSemestersFromDB();
                for (String sem : semesters) {
                    Paragraph semTitle = new Paragraph(sem, headerFont);
                    semTitle.setSpacingAfter(5);
                    document.add(semTitle);

                    PdfPTable table = new PdfPTable(6);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{1.5f, 3f, 1f, 1f, 1f, 1f});

                    addHeaderCell(table, "Course Code", headerFont);
                    addHeaderCell(table, "Title", headerFont);
                    addHeaderCell(table, "Credits", headerFont);
                    addHeaderCell(table, "Score", headerFont);
                    addHeaderCell(table, "Grade", headerFont);
                    addHeaderCell(table, "Points", headerFont);

                    List<String> sectionIds = getSectionIdsForSemester(sem);
                    double semPoints = 0;
                    int semCredits = 0;

                    for (String secId : sectionIds) {
                        try {
                            Section section = new Section(secId);
                            int creds = getCreditsForSection(secId);
                            Section.StudentGradeProxy g = section.getStudentGradeRecord(student.getId(), student.permission);
                            float total = sumMarks(g);
                            String letter = getLetterGrade(total, section);
                            double points = getGradePoint(total, section);

                            String code = secId.contains("_") && secId.split("_").length >= 2 ? secId.split("_")[1] : secId;

                            table.addCell(new Phrase(code, normalFont));
                            table.addCell(new Phrase(section.getName(), normalFont));
                            table.addCell(new Phrase(String.valueOf(creds), normalFont));
                            table.addCell(new Phrase(String.format("%.2f", total), normalFont));
                            table.addCell(new Phrase(letter, headerFont));
                            table.addCell(new Phrase(String.valueOf(points), normalFont));

                            semPoints += (points * creds);
                            semCredits += creds;

                            PdfPCell policyCell = new PdfPCell();
                            policyCell.setColspan(6);
                            policyCell.setBorder(Rectangle.BOTTOM);
                            String policyText = String.format("Policy: L:%.0f Q:%.0f M:%.0f E:%.0f | " +
                                            "Slabs: A>=%.0f B>=%.0f C>=%.0f",
                                    section.getLabs(), section.getQuiz(), section.getMidExams(), section.getEndExams(),
                                    section.getA(), section.getB(), section.getC());
                            policyCell.setPhrase(new Phrase(policyText, smallFont));
                            policyCell.setBackgroundColor(new Color(245, 245, 245));
                            table.addCell(policyCell);

                        } catch (Exception ex) { /* ignore */ }
                    }
                    document.add(table);

                    double sgpa = (semCredits > 0) ? semPoints / semCredits : 0.0;
                    Paragraph sgpaPara = new Paragraph("Semester SGPA: " + String.format("%.2f", sgpa), headerFont);
                    sgpaPara.setAlignment(Element.ALIGN_RIGHT);
                    sgpaPara.setSpacingAfter(10);
                    document.add(sgpaPara);

                    document.add(new Paragraph(" "));
                }

                document.close();
                JOptionPane.showMessageDialog(this, "PDF Transcript saved successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error generating PDF: " + ex.getMessage());
            }
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private float sumMarks(Section.StudentGradeProxy g) {
        return g.getLabs() + g.getQuiz() + g.getMidExams() + g.getEndExams() +
                g.getAssignments() + g.getProjects() + g.getBonus();
    }
    private double getGradePoint(float score, Section s) {
        if (score >= s.getO()) return 10.0;
        if (score >= s.getA()) return 10.0;
        if (score >= s.getA_()) return 9.0;
        if (score >= s.getB()) return 8.0;
        if (score >= s.getB_()) return 7.0;
        if (score >= s.getC()) return 6.0;
        if (score >= s.getC_()) return 5.0;
        if (score >= s.getD()) return 4.0;
        return 0.0;
    }
    private String getLetterGrade(float score, Section s) {
        if (score >= s.getO()) return "O";
        if (score >= s.getA()) return "A";
        if (score >= s.getA_()) return "A-";
        if (score >= s.getB()) return "B";
        if (score >= s.getB_()) return "B-";
        if (score >= s.getC()) return "C";
        if (score >= s.getC_()) return "C-";
        if (score >= s.getD()) return "D";
        return "F";
    }
    private int getCreditsForSection(String sectionId) {
        try {
            Section section = new Section(sectionId);
            Course course = new Course(section.getCourseId());
            return course.getCredits();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private List<String> getSemestersFromDB() {
        List<String> sems = new ArrayList<>();
        String sql = "SELECT DISTINCT semester FROM enrollments WHERE student_id = ?";
        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) sems.add(rs.getString("semester"));
        } catch (SQLException e) { e.printStackTrace(); }
        return sems;
    }
    private List<String> getSectionIdsForSemester(String semester) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT section_id FROM enrollments WHERE student_id = ? AND semester = ?";
        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getId());
            stmt.setString(2, semester);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getString("section_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }
}