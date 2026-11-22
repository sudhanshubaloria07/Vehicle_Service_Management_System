package service.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Design tokens, color palette, typography hierarchies, and custom Java2D UI
 * rendering helpers for a modern, automotive service-centre dashboard.
 * 
 * OOP CONCEPT: Encapsulation (Unit 1) - Centralized UI styling constants
 */
public class Theme {

    // Modern Navy & Blue Accent Color Palette
    public static final Color NAVY_DARK    = new Color(0x0B, 0x1F, 0x3A); // Sidebar background #0B1F3A
    public static final Color NAVY         = new Color(0x12, 0x29, 0x4D); // Header / Cards #12294D
    public static final Color BLUE_ACCENT  = new Color(0x2F, 0x80, 0xED); // Primary accent #2F80ED
    public static final Color BLUE_LIGHT   = new Color(0xE8, 0xF0, 0xFE); // Light active tint #E8F0FE
    public static final Color BACKGROUND   = new Color(0xF4, 0xF7, 0xFB); // Window background #F4F7FB
    public static final Color CARD_WHITE   = new Color(0xFF, 0xFF, 0xFF); // Card surface #FFFFFF
    public static final Color TEXT_DARK    = new Color(0x1B, 0x2A, 0x41); // Primary text #1B2A41
    public static final Color TEXT_MUTED   = new Color(0x6B, 0x7A, 0x90); // Secondary text #6B7A90
    public static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0); // Subtle borders #E2E8F0
    public static final Color ROW_ALT      = new Color(0xF8, 0xFA, 0xFC); // Alternating table rows

    // Functional State Colors
    public static final Color SUCCESS      = new Color(0x27, 0xAE, 0x60); // Green #27AE60
    public static final Color WARNING      = new Color(0xF2, 0x99, 0x4A); // Orange #F2994A
    public static final Color DANGER       = new Color(0xEB, 0x57, 0x57); // Red #EB5757

    // Consistent Typography Hierarchy (Segoe UI with fallbacks)
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD= new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 13);

    private Theme() {}

    /**
     * Creates a rounded card panel with soft shadow / border styling.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return card;
    }

    /**
     * Creates a custom painted rounded button with antialiasing and hover effect.
     */
    public static JButton createCustomButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createCustomButton(text, BLUE_ACCENT, new Color(0x23, 0x6E, 0xD1), Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createCustomButton(text, SUCCESS, new Color(0x21, 0x96, 0x53), Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createCustomButton(text, DANGER, new Color(0xC0, 0x39, 0x2B), Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createCustomButton(text, new Color(0x6B, 0x7A, 0x90), new Color(0x56, 0x63, 0x75), Color.WHITE);
    }

    /**
     * Styles a JTable with professional header, row height, alternating row colors,
     * and sortable columns.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(BLUE_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBTITLE);
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setOpaque(true);

        // Enable sorting
        table.setAutoCreateRowSorter(true);

        // Center default cell renderer with alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    c.setForeground(TEXT_DARK);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    /**
     * Custom cell renderer for Job Status: Pending (Orange), In Progress (Blue), Completed (Green).
     */
    public static DefaultTableCellRenderer createStatusCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BODY_BOLD);
                String status = (value != null) ? value.toString() : "";

                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    if ("Completed".equalsIgnoreCase(status)) {
                        lbl.setForeground(SUCCESS);
                    } else if ("In Progress".equalsIgnoreCase(status)) {
                        lbl.setForeground(BLUE_ACCENT);
                    } else {
                        lbl.setForeground(WARNING);
                    }
                }
                return lbl;
            }
        };
    }
}
