package Application.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledTable extends JTable {

    private int hoverRow = -1;

    public StyledTable(String[] columns, Object[][] data) {
        super(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        setFillsViewportHeight(true);
        setRowHeight(40);
        setFont(StyleConstants.NORMAL_FONT);

        setSelectionBackground(StyleConstants.DIM_WHITE);
        setSelectionForeground(StyleConstants.ACCENT_COLOR);

        setShowVerticalLines(false);
        setShowHorizontalLines(false);
        setIntercellSpacing(new Dimension(0, 0));

        setBackground(StyleConstants.WHITE);

        JTableHeader header = getTableHeader();
        header.setBackground(StyleConstants.ACCENT_COLOR);
        header.setForeground(StyleConstants.ACCENT_COLOR);
        header.setFont(StyleConstants.BUTTON_FONT);
        header.setPreferredSize(new Dimension(0, 45));

        ((JComponent) header.getDefaultRenderer()).setBorder(new EmptyBorder(0, 15, 0, 15));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                repaint();
            }
        });

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setBorder(new EmptyBorder(0, 15, 0, 15));

                if (!isSelected) {
                    if (row == hoverRow && table.isEnabled()) {
                        c.setBackground(StyleConstants.DIM_WHITE);
                    } else {
                        c.setBackground(StyleConstants.WHITE);
                    }
                    c.setForeground(StyleConstants.TEXT_COLOR);
                }

                if (!table.isEnabled()) {
                    c.setForeground(StyleConstants.DISABLED_COLOR);
                    c.setBackground(StyleConstants.WHITE);
                }

                return c;
            }
        });
    }
}