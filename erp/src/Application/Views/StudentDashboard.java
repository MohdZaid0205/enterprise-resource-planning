package Application.Views;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Views.StudentViews.*;
import Domain.Concretes.Student;
import Domain.Rules.ApplicationRules;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class StudentDashboard extends JFrame {

    private final Student student;
    private JPanel contentArea;
    private String currentSemester;

    private TimetableView timetableView;
    private boolean isMaintenance;

    public StudentDashboard(Student student) {
        this.student = student;
        this.currentSemester = calculateSemester(student.getEnrollmentDate());
        this.isMaintenance = ApplicationRules.isMaintenanceMode();

        setTitle("Student Dashboard - " + student.getName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(StyleConstants.WHITE);

        timetableView = new TimetableView(student, currentSemester);

        contentArea.add(new MyCoursesView(student, currentSemester), "MY_COURSES");
        contentArea.add(new ManageCoursesView(student, currentSemester, isMaintenance), "MANAGE");
        contentArea.add(timetableView, "TIMETABLE");

        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(StyleConstants.WHITE);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, StyleConstants.DIM_WHITE));

        sidebar.add(createProfilePanel());


        if (isMaintenance) {
            JPanel warningPanel = new JPanel(new BorderLayout());
            warningPanel.setBackground(StyleConstants.YELLOW);
            warningPanel.setBorder(new CompoundBorder(
                    new MatteBorder(0, 4, 0, 0, StyleConstants.WARN),
                    new EmptyBorder(10, 15, 10, 15)
            ));

            warningPanel.setMaximumSize(new Dimension(250, 60));
            warningPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel warnIcon = new JLabel("<html><b>Maintenance Mode</b><br><span style='font-size:10px; " +
                    "color:#555'>Changes are disabled.</span></html>");
            warnIcon.setForeground(StyleConstants.BLACK);

            warningPanel.add(warnIcon, BorderLayout.CENTER);
            sidebar.add(warningPanel);
        }

        sidebar.add(new JSeparator());
        sidebar.add(createNavigationPanel());
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createLogoutPanel());

        return sidebar;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profilePanel.setBackground(StyleConstants.WHITE);
        profilePanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(250, 200));

        String htmlInfo = "<html>" +
                "<b style='font-size:16px; color:#2c3e50'>" + student.getName() + "</b><br>" +
                "<div style='margin-top: 8px; font-size:11px; color:#7f8c8d'>" +
                "<b>ID:</b> " + student.getId() + "<br>" +
                "<b>Email:</b> " + student.getEmail() + "<br>" +
                "<b>Phone:</b> " + student.getPhone() + "<br>" +
                "<b>Enrolled:</b> " + student.getEnrollmentDate() + "<br>" +
                "<b>Status:</b> <span style='color:#2980b9'>" + currentSemester + "</span>" +
                "</div></html>";

        JLabel infoLabel = new JLabel(htmlInfo);
        infoLabel.setFont(StyleConstants.NORMAL_FONT);

        profilePanel.add(infoLabel);
        return profilePanel;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(StyleConstants.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        navPanel.add(createNavButton("My Courses", "MY_COURSES"));
        navPanel.add(createNavButton("Manage Courses", "MANAGE"));
        navPanel.add(createNavButton("Timetable", "TIMETABLE"));

        return navPanel;
    }

    private JPanel createLogoutPanel() {
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(StyleConstants.WHITE);
        logoutPanel.setBorder(new EmptyBorder(0, 15, 20, 15));
        logoutPanel.setMaximumSize(new Dimension(250, 70));
        logoutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton logoutBtn = new StyledButton("Logout", StyleConstants.RED);
        logoutBtn.setPreferredSize(new Dimension(210, 40));
        logoutBtn.addActionListener(e -> {
            dispose();
            new Application.Views.LoginView().setVisible(true);
        });

        logoutPanel.add(logoutBtn);
        return logoutPanel;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(StyleConstants.ACCENT_COLOR_ALPHA);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(StyleConstants.ACCENT_COLOR);
                    g2.fillRect(0, 0, 4, getHeight());
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(StyleConstants.BLACK);
        btn.setBackground(StyleConstants.WHITE);
        btn.setBorder(new EmptyBorder(12, 25, 12, 10));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(StyleConstants.WHITE); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(StyleConstants.BLACK); }
        });

        btn.addActionListener(e -> {
            if (cardName.equals("TIMETABLE")) {
                timetableView.refresh();
            }

            CardLayout cl = (CardLayout) contentArea.getLayout();
            cl.show(contentArea, cardName);
        });

        return btn;
    }

    private String calculateSemester(String enrollmentDate) {
        try {
            LocalDate enroll = LocalDate.parse(enrollmentDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate now = LocalDate.now();
            Period diff = Period.between(enroll, now);
            long months = diff.toTotalMonths();
            long semIndex = (months / 5) + 1;
            return "Semester " + semIndex;
        } catch (Exception e) {
            return "Semester 1";
        }
    }
}