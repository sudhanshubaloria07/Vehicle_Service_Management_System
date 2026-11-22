package service.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Metric card component for displaying key performance indicators (KPIs) on the dashboard.
 * Features an accent top bar, subtitle, and prominent numerical value.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - StatCard extends JPanel
 * OOP CONCEPT: Encapsulation (Unit 1) - Guarded state with public mutators
 */
public class StatCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setPreferredSize(new Dimension(175, 110));

        lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_SMALL);
        lblTitle.setForeground(Theme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(Theme.TEXT_DARK);
        lblValue.setHorizontalAlignment(SwingConstants.LEFT);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(Theme.FONT_SMALL);
        lblSubtitle.setForeground(accentColor);

        JPanel content = new JPanel(new BorderLayout(4, 4));
        content.setOpaque(false);
        content.add(lblTitle, BorderLayout.NORTH);
        content.add(lblValue, BorderLayout.CENTER);
        content.add(lblSubtitle, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    public void setValue(int value) {
        lblValue.setText(String.valueOf(value));
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    /**
     * OOP CONCEPT: Method Overriding (Unit 2)
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // White card background
        g2.setColor(Theme.CARD_WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        // Top accent indicator line
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, 5, 10, 10);

        // Subtle border
        g2.setColor(Theme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        g2.dispose();
        super.paintComponent(g);
    }
}
