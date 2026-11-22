package service.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Custom navigation button designed for the sidebar.
 * Displays an active-state indicator bar on the left edge and changes background on hover.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - SidebarButton extends JButton
 * OOP CONCEPT: Method Overriding (Unit 2) - Overrides paintComponent()
 */
public class SidebarButton extends JButton {

    private boolean active = false;

    private static final Color BG_DEFAULT = Theme.NAVY_DARK;
    private static final Color BG_HOVER   = new Color(0x16, 0x2E, 0x52);
    private static final Color BG_ACTIVE  = new Color(0x1E, 0x3B, 0x66);
    private static final Color ACCENT_BAR = Theme.BLUE_ACCENT;

    public SidebarButton(String text) {
        super(text);
        setFont(Theme.FONT_SUBTITLE);
        setForeground(new Color(0xD6, 0xE2, 0xF0));
        setHorizontalAlignment(SwingConstants.LEFT);
        setMargin(new Insets(10, 20, 10, 10));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(240, 48));
        setMaximumSize(new Dimension(240, 48));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setForeground(Color.WHITE);
        } else {
            setForeground(new Color(0xD6, 0xE2, 0xF0));
        }
        repaint();
    }

    /**
     * OOP CONCEPT: Method Overriding (Unit 2)
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine background color
        if (active) {
            g2.setColor(BG_ACTIVE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Active vertical accent stripe on left edge
            g2.setColor(ACCENT_BAR);
            g2.fillRect(0, 0, 5, getHeight());
        } else if (getModel().isRollover()) {
            g2.setColor(BG_HOVER);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.setColor(BG_DEFAULT);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
