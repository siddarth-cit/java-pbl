package ui;

import model.QueueTicket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Token Slip Receipt Dialog displayed upon ticket issuance.
 */
public class TokenSlipDialog extends JDialog {

    public TokenSlipDialog(Window owner, QueueTicket ticket, int estimatedWaitMins) {
        super(owner, "Digital Token Receipt", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(380, 520);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(24, 24, 27));
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Bank / Organization Header
        JLabel orgTitle = new JLabel("SMART QUEUE SYSTEM", SwingConstants.CENTER);
        orgTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        orgTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        orgTitle.setForeground(new Color(96, 165, 250));

        JLabel orgSubtitle = new JLabel("Digital Ticket Slip", SwingConstants.CENTER);
        orgSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        orgSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        orgSubtitle.setForeground(new Color(156, 163, 175));

        // Priority Badge if applicable
        JLabel badge;
        if (ticket.isPriority()) {
            badge = ThemeUtils.createBadge("★ VIP PRIORITY TICKET ★", new Color(139, 92, 246), Color.WHITE);
        } else {
            badge = ThemeUtils.createBadge("STANDARD TICKET", new Color(75, 85, 99), Color.WHITE);
        }
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Huge Token Number Display Card
        JPanel tokenBox = new JPanel();
        tokenBox.setBackground(new Color(39, 39, 42));
        tokenBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        tokenBox.setLayout(new BoxLayout(tokenBox, BoxLayout.Y_AXIS));

        JLabel tokenLabel = new JLabel(ticket.getTokenNumber(), SwingConstants.CENTER);
        tokenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tokenLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        tokenLabel.setForeground(ticket.isPriority() ? new Color(245, 158, 11) : Color.WHITE);

        String waitStr;
        if (ticket.isPriority()) {
            waitStr = "Est. Wait: ~1.5 - 2 hrs (Supadham Priority)";
        } else if (estimatedWaitMins >= 1440) {
            waitStr = "Est. Wait: 24 hrs (Max Peak)";
        } else if (estimatedWaitMins >= 60) {
            int hrs = estimatedWaitMins / 60;
            int mins = estimatedWaitMins % 60;
            waitStr = "Est. Wait: ~" + hrs + "h" + (mins > 0 ? (" " + mins + "m") : "");
        } else {
            waitStr = "Est. Wait: " + estimatedWaitMins + " mins";
        }

        JLabel waitEstimate = new JLabel(waitStr, SwingConstants.CENTER);
        waitEstimate.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitEstimate.setFont(new Font("SansSerif", Font.BOLD, 13));
        waitEstimate.setForeground(new Color(52, 211, 153));

        tokenBox.add(tokenLabel);
        tokenBox.add(Box.createVerticalStrut(6));
        tokenBox.add(waitEstimate);

        // Customer & Service Information
        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 8, 8));
        infoGrid.setBackground(new Color(24, 24, 27));
        infoGrid.setBorder(new EmptyBorder(12, 10, 12, 10));

        addGridRow(infoGrid, "Customer Name:", ticket.getCustomerName());
        addGridRow(infoGrid, "Customer ID:", ticket.getCustomerId());
        addGridRow(infoGrid, "Service:", ticket.getServiceType());
        addGridRow(infoGrid, "Issued At:", ticket.getIssuedAt());

        // Simulated Barcode / QR aesthetic
        JLabel barcodeLabel = new JLabel("|||| | ||||| || |||||| | ||||||| ||| |||", SwingConstants.CENTER);
        barcodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        barcodeLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        barcodeLabel.setForeground(new Color(113, 113, 122));

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(24, 24, 27));

        JButton closeBtn = ThemeUtils.createStyledButton("Done", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        JButton printBtn = ThemeUtils.createStyledButton("Print Slip", new Color(55, 65, 81), Color.WHITE);
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Slip sent to virtual receipt printer for Token: " + ticket.getTokenNumber(),
                    "Print Job Sent",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);

        // Assemble
        mainPanel.add(orgTitle);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(orgSubtitle);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(badge);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(tokenBox);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(infoGrid);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(barcodeLabel);
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(buttonPanel);

        setContentPane(mainPanel);
    }

    private void addGridRow(JPanel panel, String title, String val) {
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(new Color(156, 163, 175));

        JLabel v = new JLabel(val);
        v.setFont(new Font("SansSerif", Font.BOLD, 12));
        v.setForeground(Color.WHITE);

        panel.add(t);
        panel.add(v);
    }
}
