package Application.Views.AdminViews;

import Application.Components.*;
import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminInstructorView extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;

    public AdminInstructorView() {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Manage Instructors");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        StyledButton addBtn = new StyledButton("Add Instructor", StyleConstants.GREEN);
        addBtn.setPreferredSize(new Dimension(180, 40));
        addBtn.addActionListener(e -> openEditDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Phone", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new StyledTable(cols, new Object[][]{});
        table.setModel(model);

        table.getColumnModel().getColumn(4).setMinWidth(80);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    String id = (String) model.getValueAt(row, 0);
                    try {
                        Instructor i = new Instructor(id);
                        openEditDialog(i);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(StyleConstants.WHITE);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        List<String> ids = new ArrayList<>();

        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM instructors")) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String id : ids) {
            try {
                Instructor i = new Instructor(id);
                model.addRow(new Object[]{i.getId(), i.getName(), i.getEmail(), i.getPhone(), "EDIT"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openEditDialog(Instructor existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create Instructor" : "Edit Instructor", true);
        d.setSize(550, 700);
        d.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(StyleConstants.WHITE);

        JLabel heading = new JLabel(existing == null ? "Create New Instructor" : "Edit Instructor");
        heading.setFont(StyleConstants.HEADER_FONT);
        heading.setForeground(StyleConstants.TERTIARY_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledField idF = new StyledField("ID (e.g. INST_001)");
        StyledField nameF = new StyledField("Name");
        StyledField emailF = new StyledField("Email");
        StyledField phoneF = new StyledField("Phone");
        StyledPasswordField passF = new StyledPasswordField("Password");

        Dimension maxDim = new Dimension(Integer.MAX_VALUE, 45);
        idF.setMaximumSize(maxDim);
        nameF.setMaximumSize(maxDim);
        emailF.setMaximumSize(maxDim);
        phoneF.setMaximumSize(maxDim);
        passF.setMaximumSize(maxDim);

        if (existing != null) {
            idF.setText(existing.getId());
            idF.setEnabled(false);
            nameF.setText(existing.getName());
            emailF.setText(existing.getEmail());
            phoneF.setText(existing.getPhone());
        }

        mainPanel.add(heading);
        mainPanel.add(Box.createVerticalStrut(25));

        addLabeledField(mainPanel, "Identity", idF);
        addLabeledField(mainPanel, "Personal Details", nameF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(emailF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(phoneF);

        addLabeledField(mainPanel, "Security", passF);

        mainPanel.add(Box.createVerticalStrut(25));

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBackground(StyleConstants.WHITE);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        StyledButton save = new StyledButton("Save Changes", StyleConstants.PRIMARY_COLOR);
        save.setPreferredSize(new Dimension(130, 40));

        gbc.gridx = 0;
        btnPanel.add(save, gbc);

        if (existing != null) {
            StyledButton sectionsBtn = new StyledButton("Edit Sections", StyleConstants.SECONDARY_COLOR);
            sectionsBtn.setPreferredSize(new Dimension(130, 40));
            sectionsBtn.addActionListener(e -> openSectionManager(existing));

            gbc.gridx = 1;
            btnPanel.add(sectionsBtn, gbc);

            StyledButton deleteBtn = new StyledButton("Delete", StyleConstants.RED);
            deleteBtn.setPreferredSize(new Dimension(100, 40));
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(d,
                        "Are you sure you want to delete " + existing.getName() + "?\nThis cannot be undone.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        existing.onPresistenceDelete();
                        d.dispose();
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Instructor Deleted.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(d, "Error deleting: " + ex.getMessage());
                    }
                }
            });

            gbc.gridx = 2;
            btnPanel.add(deleteBtn, gbc);
        }

        save.addActionListener(e -> {
            try {
                String id = idF.getText().trim();
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String phone = phoneF.getText().trim();
                String pass = new String(passF.getPassword());

                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "ID and Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (email.contains(" ")) {
                    JOptionPane.showMessageDialog(d, "Email cannot contain spaces.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!phone.isEmpty() && !phone.matches("\\d+")) {
                    JOptionPane.showMessageDialog(d, "Phone number must contain digits only.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (existing == null) {
                    if (pass.isEmpty()) pass = "123";
                    Instructor i = new Instructor(id, name, email, phone, pass);
                    i.onPresistenceSave();
                } else {
                    existing.setName(name);
                    existing.setEmail(email);
                    existing.setPhone(phone);
                    if (!pass.isEmpty()) existing.setPassword(pass);
                    existing.onPresistenceSave();
                }
                d.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Saved Successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        mainPanel.add(btnPanel);
        d.add(mainPanel);
        d.setVisible(true);
    }

    private void addLabeledField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(StyleConstants.GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setBackground(StyleConstants.WHITE);
        labelPanel.add(label);
        labelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        panel.add(labelPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));
    }

    private void openSectionManager(Instructor instructor) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Managing Sections for " + instructor.getName(), true);
        d.setSize(600, 700);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(StyleConstants.WHITE);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel listLabel = new JLabel("Currently Assigned Sections");
        listLabel.setFont(StyleConstants.HEADER_FONT.deriveFont(18f));
        listLabel.setForeground(StyleConstants.TERTIARY_COLOR);
        listLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        DefaultTableModel secModel = new DefaultTableModel(new String[]{"Section ID", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        StyledTable secTable = new StyledTable(new String[]{"Section ID", "Action"}, new Object[][]{});
        secTable.setModel(secModel);
        secTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        secTable.getColumnModel().getColumn(1).setMaxWidth(100);

        refreshAssignedSections(instructor, secModel);

        secTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = secTable.rowAtPoint(e.getPoint());
                int col = secTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 1) {
                    String secId = (String) secModel.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(d, "Unassign " + secId + "?", "Confirm Unassign", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        unassignSection(instructor, secId, secModel);
                    }
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(secTable);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        listScroll.getViewport().setBackground(StyleConstants.WHITE);
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
        addPanel.setBackground(new Color(245, 245, 250));
        addPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        addPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel addLabel = new JLabel("Assign New Section");
        addLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addLabel.setForeground(StyleConstants.TERTIARY_COLOR);
        addLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel controlsPanel = new JPanel(new BorderLayout(10, 0));
        controlsPanel.setOpaque(false);
        controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledComboBox<String> allSectionsCombo = new StyledComboBox<>(getAllSectionIds());

        StyledButton assignBtn = new StyledButton("Assign Section", StyleConstants.GREEN);
        assignBtn.setPreferredSize(new Dimension(130, 40));
        assignBtn.addActionListener(e -> {
            String selectedSec = (String) allSectionsCombo.getSelectedItem();
            if (selectedSec != null) {
                assignSection(instructor, selectedSec, secModel);
            }
        });

        controlsPanel.add(allSectionsCombo, BorderLayout.CENTER);
        controlsPanel.add(assignBtn, BorderLayout.EAST);

        addPanel.add(addLabel);
        addPanel.add(Box.createVerticalStrut(10));
        addPanel.add(controlsPanel);

        content.add(listLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(listScroll);
        content.add(Box.createVerticalStrut(30));
        content.add(addPanel);

        d.add(content, BorderLayout.CENTER);
        d.setVisible(true);
    }

    // CHANGED: Query sections table instead of teaching table for synchronization
    private void refreshAssignedSections(Instructor instructor, DefaultTableModel model) {
        model.setRowCount(0);
        // OLD: String sql = "SELECT section_id FROM teaching WHERE instructor_id = ?";
        // NEW: Use sections table as source of truth to match AdminSectionView
        String sql = "SELECT id FROM sections WHERE instructor_id = ?";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, instructor.getId());
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("id"), "UNASSIGN"});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignSection(Instructor instructor, String sectionId, DefaultTableModel model) {
        try {
            // 1. Update teaching table (keep for redundancy/legacy if needed, or purely rely on sections)
            instructor.assignToSection(sectionId);

            // 2. Update sections table (Source of Truth)
            Section sec = new Section(sectionId);
            sec.setInstructorId(instructor.getId());
            sec.onPresistenceSave();

            refreshAssignedSections(instructor, model);
            JOptionPane.showMessageDialog(null, "Assigned " + sectionId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void unassignSection(Instructor instructor, String sectionId, DefaultTableModel model) {
        // 1. Remove from teaching table
        String sql = "DELETE FROM teaching WHERE instructor_id = ? AND section_id = ?";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, instructor.getId());
            s.setString(2, sectionId);
            s.executeUpdate();

            // 2. Update sections table (Source of Truth)
            Section sec = new Section(sectionId);
            if (sec.getInstructorId() != null && sec.getInstructorId().equals(instructor.getId())) {
                sec.setInstructorId("Unassigned");
                sec.onPresistenceSave();
            }

            refreshAssignedSections(instructor, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getAllSectionIds() {
        List<String> list = new ArrayList<>();
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM sections")) {
            while (rs.next()) list.add(rs.getString("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new String[0]);
    }
}