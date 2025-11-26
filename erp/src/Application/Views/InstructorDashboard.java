package Application.Views;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Views.InstructorViews.CourseStatisticsView;
import Application.Views.InstructorViews.GradebookView;
import Application.Views.InstructorViews.SectionView;
import Domain.Concretes.Instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InstructorDashboard extends JFrame {

    private final Instructor instructor;
    private JPanel contentArea;

    public InstructorDashboard(Instructor instructor) {
        this.instructor = instructor;

        setTitle("Instructor Dashboard - " + instructor.getName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(StyleConstants.WHITE);

        contentArea.add(new SectionView(instructor), "SECTIONS");

        contentArea.add(new GradebookView(instructor), "GRADING");

        contentArea.add(new CourseStatisticsView(instructor), "STATS");

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
        sidebar.add(createLogoutPanel());

        return sidebar;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profilePanel.setBackground(StyleConstants.WHITE);
        profilePanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        profilePanel.setMaximumSize(new Dimension(250, 180));

        String htmlInfo = "<html>" +
                "<b style='font-size:16px; color:#2c3e50'>" + instructor.getName() + "</b><br>" +
                "<div style='margin-top: 8px; font-size:11px; color:#7f8c8d'>" +
                "<b>ID:</b> " + instructor.getId() + "<br>" +
                "<b>Email:</b> " + instructor.getEmail() + "<br>" +
                "<b>Phone:</b> " + instructor.getPhone() + "<br>" +
                "<b>Role:</b> <span style='color:#e67e22'>Faculty Instructor</span>" +
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

        navPanel.add(createNavButton("My Sections", "SECTIONS"));
        navPanel.add(createNavButton("Gradebook", "GRADING"));
        navPanel.add(createNavButton("Statistics", "STATS"));

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
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(StyleConstants.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(StyleConstants.BLACK);
            }
        });

        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentArea.getLayout();
            cl.show(contentArea, cardName);
        });

        return btn;
    }
}