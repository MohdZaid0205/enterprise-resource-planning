package Application.Views;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Application.Views.AdminViews.*;
import Domain.Concretes.Admin;
import Domain.Rules.ApplicationRules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdminDashboard extends JFrame {

    private final Admin admin;
    private JPanel contentArea;
    private StyledButton maintenanceBtn;

    private AdminStudentView studentView;
    private AdminInstructorView instructorView;
    private AdminCourseView courseView;
    private AdminSectionView sectionView;

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

        studentView = new AdminStudentView();
        instructorView = new AdminInstructorView();
        courseView = new AdminCourseView();
        sectionView = new AdminSectionView();

        contentArea.add(studentView, "STUDENTS");
        contentArea.add(instructorView, "INSTRUCTORS");
        contentArea.add(courseView, "COURSES");
        contentArea.add(sectionView, "SECTIONS");

        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(StyleConstants.WHITE);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, StyleConstants.DIM_WHITE));

        sidebar.add(createProfilePanel());
        sidebar.add(new JSeparator());
        sidebar.add(createNavigationPanel());
        sidebar.add(Box.createVerticalGlue());

        sidebar.add(createBackupRestorePanel());
        sidebar.add(Box.createVerticalStrut(10));

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

    private JPanel createBackupRestorePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        p.setBackground(StyleConstants.WHITE);
        p.setBorder(new EmptyBorder(0, 15, 0, 15));
        p.setMaximumSize(new Dimension(250, 45));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton exportBtn = new StyledButton("Export DB", StyleConstants.ACCENT_COLOR);
        exportBtn.setPreferredSize(new Dimension(100, 35));
        exportBtn.addActionListener(e -> handleExport());

        StyledButton importBtn = new StyledButton("Import DB", StyleConstants.ACCENT_COLOR);
        importBtn.setPreferredSize(new Dimension(100, 35));
        importBtn.addActionListener(e -> handleImport());

        p.add(exportBtn);
        p.add(importBtn);
        return p;
    }

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Export Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetDir = chooser.getSelectedFile();
            String prefix = JOptionPane.showInputDialog(this, "Enter filename prefix (e.g., 'backup_2023'):", "backup");

            if (prefix == null || prefix.trim().isEmpty()) prefix = "backup";

            try {
                File erpDb = new File("erp.db");
                File credDb = new File("credentials.db");

                if (erpDb.exists()) {
                    Files.copy(erpDb.toPath(), new File(targetDir, prefix + "_erp.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                if (credDb.exists()) {
                    Files.copy(credDb.toPath(), new File(targetDir, prefix + "_credentials.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                JOptionPane.showMessageDialog(this, "Database exported successfully as " + prefix + "_*.db");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleImport() {
        JFileChooser erpChooser = new JFileChooser();
        erpChooser.setDialogTitle("Select ERP Database Backup File (.db)");
        erpChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database", "db"));
        if (erpChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File backupErp = erpChooser.getSelectedFile();

        JFileChooser credChooser = new JFileChooser();
        credChooser.setDialogTitle("Select Credentials Database Backup File (.db)");
        credChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database", "db"));
        if (credChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File backupCred = credChooser.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
                "This will overwrite current data with the selected backups. Application needs restart. Continue?",
                "Confirm Import", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (backupErp.exists()) {
                    Files.copy(backupErp.toPath(), new File("erp.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                if (backupCred.exists()) {
                    Files.copy(backupCred.toPath(), new File("credentials.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                JOptionPane.showMessageDialog(this, "Import Successful. Please restart the application.");
                System.exit(0);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Import Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createMaintenancePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(StyleConstants.WHITE);
        p.setBorder(new EmptyBorder(0, 15, 10, 15));
        p.setMaximumSize(new Dimension(250, 60));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        maintenanceBtn = new StyledButton("Maintenance: OFF", Color.ORANGE);
        maintenanceBtn.setPreferredSize(new Dimension(210, 40));
        maintenanceBtn.setForeground(StyleConstants.BLACK);

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
            maintenanceBtn.setText("MAINTAINANCE: ON");
            maintenanceBtn.setBackground(Color.ORANGE);
        } else {
            maintenanceBtn.setText("MAINTAINANCE: OFF");
            maintenanceBtn.setBackground(StyleConstants.ACCENT_COLOR);
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
                    g2.setColor(StyleConstants.DIM_WHITE);
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

            if ("SECTIONS".equals(cardName)) {
                sectionView.refreshData();
            } else if ("INSTRUCTORS".equals(cardName)) {
                instructorView.refreshData();
            }
        });

        return btn;
    }
}