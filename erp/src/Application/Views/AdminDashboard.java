package Application.Views;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Views.AdminViews.*;
import Domain.Concretes.Admin;
import Domain.Rules.ApplicationRules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboard extends JFrame {

    private final Admin admin;
    private JPanel contentArea;
    private StyledButton maintenanceBtn;

    public AdminDashboard(Admin admin) {
        this.admin = admin;

        setTitle("Admin Dashboard - " + admin.getName());
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(StyleConstants.WHITE);

        contentArea.add(new AdminStudentView(), "STUDENTS");
        contentArea.add(new AdminInstructorView(), "INSTRUCTORS");
        contentArea.add(new AdminCourseView(), "COURSES");
        contentArea.add(new AdminSectionView(), "SECTIONS");

        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(StyleConstants.WHITE);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        sidebar.add(createProfilePanel());
        sidebar.add(new JSeparator());
        sidebar.add(createNavigationPanel());
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createMaintenancePanel());
        sidebar.add(createLogoutPanel());

        return sidebar;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profilePanel.setBackground(StyleConstants.WHITE);
        profilePanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(250, 150));

        String htmlInfo = "<html>" +
                "<b style='font-size:16px; color:#c0392b'>ADMINISTRATOR</b><br>" +
                "<div style='margin-top: 8px; font-size:12px; color:#7f8c8d'>" +
                "<b>ID:</b> " + admin.getId() + "<br>" +
                "<b>Name:</b> " + admin.getName() +
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

        navPanel.add(createNavButton("Manage Students", "STUDENTS"));
        navPanel.add(createNavButton("Manage Instructors", "INSTRUCTORS"));
        navPanel.add(createNavButton("Manage Courses", "COURSES"));
        navPanel.add(createNavButton("Manage Sections", "SECTIONS"));

        return navPanel;
    }

    private JPanel createMaintenancePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(StyleConstants.WHITE);
        p.setBorder(new EmptyBorder(0, 15, 10, 15));
        p.setMaximumSize(new Dimension(250, 60));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        maintenanceBtn = new StyledButton("Maintenance: OFF", Color.ORANGE);
        maintenanceBtn.setPreferredSize(new Dimension(210, 40));
        maintenanceBtn.setForeground(Color.BLACK); // Dark text for yellow/orange bg

        // Set initial state
        boolean isModeOn = ApplicationRules.isMaintenanceMode();
        updateMaintenanceButton(isModeOn);

        maintenanceBtn.addActionListener(e -> {
            boolean currentState = ApplicationRules.isMaintenanceMode();
            ApplicationRules.setMaintenanceMode(!currentState);
            updateMaintenanceButton(!currentState);
        });

        p.add(maintenanceBtn);
        return p;
    }

    private void updateMaintenanceButton(boolean isOn) {
        if (isOn) {
            maintenanceBtn.setText("MAINTENANCE: ON");
            maintenanceBtn.setBackground(Color.YELLOW);
        } else {
            maintenanceBtn.setText("Maintenance: OFF");
            maintenanceBtn.setBackground(Color.ORANGE);
        }
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
            new LoginView().setVisible(true);
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
                    g2.setColor(new Color(240, 240, 240));
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
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(StyleConstants.ACCENT_COLOR); }
            @Override public void mouseExited(MouseEvent e) { btn.setForeground(StyleConstants.BLACK); }
        });

        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentArea.getLayout();
            cl.show(contentArea, cardName);
        });

        return btn;
    }
}