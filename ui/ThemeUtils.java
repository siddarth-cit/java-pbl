package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Modern UI theme configurations and custom Swing component builders.
 * Configures FlatLaf or native system look and feel.
 */
public class ThemeUtils {

    // Color Palette
    public static final Color BG_DARK = new Color(17, 24, 39);          // #111827
    public static final Color CARD_BG = new Color(31, 41, 55);          // #1F2937
    public static final Color CARD_BG_LIGHT = new Color(243, 244, 246);  // #F3F4F6
    public static final Color BORDER_COLOR = new Color(55, 65, 81);     // #374151
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);    // #3B82F6
    public static final Color ACCENT_HOVER = new Color(37, 99, 235);   // #2563EB
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);  // #10B981
    public static final Color WARNING_AMBER = new Color(245, 158, 11);  // #F59E0B
    public static final Color DANGER_RED = new Color(239, 68, 68);      // #EF4444
    public static final Color PURPLE_VIP = new Color(139, 92, 246);     // #8B5CF6
    public static final Color TEXT_MAIN = new Color(243, 244, 246);     // #F3F4F6
    public static final Color TEXT_MUTED = new Color(156, 163, 175);    // #9CA3AF

    public static void applyTheme(boolean dark) {
        try {
            if (dark) {
                // Try FlatLaf FlatDarkLaf
                Class<?> clazz = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
                clazz.getMethod("setup").invoke(null);
            } else {
                Class<?> clazz = Class.forName("com.formdev.flatlaf.FlatLightLaf");
                clazz.getMethod("setup").invoke(null);
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Set global UI defaults
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 8);
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        return btn;
    }

    public static JLabel createHeaderLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        label.setForeground(TEXT_MAIN);
        return label;
    }

    public static JLabel createMutedLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setForeground(fg);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        return badge;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(55, 65, 81, 100));
        table.setSelectionBackground(new Color(59, 130, 246, 60));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Center cell renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i == 1 || i == 5 || i == 6) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }
}
