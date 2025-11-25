package Application.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {

    private boolean isHovered = false;
    private boolean isPressed = false;
    private Color primaryColor;

    public StyledButton(String text, Color color) {
        super(text);
        setFont(StyleConstants.BUTTON_FONT);
        setMargin(new Insets(0,0,0,0));
        setForeground(Color.WHITE);

        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        primaryColor = color;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()){
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        setCursor(b ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        if (!isEnabled()) {
            g2.setColor(StyleConstants.DISABLED_COLOR);
        } else if (isPressed) {
            g2.setColor(this.primaryColor.darker());
        } else if (isHovered) {
            g2.setColor(this.primaryColor.brighter());
        } else {
            g2.setColor(this.primaryColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();

        if (!isEnabled()) {
            Color original = getForeground();
            super.setForeground(Color.WHITE);
            super.paintComponent(g);
            super.setForeground(original);
        } else {
            super.paintComponent(g);
        }
    }
}