package Application.Views.AdminViews;

import Application.Components.*;
import Domain.Concretes.Student;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminStudentView extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;

    public AdminStudentView() {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Manage Students");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        StyledButton addBtn = new StyledButton("Add Student", StyleConstants.GREEN);
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> openEditDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0,0,20,0));
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Phone", "Enrolled", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new StyledTable(cols, new Object[][]{});
        table.setModel(model);

        table.getColumnModel().getColumn(5).setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    String id = (String) model.getValueAt(row, 0);
                    try {
                        Student s = new Student(id);
                        openEditDialog(s);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(StyleConstants.WHITE);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    private void refreshData() {
        model.setRowCount(0);
        List<String> ids = new ArrayList<>();

        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM students")) {
            while(rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (String id : ids) {
            try {
                Student s = new Student(id);
                model.addRow(new Object[]{
                        s.getId(), s.getName(), s.getEmail(), s.getPhone(), s.getEnrollmentDate(), "EDIT"
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openEditDialog(Student existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create Student" : "Edit Student", true);
        d.setSize(500, 650);
        d.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(StyleConstants.WHITE);

        JLabel heading = new JLabel(existing == null ? "Create New Student" : "Edit Student");
        heading.setFont(StyleConstants.HEADER_FONT);
        heading.setForeground(StyleConstants.TERTIARY_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledField idF = new StyledField("ID (e.g. STU_001)");
        StyledField nameF = new StyledField("Full Name");
        StyledField emailF = new StyledField("Email Address");
        StyledField phoneF = new StyledField("Phone Number");
        StyledField dateF = new StyledField("Enrollment Date (YYYY-MM-DD)");
        StyledPasswordField passF = new StyledPasswordField("Password");

        Dimension fieldDim = new Dimension(Integer.MAX_VALUE, 45);
        idF.setMaximumSize(fieldDim); nameF.setMaximumSize(fieldDim);
        emailF.setMaximumSize(fieldDim); phoneF.setMaximumSize(fieldDim);
        dateF.setMaximumSize(fieldDim); passF.setMaximumSize(fieldDim);

        if (existing != null) {
            idF.setText(existing.getId()); idF.setEnabled(false);
            nameF.setText(existing.getName());
            emailF.setText(existing.getEmail());
            phoneF.setText(existing.getPhone());
            dateF.setText(existing.getEnrollmentDate());
        }

        mainPanel.add(heading);
        mainPanel.add(Box.createVerticalStrut(30));

        addLabeledField(mainPanel, "Account Identity", idF);
        addLabeledField(mainPanel, "Personal Details", nameF);
//        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(emailF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(phoneF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(dateF);
        mainPanel.add(Box.createVerticalStrut(15));


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
        save.setPreferredSize(new Dimension(140, 40));

        gbc.gridx = 0;
        btnPanel.add(save, gbc);

        if (existing != null) {
            StyledButton deleteBtn = new StyledButton("Delete", StyleConstants.RED);
            deleteBtn.setPreferredSize(new Dimension(100, 40));
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(d,
                        "Are you sure you want to delete student " + existing.getName() + "?\nThis action removes all records.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        existing.onPresistenceDelete();
                        d.dispose();
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Student Deleted.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(d, "Error deleting: " + ex.getMessage());
                    }
                }
            });

            gbc.gridx = 1;
            btnPanel.add(deleteBtn, gbc);
        }

        save.addActionListener(e -> {
            try {
                String pass = new String(passF.getPassword());

                if(idF.getText().trim().isEmpty() || nameF.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(d, "ID and Name are required.");
                    return;
                }

                if (existing == null) {
                    if(pass.isEmpty()) pass = "123";
                    Student s = new Student(idF.getText(), nameF.getText(), dateF.getText(),
                            emailF.getText(), phoneF.getText(), pass);
                    s.onPresistenceSave();
                } else {
                    existing.setName(nameF.getText());
                    existing.setEmail(emailF.getText());
                    existing.setPhone(phoneF.getText());
                    if(!pass.isEmpty()) existing.setPassword(pass);
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
}