package Application.Views.AdminViews;

import Application.Components.*;
import Domain.Concretes.Course;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminSectionView extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;

    public AdminSectionView() {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Manage Sections");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);

        StyledButton addBtn = new StyledButton("Add Section", StyleConstants.GREEN);
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> openEditDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0,0,20,0));
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Course ID", "Instructor ID", "Semester", "Cap", "Enrolled", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new StyledTable(cols, new Object[][]{});
        table.setModel(model);

        table.getColumnModel().getColumn(7).setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) {
                    String id = (String) model.getValueAt(row, 0);
                    try {
                        Section s = new Section(id);
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

        try (Connection c = sqliteConnector.connect("jdbc:sqlite:sections.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM sections")) {
            while(rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (String id : ids) {
            try {
                Section s = new Section(id);
                model.addRow(new Object[]{
                        s.getId(), s.getName(), s.getCourseId(), s.getInstructorId(),
                        s.getSemester(), s.getCapacity(), s.getContains(), "EDIT"
                });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void openEditDialog(Section existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create Section" : "Edit Section", true);
        d.setSize(450, 700);
        d.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(StyleConstants.WHITE);

        JLabel heading = new JLabel(existing == null ? "Create New Section" : "Edit Section");
        heading.setFont(StyleConstants.HEADER_FONT);
        heading.setForeground(StyleConstants.TERTIARY_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledField idF = new StyledField("Section ID (e.g. SEC_CS101_A)");
        StyledField nameF = new StyledField("Section Name");
        StyledField semF = new StyledField("Semester (e.g. FALL_2025)");
        StyledField capF = new StyledField("Capacity");

        String[] courses = getAllCourseIds();
        StyledComboBox<String> courseCombo = new StyledComboBox<>(courses);

        JPanel instrPanel = new JPanel();
        instrPanel.setLayout(new BoxLayout(instrPanel, BoxLayout.X_AXIS));
        instrPanel.setOpaque(false);

        StyledField instF = new StyledField("Instructor ID");
        instF.setEditable(false);
        instF.setPreferredSize(new Dimension(200, 40)); // Give field specific size

        StyledButton assignInstBtn = new StyledButton("Select", StyleConstants.SECONDARY_COLOR);
        assignInstBtn.setPreferredSize(new Dimension(80, 40));
        assignInstBtn.setMaximumSize(new Dimension(80, 40)); // Lock button size

        assignInstBtn.addActionListener(e -> {
            String selectedInst = openInstructorSelector(d);
            if (selectedInst != null) {
                instF.setText(selectedInst);
            }
        });

        instrPanel.add(instF);
        instrPanel.add(Box.createHorizontalStrut(10)); // Spacing
        instrPanel.add(assignInstBtn);

        Dimension fieldDim = new Dimension(350, 45);
        idF.setMaximumSize(fieldDim); nameF.setMaximumSize(fieldDim);
        courseCombo.setMaximumSize(fieldDim);
        instrPanel.setMaximumSize(new Dimension(350, 45)); // Constrain panel height
        semF.setMaximumSize(fieldDim); capF.setMaximumSize(fieldDim);

        if (existing != null) {
            idF.setText(existing.getId()); idF.setEnabled(false);
            nameF.setText(existing.getName());
            courseCombo.setSelectedItem(existing.getCourseId());
            instF.setText(existing.getInstructorId());
            semF.setText(existing.getSemester());
            capF.setText(String.valueOf(existing.getCapacity()));
        }

        mainPanel.add(heading);
        mainPanel.add(Box.createVerticalStrut(30));

        addLabeledComponent(mainPanel, "Section Identity", idF);
        addLabeledComponent(mainPanel, "Details", nameF);
        addLabeledComponent(mainPanel, "Course", courseCombo);
        addLabeledComponent(mainPanel, "Assigned Instructor", instrPanel);
        addLabeledComponent(mainPanel, "Semester", semF);
        addLabeledComponent(mainPanel, "Capacity", capF);

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
                        "Are you sure you want to delete section " + existing.getId() + "?\nThis cannot be undone.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        existing.onPresistenceDelete();
                        d.dispose();
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Section Deleted.");
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
                String sid = idF.getText().trim();
                String name = nameF.getText().trim();
                String cid = (String) courseCombo.getSelectedItem();
                String iid = instF.getText().trim();
                String sem = semF.getText().trim();
                String capStr = capF.getText().trim();

                if(sid.isEmpty() || name.isEmpty() || cid == null || sem.isEmpty() || capStr.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "All fields except Instructor are required.");
                    return;
                }

                int cap = Integer.parseInt(capStr);

                if (cap < 1)
                    throw new ArithmeticException("capacity must be greater than 0.");

                if (existing == null) {
                    Section s = new Section(sid, name, cid, iid, sem, cap, 0);
                    s.onPresistenceSave();
                } else {
                    existing.setName(name);
                    existing.setInstructorId(iid);
                    existing.setSemester(sem);
                    existing.setCapacity(cap);
                    existing.onPresistenceSave();

                    updateSectionCourseId(sid, cid);
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

    private void addLabeledComponent(JPanel panel, String labelText, JComponent comp) {
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
        panel.add(comp);
        panel.add(Box.createVerticalStrut(15));
    }

    private String openInstructorSelector(JDialog parent) {
        JDialog selector = new JDialog(parent, "Select Instructor", true);
        selector.setSize(400, 500);
        selector.setLocationRelativeTo(parent);
        selector.setLayout(new BorderLayout());

        String[] cols = {"ID", "Name"};
        DefaultTableModel iModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        StyledTable iTable = new StyledTable(cols, new Object[][]{});
        iTable.setModel(iModel);

        List<String> loadedIds = new ArrayList<>();
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:instructors.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM instructors")) {
            while(rs.next()) loadedIds.add(rs.getString("id"));
        } catch (Exception e) { e.printStackTrace(); }

        for(String id : loadedIds) {
            try {
                Instructor inst = new Instructor(id);
                iModel.addRow(new Object[]{inst.getId(), inst.getName()});
            } catch(Exception ignored){}
        }

        final String[] selectedResult = {null};
        StyledButton selectBtn = new StyledButton("Confirm Selection", StyleConstants.PRIMARY_COLOR);
        selectBtn.setEnabled(false);

        iTable.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                selectBtn.setEnabled(iTable.getSelectedRow() != -1);
            }
        });

        selectBtn.addActionListener(e -> {
            int row = iTable.getSelectedRow();
            if(row != -1) {
                selectedResult[0] = (String) iModel.getValueAt(row, 0);
                selector.dispose();
            }
        });

        selector.add(new JScrollPane(iTable), BorderLayout.CENTER);
        selector.add(selectBtn, BorderLayout.SOUTH);
        selector.setVisible(true);

        return selectedResult[0];
    }

    private String[] getAllCourseIds() {
        List<String> ids = new ArrayList<>();
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:courses.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM courses")) {
            while(rs.next()) ids.add(rs.getString("id"));
        } catch (Exception e) { e.printStackTrace(); }
        return ids.toArray(new String[0]);
    }

    private void updateSectionCourseId(String sectionId, String newCourseId) {
        String sql = "UPDATE sections SET course_id = ? WHERE id = ?";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:sections.db");
             java.sql.PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, newCourseId);
            s.setString(2, sectionId);
            s.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}