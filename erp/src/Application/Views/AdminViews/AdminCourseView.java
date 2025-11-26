package Application.Views.AdminViews;

import Application.Components.*;
import Domain.Concretes.Course;
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

public class AdminCourseView extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;

    public AdminCourseView() {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Manage Courses");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        StyledButton addBtn = new StyledButton("Add Course", StyleConstants.GREEN);
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> openEditDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0,0,20,0));
        add(header, BorderLayout.NORTH);

        String[] cols = {"Code", "Title", "Credits", "Base Capacity", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new StyledTable(cols, new Object[][]{});
        table.setModel(model);

        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    String id = (String) model.getValueAt(row, 0);
                    try {
                        Course c = new Course(id);
                        openEditDialog(c);
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
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM courses")) {
            while(rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (String id : ids) {
            try {
                Course course = new Course(id);
                model.addRow(new Object[]{
                        course.getId(), course.getName(), course.getCredits(), course.getCapacity(), "EDIT"
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openEditDialog(Course existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create Course" : "Edit Course", true);
        d.setSize(450, 500);
        d.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(StyleConstants.WHITE);

        JLabel heading = new JLabel(existing == null ? "Create New Course" : "Edit Course");
        heading.setFont(StyleConstants.HEADER_FONT);
        heading.setForeground(StyleConstants.TERTIARY_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledField codeF = new StyledField("Course Code (e.g. CS101)");
        StyledField titleF = new StyledField("Course Title");
        StyledField creditF = new StyledField("Credits (Integer)");
        StyledField capF = new StyledField("Base Capacity (Integer)");

        Dimension fieldDim = new Dimension(Integer.MAX_VALUE, 45);
        codeF.setMaximumSize(fieldDim); titleF.setMaximumSize(fieldDim);
        creditF.setMaximumSize(fieldDim); capF.setMaximumSize(fieldDim);

        if (existing != null) {
            codeF.setText(existing.getId()); codeF.setEnabled(false);
            titleF.setText(existing.getName());
            creditF.setText(String.valueOf(existing.getCredits()));
            capF.setText(String.valueOf(existing.getCapacity()));
        }

        mainPanel.add(heading);
        mainPanel.add(Box.createVerticalStrut(30));

        addLabeledField(mainPanel, "Course Identity", codeF);
        addLabeledField(mainPanel, "Course Details", titleF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(creditF);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(capF);

        mainPanel.add(Box.createVerticalStrut(25));

        // Responsive Button Panel
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
                        "Are you sure you want to delete course " + existing.getName() + "?\nThis action removes the course definition.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        existing.onPresistenceDelete();
                        d.dispose();
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Course Deleted.");
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
                String code = codeF.getText().trim();
                String titleText = titleF.getText().trim();
                String crStr = creditF.getText().trim();
                String capStr = capF.getText().trim();

                if(code.isEmpty() || titleText.isEmpty() || crStr.isEmpty() || capStr.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "All fields are required.");
                    return;
                }

                int cr = Integer.parseInt(crStr);
                int cap = Integer.parseInt(capStr);

                if (cr < 1 || cap < 1) throw new ArithmeticException("cannot have capacity or credits less than one");

                if (existing == null) {
                    Course c = new Course(code, titleText, cr, cap);
                    c.onPresistenceSave();
                } else {
                    existing.setName(titleText);
                    existing.setCredits(cr);
                    existing.setCapacity(cap);
                    existing.onPresistenceSave();
                }
                d.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Saved Successfully");
            } catch (ArithmeticException ex) {
                JOptionPane.showMessageDialog(d, "Error saving: " + ex.getMessage());
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Credits and Capacity must be integers.");
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