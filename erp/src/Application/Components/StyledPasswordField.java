package Application.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class StyledPasswordField extends JPasswordField {

    private String placeholder;
    private boolean isFocused = false;

    public StyledPasswordField(String placeholder) {
        this.placeholder = placeholder;

        setOpaque(false);
        setBorder(new EmptyBorder(4, 15, 4, 15));
        setFont(StyleConstants.BUTTON_FONT.deriveFont(Font.PLAIN));
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setEchoChar('•'); // Standard masking character

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Background
        g2.setColor(getBackground());
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

        super.paintComponent(g);

        // Placeholder (Only show if password is empty)
        if (getPassword().length == 0 && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }

        if (isFocused) {
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(2.0f));
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1.0f));
        }

        float strokeWidth = isFocused ? 2.0f : 1.0f;
        float inset = strokeWidth / 2.0f;

        g2.draw(new java.awt.geom.RoundRectangle2D.Float(
                inset,
                inset,
                getWidth() - strokeWidth,
                getHeight() - strokeWidth,
                10,
                10
        ));

        g2.dispose();
    }
}