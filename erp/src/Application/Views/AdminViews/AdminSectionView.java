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

    // CHANGED FROM PRIVATE TO PUBLIC
    public void refreshData() {
        model.setRowCount(0);
        List<String> ids = new ArrayList<>();

        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
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
        d.setSize(500, 750);
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
        instF.setPreferredSize(new Dimension(200, 40));

        StyledButton assignInstBtn = new StyledButton("Select", StyleConstants.SECONDARY_COLOR);
        assignInstBtn.setPreferredSize(new Dimension(80, 40));
        assignInstBtn.setMaximumSize(new Dimension(80, 40));

        assignInstBtn.addActionListener(e -> {
            String selectedInst = openInstructorSelector(d);
            if (selectedInst != null) {
                instF.setText(selectedInst);
            }
        });

        instrPanel.add(instF);
        instrPanel.add(Box.createHorizontalStrut(10));
        instrPanel.add(assignInstBtn);

        Dimension fieldDim = new Dimension(350, 45);
        idF.setMaximumSize(fieldDim); nameF.setMaximumSize(fieldDim);
        courseCombo.setMaximumSize(fieldDim);
        instrPanel.setMaximumSize(new Dimension(350, 45));
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

        StyledButton save = new StyledButton("Save", StyleConstants.PRIMARY_COLOR);
        save.setPreferredSize(new Dimension(100, 40));

        gbc.gridx = 0;
        btnPanel.add(save, gbc);

        if (existing != null) {
            // Timetable Button
            StyledButton timeBtn = new StyledButton("Timetable", StyleConstants.SECONDARY_COLOR);
            timeBtn.setPreferredSize(new Dimension(100, 40));
            timeBtn.addActionListener(e -> openTimetableManager(existing));
            gbc.gridx = 1;
            btnPanel.add(timeBtn, gbc);

            // Delete Button
            StyledButton deleteBtn = new StyledButton("Delete", StyleConstants.RED);
            deleteBtn.setPreferredSize(new Dimension(100, 40));
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(d,
                        "Are you sure you want to delete section " + existing.getId() + "?\nThis cannot be undone.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        removeFromTeachingTable(existing.getInstructorId(), existing.getId());
                        existing.onPresistenceDelete();
                        d.dispose();
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Section Deleted.");
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
                if (cap < 1) throw new ArithmeticException("Capacity must be > 0");

                String oldInstructorId = (existing != null) ? existing.getInstructorId() : null;

                if (existing == null) {
                    Section s = new Section(sid, name, cid, iid, sem, cap, 0);
                    s.onPresistenceSave();
                    if (!iid.isEmpty() && !iid.equals("Unassigned")) {
                        addToTeachingTable(iid, sid);
                    }
                } else {
                    existing.setName(name);
                    existing.setInstructorId(iid);
                    existing.setSemester(sem);
                    existing.setCapacity(cap);
                    existing.onPresistenceSave();
                    updateSectionCourseId(sid, cid);

                    if (oldInstructorId != null && !oldInstructorId.equals(iid)) {
                        if (!oldInstructorId.isEmpty() && !oldInstructorId.equals("Unassigned")) {
                            removeFromTeachingTable(oldInstructorId, sid);
                        }
                        if (!iid.isEmpty() && !iid.equals("Unassigned")) {
                            addToTeachingTable(iid, sid);
                        }
                    }
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

    private void openTimetableManager(Section section) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Manage Timetable: " + section.getId(), true);
        d.setSize(600, 500);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        String[] cols = {"Day", "Start", "Duration (m)", "Room", "Action"};
        DefaultTableModel tModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        StyledTable tTable = new StyledTable(cols, new Object[][]{});
        tTable.setModel(tModel);

        List<Section.TimeSlot> slots = section.getTimetable();
        for(Section.TimeSlot ts : slots) {
            tModel.addRow(new Object[]{ts.day, ts.startTime, ts.durationMins, ts.room, "REMOVE"});
        }

        tTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tTable.rowAtPoint(e.getPoint());
                int col = tTable.columnAtPoint(e.getPoint());
                if(row >= 0 && col == 4) {
                    slots.remove(row);
                    try {
                        section.updateTimetable(slots, Application.Views.AdminViews.AdminSectionView.this.model == null ? Domain.Abstracts.UserEntity.Permission.PERMISSION_ADMIN : Domain.Abstracts.UserEntity.Permission.PERMISSION_ADMIN);
                        // Note: Passed permission directly from enum in loop logic
                        tModel.removeRow(row);
                    } catch(Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Current Slots"));

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addPanel.setBackground(StyleConstants.WHITE);
        addPanel.setBorder(BorderFactory.createTitledBorder("Add Slot"));

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        StyledComboBox<String> dayBox = new StyledComboBox<>(days);

        StyledField startF = new StyledField("Start (HH:MM)");
        startF.setPreferredSize(new Dimension(100, 40));

        StyledField durF = new StyledField("Mins");
        durF.setPreferredSize(new Dimension(60, 40));

        StyledField roomF = new StyledField("Room");
        roomF.setPreferredSize(new Dimension(80, 40));

        StyledButton addBtn = new StyledButton("Add", StyleConstants.GREEN);
        addBtn.setPreferredSize(new Dimension(70, 40));

        addBtn.addActionListener(e -> {
            try {
                String dStr = (String) dayBox.getSelectedItem();
                String tStr = startF.getText().trim();
                int min = Integer.parseInt(durF.getText().trim());
                String rStr = roomF.getText().trim();

                if(!tStr.matches("\\d{2}:\\d{2}")) throw new Exception("Invalid Time Format (HH:MM)");

                slots.add(new Section.TimeSlot(dStr, tStr, min, rStr));
                section.updateTimetable(slots, Domain.Abstracts.UserEntity.Permission.PERMISSION_ADMIN);

                tModel.setRowCount(0);
                for(Section.TimeSlot ts : slots) {
                    tModel.addRow(new Object[]{ts.day, ts.startTime, ts.durationMins, ts.room, "REMOVE"});
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        addPanel.add(dayBox);
        addPanel.add(startF);
        addPanel.add(durF);
        addPanel.add(roomF);
        addPanel.add(addBtn);

        d.add(scroll, BorderLayout.CENTER);
        d.add(addPanel, BorderLayout.SOUTH);
        d.setVisible(true);
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
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
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
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             ResultSet rs = c.createStatement().executeQuery("SELECT id FROM courses")) {
            while(rs.next()) ids.add(rs.getString("id"));
        } catch (Exception e) { e.printStackTrace(); }
        return ids.toArray(new String[0]);
    }

    private void updateSectionCourseId(String sectionId, String newCourseId) {
        String sql = "UPDATE sections SET course_id = ? WHERE id = ?";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, newCourseId);
            s.setString(2, sectionId);
            s.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addToTeachingTable(String instructorId, String sectionId) {
        String sql = "INSERT INTO teaching(instructor_id, section_id) VALUES(?, ?) " +
                "ON CONFLICT(instructor_id, section_id) DO NOTHING";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, instructorId);
            s.setString(2, sectionId);
            s.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void removeFromTeachingTable(String instructorId, String sectionId) {
        String sql = "DELETE FROM teaching WHERE instructor_id = ? AND section_id = ?";
        try (Connection c = sqliteConnector.connect("jdbc:sqlite:erp.db");
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, instructorId);
            s.setString(2, sectionId);
            s.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}