package ui;

import db.DatabaseManager;
import model.Customer;
import model.QueueStatistics;
import model.QueueTicket;
import service.QueueService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main graphical dashboard for Smart Digital Queue Management System.
 * Supports dual operating modes:
 *  1) Standard Corporate / Banking Mode
 *  2) Tirumala Tirupati Devasthanams (TTD) Pilgrimage Queue Demo Mode
 */
public class QueueDashboardFrame extends JFrame {

    private final QueueService queueService;

    // Mode Toggle State
    private boolean isTirupatiMode = false;
    private JToggleButton tirupatiToggleBtn;
    private JLabel modeBadge;
    private JLabel brandIcon;
    private JLabel mainTitle;
    private JLabel subTitle;

    // Header & Metric Labels
    private JLabel clockLabel;
    private JLabel statTotalCustomersLabel;
    private JLabel statWaitingNormalLabel;
    private JLabel statWaitingPriorityLabel;
    private JLabel statServingLabel;
    private JLabel statCompletedLabel;
    private JLabel statEstimatedWaitLabel;

    private JLabel card1Title;
    private JLabel card2Title;
    private JLabel card3Title;
    private JLabel card4Title;
    private JLabel card5Title;
    private JLabel card6Title;

    // Calling Counter Elements
    private JLabel screenTitle;
    private JComboBox<String> counterDropdown;
    private JLabel servingTokenLabel;
    private JLabel servingCustomerNameLabel;
    private JLabel servingServiceLabel;
    private JLabel servingCounterLabel;
    private JLabel servingStatusBadge;
    private DefaultListModel<String> upcomingListModel;

    // Registration Form Inputs
    private JLabel newCustTitle;
    private JTextField regNameField;
    private JSpinner regAgeSpinner;
    private JTextField regPhoneField;
    private JComboBox<String> regServiceCombo;
    private JCheckBox regPriorityCheck;
    private JComboBox<String> existingCustomerCombo;

    // Table Models
    private DefaultTableModel queueTableModel;
    private JTable queueTable;
    private DefaultTableModel customerTableModel;
    private JTable customerTable;
    private JTextField customerSearchField;

    // Filter state for queue table
    private String currentQueueFilter = "ALL";

    public QueueDashboardFrame() {
        this.queueService = QueueService.getInstance();

        setTitle("Smart Digital Queue Management System - Enterprise Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 800);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        initComponents();
        startClockAndAutoRefresh();
        refreshAllData();
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBackground(ThemeUtils.BG_DARK);
        rootPanel.setBorder(new EmptyBorder(12, 16, 16, 16));

        // 1. Top Header with Tirupati Demo Toggle
        rootPanel.add(buildHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content: Metrics Row + Tabbed Workspace
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setBackground(ThemeUtils.BG_DARK);
        centerContainer.add(buildMetricsPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.addTab("  📢 Calling Station & Live Display  ", buildCallingStationTab());
        tabbedPane.addTab("  🎫 Token Dispenser & Registration  ", buildRegistrationTab());
        tabbedPane.addTab("  📋 Live Queue Monitor  ", buildQueueMonitorTab());
        tabbedPane.addTab("  👥 Customer / Devotee Directory  ", buildCustomerDirectoryTab());
        tabbedPane.addTab("  ⚙️ System Tools & SQL Database  ", buildSystemToolsTab());

        centerContainer.add(tabbedPane, BorderLayout.CENTER);
        rootPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtils.CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Left: Title & Brand
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBox.setOpaque(false);

        brandIcon = new JLabel("⚡");
        brandIcon.setFont(new Font("SansSerif", Font.PLAIN, 26));

        JPanel textTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        textTitles.setOpaque(false);
        mainTitle = new JLabel("SMART DIGITAL QUEUE MANAGEMENT");
        mainTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        mainTitle.setForeground(ThemeUtils.TEXT_MAIN);

        subTitle = new JLabel("Enterprise Flow Optimization • JDBC & SQL Persistent");
        subTitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subTitle.setForeground(ThemeUtils.TEXT_MUTED);

        textTitles.add(mainTitle);
        textTitles.add(subTitle);
        titleBox.add(brandIcon);
        titleBox.add(textTitles);

        // Center: Tirupati (TTD) Use Case Toggle Switch
        JPanel centerToggleBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerToggleBox.setOpaque(false);

        modeBadge = ThemeUtils.createBadge("🏛️ CORPORATE / BANK MODE", new Color(30, 41, 59), new Color(148, 163, 184));

        tirupatiToggleBtn = new JToggleButton("🛕 Switch to Tirupati (TTD) Demo");
        tirupatiToggleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        tirupatiToggleBtn.setFocusPainted(false);
        tirupatiToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tirupatiToggleBtn.setBackground(new Color(45, 55, 72));
        tirupatiToggleBtn.setForeground(Color.WHITE);
        tirupatiToggleBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 119, 6), 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        tirupatiToggleBtn.addActionListener(e -> toggleTirupatiMode());

        centerToggleBox.add(modeBadge);
        centerToggleBox.add(tirupatiToggleBtn);

        // Right Info: DB Badge + Live Clock + Refresh Button
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBox.setOpaque(false);

        String dbType = DatabaseManager.getInstance().getDatabaseType();
        JLabel dbBadge = ThemeUtils.createBadge("● " + dbType + " JDBC Active", new Color(6, 95, 70), new Color(110, 231, 183));

        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        clockLabel.setForeground(ThemeUtils.TEXT_MAIN);

        JButton refreshBtn = ThemeUtils.createStyledButton("🔄 Refresh", new Color(55, 65, 81), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAllData());

        rightBox.add(dbBadge);
        rightBox.add(clockLabel);
        rightBox.add(refreshBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(centerToggleBox, BorderLayout.CENTER);
        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMetricsPanel() {
        JPanel metrics = new JPanel(new GridLayout(1, 6, 10, 0));
        metrics.setOpaque(false);

        statTotalCustomersLabel = new JLabel("0", SwingConstants.CENTER);
        statWaitingNormalLabel = new JLabel("0", SwingConstants.CENTER);
        statWaitingPriorityLabel = new JLabel("0", SwingConstants.CENTER);
        statServingLabel = new JLabel("0", SwingConstants.CENTER);
        statCompletedLabel = new JLabel("0", SwingConstants.CENTER);
        statEstimatedWaitLabel = new JLabel("0m", SwingConstants.CENTER);

        card1Title = new JLabel("👥 Total Customers");
        card2Title = new JLabel("⏳ Normal Wait");
        card3Title = new JLabel("⭐ Priority (VIP) Wait");
        card4Title = new JLabel("🔔 Currently Serving");
        card5Title = new JLabel("✅ Completed Today");
        card6Title = new JLabel("⏱️ Est. Waiting Time");

        metrics.add(createMetricCard(card1Title, statTotalCustomersLabel, new Color(59, 130, 246)));
        metrics.add(createMetricCard(card2Title, statWaitingNormalLabel, new Color(96, 165, 250)));
        metrics.add(createMetricCard(card3Title, statWaitingPriorityLabel, ThemeUtils.WARNING_AMBER));
        metrics.add(createMetricCard(card4Title, statServingLabel, ThemeUtils.PURPLE_VIP));
        metrics.add(createMetricCard(card5Title, statCompletedLabel, ThemeUtils.SUCCESS_GREEN));
        metrics.add(createMetricCard(card6Title, statEstimatedWaitLabel, new Color(244, 114, 182)));

        return metrics;
    }

    private JPanel createMetricCard(JLabel titleLbl, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(ThemeUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeUtils.BORDER_COLOR, 1, false),
                        new EmptyBorder(8, 12, 8, 12)
                )
        ));

        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLbl.setForeground(ThemeUtils.TEXT_MUTED);

        valLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valLabel.setForeground(ThemeUtils.TEXT_MAIN);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    // TAB 1: CALLING STATION
    private JPanel buildCallingStationTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(ThemeUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Left: Big Digital Display Screen
        JPanel displayScreen = ThemeUtils.createCardPanel();
        displayScreen.setLayout(new BoxLayout(displayScreen, BoxLayout.Y_AXIS));

        screenTitle = new JLabel("PUBLIC DISPLAY MONITOR", SwingConstants.CENTER);
        screenTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        screenTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        screenTitle.setForeground(ThemeUtils.TEXT_MUTED);

        JLabel nowServingTag = new JLabel("NOW SERVING", SwingConstants.CENTER);
        nowServingTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        nowServingTag.setFont(new Font("SansSerif", Font.BOLD, 20));
        nowServingTag.setForeground(new Color(96, 165, 250));

        servingTokenLabel = new JLabel("---", SwingConstants.CENTER);
        servingTokenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        servingTokenLabel.setFont(new Font("SansSerif", Font.BOLD, 72));
        servingTokenLabel.setForeground(ThemeUtils.WARNING_AMBER);

        servingStatusBadge = ThemeUtils.createBadge("STANDBY", new Color(55, 65, 81), Color.WHITE);
        servingStatusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        servingCustomerNameLabel = new JLabel("No Customer Active", SwingConstants.CENTER);
        servingCustomerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        servingCustomerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        servingCustomerNameLabel.setForeground(ThemeUtils.TEXT_MAIN);

        servingServiceLabel = new JLabel("Service: -", SwingConstants.CENTER);
        servingServiceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        servingServiceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        servingServiceLabel.setForeground(ThemeUtils.TEXT_MUTED);

        servingCounterLabel = new JLabel("Please proceed to: Counter 1", SwingConstants.CENTER);
        servingCounterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        servingCounterLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        servingCounterLabel.setForeground(ThemeUtils.SUCCESS_GREEN);

        displayScreen.add(screenTitle);
        displayScreen.add(Box.createVerticalStrut(14));
        displayScreen.add(nowServingTag);
        displayScreen.add(Box.createVerticalStrut(10));
        displayScreen.add(servingTokenLabel);
        displayScreen.add(Box.createVerticalStrut(10));
        displayScreen.add(servingStatusBadge);
        displayScreen.add(Box.createVerticalStrut(18));
        displayScreen.add(servingCustomerNameLabel);
        displayScreen.add(Box.createVerticalStrut(6));
        displayScreen.add(servingServiceLabel);
        displayScreen.add(Box.createVerticalStrut(8));
        displayScreen.add(servingCounterLabel);
        displayScreen.add(Box.createVerticalGlue());

        // Right: Staff Operator Controls & Next in Line
        JPanel controlSide = new JPanel(new BorderLayout(0, 12));
        controlSide.setOpaque(false);
        controlSide.setPreferredSize(new Dimension(380, 0));

        JPanel operatorCard = ThemeUtils.createCardPanel();
        operatorCard.setLayout(new BoxLayout(operatorCard, BoxLayout.Y_AXIS));

        JLabel opTitle = ThemeUtils.createHeaderLabel("Counter Staff Controls", 15);
        opTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel counterSelectLbl = ThemeUtils.createMutedLabel("Select Operating Counter / Gate:", 12);
        counterSelectLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] counters = {
                "Counter 1 - General Banking",
                "Counter 2 - Cashier & Deposits",
                "Counter 3 - Loans & Mortgages",
                "Counter 4 - Priority & VIP Desk"
        };
        counterDropdown = new JComboBox<>(counters);
        counterDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        counterDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Control Buttons
        JButton callNextBtn = ThemeUtils.createStyledButton("📢 Call Next Devotee / Customer", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        callNextBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        callNextBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        callNextBtn.addActionListener(e -> handleCallNext());

        JButton recallBtn = ThemeUtils.createStyledButton("🔁 Re-Announce Customer", new Color(75, 85, 99), Color.WHITE);
        recallBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        recallBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        recallBtn.addActionListener(e -> handleRecall());

        JButton completeBtn = ThemeUtils.createStyledButton("✅ Mark as Completed", ThemeUtils.SUCCESS_GREEN, Color.WHITE);
        completeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        completeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeBtn.addActionListener(e -> handleCompleteServing());

        JButton cancelBtn = ThemeUtils.createStyledButton("⏭️ Skip / No-Show", ThemeUtils.DANGER_RED, Color.WHITE);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.addActionListener(e -> handleCancelServing());

        operatorCard.add(opTitle);
        operatorCard.add(Box.createVerticalStrut(12));
        operatorCard.add(counterSelectLbl);
        operatorCard.add(Box.createVerticalStrut(4));
        operatorCard.add(counterDropdown);
        operatorCard.add(Box.createVerticalStrut(16));
        operatorCard.add(callNextBtn);
        operatorCard.add(Box.createVerticalStrut(8));
        operatorCard.add(recallBtn);
        operatorCard.add(Box.createVerticalStrut(8));
        operatorCard.add(completeBtn);
        operatorCard.add(Box.createVerticalStrut(8));
        operatorCard.add(cancelBtn);

        // Upcoming waiting list
        JPanel upcomingCard = ThemeUtils.createCardPanel();
        upcomingCard.setLayout(new BorderLayout(0, 8));
        JLabel upcomingTitle = ThemeUtils.createHeaderLabel("Next In Line (Priority First):", 13);
        upcomingListModel = new DefaultListModel<>();
        JList<String> upcomingList = new JList<>(upcomingListModel);
        upcomingList.setBackground(new Color(24, 24, 27));
        upcomingList.setForeground(Color.WHITE);
        upcomingList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        upcomingList.setBorder(new EmptyBorder(6, 6, 6, 6));

        upcomingCard.add(upcomingTitle, BorderLayout.NORTH);
        upcomingCard.add(new JScrollPane(upcomingList), BorderLayout.CENTER);

        controlSide.add(operatorCard, BorderLayout.NORTH);
        controlSide.add(upcomingCard, BorderLayout.CENTER);

        panel.add(displayScreen, BorderLayout.CENTER);
        panel.add(controlSide, BorderLayout.EAST);
        return panel;
    }

    // TAB 2: TOKEN DISPENSER & REGISTRATION
    private JPanel buildRegistrationTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(ThemeUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Form 1: New Registration & Instant Token
        JPanel newCustCard = ThemeUtils.createCardPanel();
        newCustCard.setLayout(new BoxLayout(newCustCard, BoxLayout.Y_AXIS));

        newCustTitle = ThemeUtils.createHeaderLabel("Register New Customer & Issue Token", 16);
        newCustTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        regNameField = new JTextField();
        regAgeSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 120, 1));
        regPhoneField = new JTextField();
        regServiceCombo = new JComboBox<>(new String[]{
                "General Banking Inquiry",
                "Cash Deposit & Withdrawal",
                "Loan & Mortgage Consultation",
                "Credit Card & Account Opening",
                "Wealth Management (VIP)",
                "Support & Dispute Resolution"
        });
        regPriorityCheck = new JCheckBox("Priority Customer (Senior Citizen / VIP / Special Assistance)");
        regPriorityCheck.setFont(new Font("SansSerif", Font.BOLD, 12));
        regPriorityCheck.setForeground(ThemeUtils.WARNING_AMBER);
        regPriorityCheck.setOpaque(false);

        JButton registerAndIssueBtn = ThemeUtils.createStyledButton("⚡ Register & Dispense Token Slip", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        registerAndIssueBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        registerAndIssueBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerAndIssueBtn.addActionListener(e -> handleRegisterNewCustomer());

        newCustCard.add(newCustTitle);
        newCustCard.add(Box.createVerticalStrut(14));
        addFormField(newCustCard, "Full Name *:", regNameField);
        addFormField(newCustCard, "Age *:", regAgeSpinner);
        addFormField(newCustCard, "Phone Number *:", regPhoneField);
        addFormField(newCustCard, "Service / Darshan Category *:", regServiceCombo);
        newCustCard.add(Box.createVerticalStrut(6));
        newCustCard.add(regPriorityCheck);
        newCustCard.add(Box.createVerticalStrut(18));
        newCustCard.add(registerAndIssueBtn);
        newCustCard.add(Box.createVerticalGlue());

        // Form 2: Existing Profile Fast Token Dispenser
        JPanel existCard = ThemeUtils.createCardPanel();
        existCard.setLayout(new BoxLayout(existCard, BoxLayout.Y_AXIS));

        JLabel existTitle = ThemeUtils.createHeaderLabel("Existing Profile - Fast Token", 16);
        existTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel existDesc = ThemeUtils.createMutedLabel("Select an existing registered profile to issue a new queue ticket:", 12);
        existDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        existingCustomerCombo = new JComboBox<>();
        existingCustomerCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        existingCustomerCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton issueExistingBtn = ThemeUtils.createStyledButton("🎫 Dispense Token for Selected", ThemeUtils.SUCCESS_GREEN, Color.WHITE);
        issueExistingBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        issueExistingBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        issueExistingBtn.addActionListener(e -> handleIssueExistingCustomerToken());

        // Quick Guidelines Box
        JPanel guideBox = new JPanel();
        guideBox.setLayout(new BoxLayout(guideBox, BoxLayout.Y_AXIS));
        guideBox.setBackground(new Color(24, 24, 27));
        guideBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        guideBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel g1 = new JLabel("📌 Queue & Priority Policy:");
        g1.setFont(new Font("SansSerif", Font.BOLD, 12));
        g1.setForeground(ThemeUtils.WARNING_AMBER);
        JLabel g2 = new JLabel("• Priority tickets (P-series/VIP) automatically precede normal tickets (T-series/SSD).");
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setForeground(ThemeUtils.TEXT_MUTED);
        JLabel g3 = new JLabel("• Tirupati Mode routes Supadham, Senior Citizens, and Infants with zero waiting bottleneck.");
        g3.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g3.setForeground(ThemeUtils.TEXT_MUTED);
        JLabel g4 = new JLabel("• All tickets and customer profiles are instantly written to the SQL Database via JDBC.");
        g4.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g4.setForeground(ThemeUtils.TEXT_MUTED);

        guideBox.add(g1);
        guideBox.add(Box.createVerticalStrut(4));
        guideBox.add(g2);
        guideBox.add(Box.createVerticalStrut(2));
        guideBox.add(g3);
        guideBox.add(Box.createVerticalStrut(2));
        guideBox.add(g4);

        existCard.add(existTitle);
        existCard.add(Box.createVerticalStrut(6));
        existCard.add(existDesc);
        existCard.add(Box.createVerticalStrut(14));
        existCard.add(existingCustomerCombo);
        existCard.add(Box.createVerticalStrut(14));
        existCard.add(issueExistingBtn);
        existCard.add(Box.createVerticalStrut(24));
        existCard.add(guideBox);
        existCard.add(Box.createVerticalGlue());

        panel.add(newCustCard);
        panel.add(existCard);
        return panel;
    }

    private void addFormField(JPanel parent, String label, JComponent comp) {
        JLabel l = ThemeUtils.createMutedLabel(label, 12);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);

        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
        parent.add(comp);
        parent.add(Box.createVerticalStrut(10));
    }

    // TAB 3: LIVE QUEUE MONITOR
    private JPanel buildQueueMonitorTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Filter Bar & Action buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBtns.setOpaque(false);

        JButton allBtn = ThemeUtils.createStyledButton("All Tickets", ThemeUtils.CARD_BG, Color.WHITE);
        JButton waitBtn = ThemeUtils.createStyledButton("Waiting", ThemeUtils.CARD_BG, Color.WHITE);
        JButton servBtn = ThemeUtils.createStyledButton("Serving / Entering", ThemeUtils.CARD_BG, Color.WHITE);
        JButton compBtn = ThemeUtils.createStyledButton("Completed", ThemeUtils.CARD_BG, Color.WHITE);

        allBtn.addActionListener(e -> { currentQueueFilter = "ALL"; refreshQueueTable(); });
        waitBtn.addActionListener(e -> { currentQueueFilter = "WAITING"; refreshQueueTable(); });
        servBtn.addActionListener(e -> { currentQueueFilter = "SERVING"; refreshQueueTable(); });
        compBtn.addActionListener(e -> { currentQueueFilter = "COMPLETED"; refreshQueueTable(); });

        filterBtns.add(allBtn);
        filterBtns.add(waitBtn);
        filterBtns.add(servBtn);
        filterBtns.add(compBtn);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton callSelectedBtn = ThemeUtils.createStyledButton("📢 Call Selected", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        callSelectedBtn.addActionListener(e -> handleCallSelectedTicket());

        JButton completeSelectedBtn = ThemeUtils.createStyledButton("✅ Complete Selected", ThemeUtils.SUCCESS_GREEN, Color.WHITE);
        completeSelectedBtn.addActionListener(e -> handleCompleteSelectedTicket());

        JButton cancelSelectedBtn = ThemeUtils.createStyledButton("❌ Cancel Ticket", ThemeUtils.DANGER_RED, Color.WHITE);
        cancelSelectedBtn.addActionListener(e -> handleCancelSelectedTicket());

        actionBtns.add(callSelectedBtn);
        actionBtns.add(completeSelectedBtn);
        actionBtns.add(cancelSelectedBtn);

        topBar.add(filterBtns, BorderLayout.WEST);
        topBar.add(actionBtns, BorderLayout.EAST);

        // Queue Table
        String[] columnNames = {"Token", "Priority", "ID", "Name", "Service / Category", "Status", "Counter / Gate", "Issued At", "Served At"};
        queueTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        queueTable = new JTable(queueTableModel);
        ThemeUtils.styleTable(queueTable);

        // Custom Cell Renderer for Status and Priority columns
        queueTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if ("VIP".equals(value)) {
                    setForeground(new Color(245, 158, 11));
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                } else {
                    setForeground(new Color(156, 163, 175));
                    setFont(new Font("SansSerif", Font.PLAIN, 12));
                }
                return c;
            }
        });

        queueTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                if ("WAITING".equals(value)) {
                    setForeground(new Color(96, 165, 250));
                } else if ("SERVING".equals(value)) {
                    setForeground(new Color(167, 139, 250));
                } else if ("COMPLETED".equals(value)) {
                    setForeground(new Color(52, 211, 153));
                } else {
                    setForeground(new Color(239, 68, 68));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(queueTable);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // TAB 4: CUSTOMER / DEVOTEE DIRECTORY
    private JPanel buildCustomerDirectoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search & Action bar
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setOpaque(false);

        JPanel searchInputBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchInputBox.setOpaque(false);

        JLabel searchLbl = ThemeUtils.createMutedLabel("Search Directory (ID / Name / Phone):", 12);
        customerSearchField = new JTextField(20);
        customerSearchField.setPreferredSize(new Dimension(220, 32));

        JButton searchBtn = ThemeUtils.createStyledButton("🔍 Search", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        searchBtn.addActionListener(e -> refreshCustomerTable(customerSearchField.getText()));

        JButton clearSearchBtn = ThemeUtils.createStyledButton("Clear", new Color(55, 65, 81), Color.WHITE);
        clearSearchBtn.addActionListener(e -> {
            customerSearchField.setText("");
            refreshCustomerTable("");
        });

        searchInputBox.add(searchLbl);
        searchInputBox.add(customerSearchField);
        searchInputBox.add(searchBtn);
        searchInputBox.add(clearSearchBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton issueForCustBtn = ThemeUtils.createStyledButton("🎫 Issue Token", ThemeUtils.SUCCESS_GREEN, Color.WHITE);
        issueForCustBtn.addActionListener(e -> handleIssueTokenFromDirectory());

        JButton viewDetailsBtn = ThemeUtils.createStyledButton("ℹ️ View Details", new Color(75, 85, 99), Color.WHITE);
        viewDetailsBtn.addActionListener(e -> handleViewCustomerDetails());

        rightActions.add(issueForCustBtn);
        rightActions.add(viewDetailsBtn);

        searchBar.add(searchInputBox, BorderLayout.WEST);
        searchBar.add(rightActions, BorderLayout.EAST);

        // Customer Table
        String[] customerCols = {"ID", "Name", "Age", "Phone", "Category", "Priority Status", "Last Token", "Registered At"};
        customerTableModel = new DefaultTableModel(customerCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(customerTableModel);
        ThemeUtils.styleTable(customerTable);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // TAB 5: SYSTEM TOOLS & DATABASE
    private JPanel buildSystemToolsTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(ThemeUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Card 1: Data Management Tools
        JPanel toolCard = ThemeUtils.createCardPanel();
        toolCard.setLayout(new BoxLayout(toolCard, BoxLayout.Y_AXIS));

        JLabel toolTitle = ThemeUtils.createHeaderLabel("System Testing & Demonstrations", 16);
        toolTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton tirupatiDemoBtn = ThemeUtils.createStyledButton("🛕 Load Tirupati Scenario (7 Pilgrims & VQC Queues)", new Color(217, 119, 6), Color.WHITE);
        tirupatiDemoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tirupatiDemoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        tirupatiDemoBtn.addActionListener(e -> {
            if (!isTirupatiMode) {
                tirupatiToggleBtn.setSelected(true);
                toggleTirupatiMode();
            } else {
                queueService.seedTirupatiDemoData();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Tirupati pilgrimage scenario refreshed!", "TTD Data Loaded", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton oneLakhBtn = ThemeUtils.createStyledButton("🚀 STRESS TEST: Load 100,000 (1 Lakh) Devotees into SQL DB", new Color(180, 83, 9), Color.WHITE);
        oneLakhBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        oneLakhBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        oneLakhBtn.addActionListener(e -> handleLoadOneLakhDevotees());

        JButton seedBtn = ThemeUtils.createStyledButton("🌱 Populate Banking Demo Data (7 Indian Customers + Tickets)", ThemeUtils.ACCENT_BLUE, Color.WHITE);
        seedBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        seedBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        seedBtn.addActionListener(e -> {
            if (isTirupatiMode) {
                tirupatiToggleBtn.setSelected(false);
                toggleTirupatiMode();
            } else {
                queueService.seedSampleData();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Realistic banking demo data seeded successfully!", "Data Seeded", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton exportBtn = ThemeUtils.createStyledButton("📥 Export Queue Log to CSV File", new Color(16, 185, 129), Color.WHITE);
        exportBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        exportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportBtn.addActionListener(e -> handleExportCSV());

        JButton resetQueueBtn = ThemeUtils.createStyledButton("⚠️ Reset Today's Queue (Clear Tickets)", ThemeUtils.DANGER_RED, Color.WHITE);
        resetQueueBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        resetQueueBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetQueueBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to reset all queue tickets? (Registered profiles will be kept)",
                    "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                queueService.resetQueue();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Queue reset successfully.", "Queue Reset", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toolCard.add(toolTitle);
        toolCard.add(Box.createVerticalStrut(14));
        toolCard.add(tirupatiDemoBtn);
        toolCard.add(Box.createVerticalStrut(10));
        toolCard.add(oneLakhBtn);
        toolCard.add(Box.createVerticalStrut(10));
        toolCard.add(seedBtn);
        toolCard.add(Box.createVerticalStrut(10));
        toolCard.add(exportBtn);
        toolCard.add(Box.createVerticalStrut(10));
        toolCard.add(resetQueueBtn);
        toolCard.add(Box.createVerticalGlue());

        // Card 2: Database Status & Technical Architecture
        JPanel infoCard = ThemeUtils.createCardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        JLabel infoTitle = ThemeUtils.createHeaderLabel("JDBC & Architecture Info", 16);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoText = new JTextArea();
        infoText.setEditable(false);
        infoText.setBackground(new Color(24, 24, 27));
        infoText.setForeground(new Color(209, 213, 219));
        infoText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoText.setBorder(new EmptyBorder(10, 10, 10, 10));

        DatabaseManager db = DatabaseManager.getInstance();
        infoText.setText(
                "--- DATABASE CONFIGURATION ---\n" +
                "Engine         : " + db.getDatabaseType() + "\n" +
                "JDBC URL       : " + db.getDbUrl() + "\n" +
                "Status         : CONNECTED & SYNCHRONIZED\n" +
                "Schema         : customers, queue_tickets, service_counters\n\n" +
                "--- TIRUPATI (TTD) PILGRIMAGE SCALING ---\n" +
                "• VQC Compartments : PriorityQueue partitions (Sarva Darshan / SED ₹300)\n" +
                "• Supadham Entry   : Priority routing for Divyangjan & Infants\n" +
                "• Mahadwaram Gate  : Protocol VIP Break Darshan\n" +
                "• Turnstiles & DB  : PreparedStatements with immutable audit timestamps\n\n" +
                "--- OOP & DATA STRUCTURE ARCHITECTURE ---\n" +
                "• Abstract Class    : Person (model.Person)\n" +
                "• Inheritance       : Customer extends Person\n" +
                "• Polymorphism      : displayDetails() overridden\n" +
                "• Comparable        : Customer & QueueTicket prioritize VIP\n" +
                "• Data Structures   : PriorityQueue (VIP) + LinkedList (FIFO)\n" +
                "• Persistence       : Full JDBC Driver (SQLite/MySQL)\n" +
                "• Architecture      : MVC with DAO pattern (CustomerDAO, TicketDAO)"
        );

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(12));
        infoCard.add(new JScrollPane(infoText));

        panel.add(toolCard);
        panel.add(infoCard);
        return panel;
    }

    // MODE TOGGLE IMPLEMENTATION

    private void toggleTirupatiMode() {
        isTirupatiMode = tirupatiToggleBtn.isSelected();
        applyModeConfigurations();
    }

    private void applyModeConfigurations() {
        if (isTirupatiMode) {
            tirupatiToggleBtn.setText("🛕 Tirupati (TTD) Mode: ACTIVE");
            tirupatiToggleBtn.setBackground(new Color(217, 119, 6)); // Saffron
            tirupatiToggleBtn.setForeground(Color.WHITE);

            modeBadge.setText("🛕 TTD VAIKUNTAM PILGRIMAGE MODE");
            modeBadge.setBackground(new Color(120, 53, 15));
            modeBadge.setForeground(new Color(254, 215, 170));

            brandIcon.setText("🛕");
            mainTitle.setText("TTD SMART VAIKUNTAM QUEUE MANAGEMENT SYSTEM");
            subTitle.setText("Tirumala Devotee Flow • Sarva Darshan (SSD), Special Entry (SED ₹300), Supadham & VIP Break");
            setTitle("TTD Smart Vaikuntam Queue System - Tirumala Tirupati Devasthanams");

            card1Title.setText("👥 Total Devotees");
            card2Title.setText("⏳ VQC Normal Wait");
            card3Title.setText("⭐ VIP / Supadham Wait");
            card4Title.setText("🔔 Entering Sanctum");
            card5Title.setText("🙏 Darshan Completed");
            card6Title.setText("⏱️ Est. Wait to Sanctum");

            screenTitle.setText("VAIKUNTAM QUEUE COMPLEX (VQC) PUBLIC DISPLAY");
            newCustTitle.setText("Register Devotee & Issue Darshan Pass");
            regPriorityCheck.setText("Special Category Devotee (Senior Citizen / Divyangjan / VIP Protocol / Infant)");

            // Populate Tirupati counters
            counterDropdown.removeAllItems();
            counterDropdown.addItem("Supadham Entry (Senior Citizen & Infant)");
            counterDropdown.addItem("VQC-I Compartment 4 (Sarva Darshan SSD)");
            counterDropdown.addItem("VQC-II Compartment 12 (Slotted Token)");
            counterDropdown.addItem("Special Entry Gate (SED ₹300)");
            counterDropdown.addItem("Mahadwaram VIP Break Entry Gate");
            counterDropdown.addItem("Laddoo Prasadam Distribution Complex");

            // Populate Tirupati services
            regServiceCombo.removeAllItems();
            regServiceCombo.addItem("Divyangjan & Senior Citizens Darshan");
            regServiceCombo.addItem("Special Entry Darshan (SED ₹300)");
            regServiceCombo.addItem("VIP Break Darshan (Protocol L1/L2)");
            regServiceCombo.addItem("Sarva Darshan (Free Slotted SSD)");
            regServiceCombo.addItem("Divya Darshan (Alipiri Footpath)");
            regServiceCombo.addItem("Infant (<1 yr) & Parents Priority");
            regServiceCombo.addItem("Arjitha Seva (Kalyanotsavam Pass)");
            regServiceCombo.addItem("Laddoo Prasadam Distribution");

            // Automatically seed realistic Tirupati demo data
            queueService.seedTirupatiDemoData();
            refreshAllData();

            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 320px;'>" +
                    "<h3 style='color: #D97706;'>🛕 Tirupati (TTD) Use Case Activated!</h3>" +
                    "<p>Configured for <b>Tirumala Tirupati Devasthanams</b> crowd management:</p>" +
                    "<ul>" +
                    "<li><b>VQC Compartments:</b> Sarva Darshan (SSD) & SED ₹300 queues</li>" +
                    "<li><b>Supadham Entry:</b> Automatic priority routing for Senior Citizens & Infants</li>" +
                    "<li><b>Mahadwaram:</b> Protocol VIP Break Darshan channel</li>" +
                    "<li><b>Real-time DB:</b> Live pilgrims loaded into SQLite database!</li>" +
                    "</ul>" +
                    "</body></html>",
                    "TTD Tirupati Mode Active",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            tirupatiToggleBtn.setText("🛕 Switch to Tirupati (TTD) Demo");
            tirupatiToggleBtn.setBackground(new Color(45, 55, 72));
            tirupatiToggleBtn.setForeground(Color.WHITE);

            modeBadge.setText("🏛️ CORPORATE / BANK MODE");
            modeBadge.setBackground(new Color(30, 41, 59));
            modeBadge.setForeground(new Color(148, 163, 184));

            brandIcon.setText("⚡");
            mainTitle.setText("SMART DIGITAL QUEUE MANAGEMENT");
            subTitle.setText("Enterprise Flow Optimization • JDBC & SQL Persistent");
            setTitle("Smart Digital Queue Management System - Enterprise Edition");

            card1Title.setText("👥 Total Customers");
            card2Title.setText("⏳ Normal Wait");
            card3Title.setText("⭐ Priority (VIP) Wait");
            card4Title.setText("🔔 Currently Serving");
            card5Title.setText("✅ Completed Today");
            card6Title.setText("⏱️ Est. Waiting Time");

            screenTitle.setText("PUBLIC DISPLAY MONITOR");
            newCustTitle.setText("Register New Customer & Issue Token");
            regPriorityCheck.setText("Priority Customer (Senior Citizen / VIP / Special Assistance)");

            // Populate Standard counters
            counterDropdown.removeAllItems();
            counterDropdown.addItem("Counter 1 - General Banking");
            counterDropdown.addItem("Counter 2 - Cashier & Deposits");
            counterDropdown.addItem("Counter 3 - Loans & Mortgages");
            counterDropdown.addItem("Counter 4 - Priority & VIP Desk");

            // Populate Standard services
            regServiceCombo.removeAllItems();
            regServiceCombo.addItem("General Banking Inquiry");
            regServiceCombo.addItem("Cash Deposit & Withdrawal");
            regServiceCombo.addItem("Loan & Mortgage Consultation");
            regServiceCombo.addItem("Credit Card & Account Opening");
            regServiceCombo.addItem("Wealth Management (VIP)");
            regServiceCombo.addItem("Support & Dispute Resolution");

            // Reload standard demo data
            queueService.seedSampleData();
            refreshAllData();
        }
    }

    // ACTION HANDLERS

    private void handleLoadOneLakhDevotees() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 320px;'>" +
                "<h3 style='color: #D97706;'>🚀 1 Lakh (100,000) Devotees Stress Test</h3>" +
                "<p>This will generate and insert <b>100,000 Tirupati pilgrims</b> into the SQLite SQL Database using fast JDBC batching.</p>" +
                "<p>Do you want to proceed?</p></body></html>",
                "Confirm 100k Devotees Batch Insert",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Ensure we are in Tirupati mode
        if (!isTirupatiMode) {
            tirupatiToggleBtn.setSelected(true);
            isTirupatiMode = true;
            applyModeConfigurations();
        }

        // Run batch insertion in background SwingWorker thread to keep UI completely responsive
        JDialog loadingDialog = new JDialog(this, "Loading 100,000 Devotees...", true);
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(ThemeUtils.BG_DARK);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel msg = ThemeUtils.createHeaderLabel("⚡ Inserting 100,000 Devotees into SQLite Database...", 14);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        p.add(msg, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        loadingDialog.setContentPane(p);
        loadingDialog.setSize(440, 140);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private long durationMs = 0;

            @Override
            protected Void doInBackground() {
                long t0 = System.currentTimeMillis();
                queueService.seedOneLakhDevotees();
                durationMs = System.currentTimeMillis() - t0;
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(QueueDashboardFrame.this,
                        "<html><body style='width: 340px;'>" +
                        "<h3 style='color: #10B981;'>✅ 100,000 Devotees Loaded Successfully!</h3>" +
                        "<p><b>Database:</b> SQLite (queue_system.db)</p>" +
                        "<p><b>Total Devotees in SQL DB:</b> 100,000 (1 Lakh)</p>" +
                        "<p><b>Insertion Duration:</b> " + (durationMs / 1000.0) + " seconds</p>" +
                        "<p><b>VQC Queues & Metrics:</b> Live aggregate metrics updated!</p>" +
                        "</body></html>",
                        "Stress Test Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private void handleCallNext() {
        String counter = (String) counterDropdown.getSelectedItem();
        QueueTicket called = queueService.callNextCustomer(counter);
        if (called == null) {
            String msg = isTirupatiMode ? "No devotees currently waiting in VQC compartments." : "No customers currently waiting in queue.";
            JOptionPane.showMessageDialog(this, msg, "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Beep chime sound
            Toolkit.getDefaultToolkit().beep();
        }
        refreshAllData();
    }

    private void handleRecall() {
        QueueTicket serving = queueService.getCurrentlyServingTicket();
        if (serving == null) {
            JOptionPane.showMessageDialog(this, "No person currently serving to re-announce.", "No Devotee / Customer", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
        String msg = isTirupatiMode
                ? "📢 Attention Devotee: Token " + serving.getTokenNumber() + " (" + serving.getCustomerName() +
                  ") please proceed to " + serving.getCounterAssigned() + " for Darshan Entry!"
                : "📢 Attention Customer: Token " + serving.getTokenNumber() + " (" + serving.getCustomerName() +
                  ") please report to " + serving.getCounterAssigned() + "!";

        JOptionPane.showMessageDialog(this, msg, "Announcement Re-Broadcasted", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCompleteServing() {
        QueueTicket serving = queueService.getCurrentlyServingTicket();
        if (serving == null) {
            JOptionPane.showMessageDialog(this, "No active person currently being served.", "None Active", JOptionPane.WARNING_MESSAGE);
            return;
        }
        queueService.completeTicket(serving.getId());
        refreshAllData();
    }

    private void handleCancelServing() {
        QueueTicket serving = queueService.getCurrentlyServingTicket();
        if (serving == null) {
            JOptionPane.showMessageDialog(this, "No active person currently being served.", "None Active", JOptionPane.WARNING_MESSAGE);
            return;
        }
        queueService.cancelTicket(serving.getId());
        refreshAllData();
    }

    private void handleRegisterNewCustomer() {
        String name = regNameField.getText().trim();
        int age = (Integer) regAgeSpinner.getValue();
        String phone = regPhoneField.getText().trim();
        String service = (String) regServiceCombo.getSelectedItem();
        boolean isPriority = regPriorityCheck.isSelected();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter name and phone number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer customer = queueService.registerCustomer(name, age, phone, service, isPriority);
        QueueTicket ticket = queueService.issueToken(customer.getCustomerId());

        // Clear form
        regNameField.setText("");
        regPhoneField.setText("");
        regPriorityCheck.setSelected(false);

        refreshAllData();

        // Show Token Slip Popup
        int estWait = queueService.getStatistics().getEstimatedWaitMinutes();
        TokenSlipDialog dialog = new TokenSlipDialog(this, ticket, estWait);
        dialog.setVisible(true);
    }

    private void handleIssueExistingCustomerToken() {
        int idx = existingCustomerCombo.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Please select an existing profile.", "None Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Customer> customers = queueService.getAllCustomers();
        if (idx < customers.size()) {
            Customer c = customers.get(idx);
            QueueTicket ticket = queueService.issueToken(c.getCustomerId());
            refreshAllData();

            int estWait = queueService.getStatistics().getEstimatedWaitMinutes();
            TokenSlipDialog dialog = new TokenSlipDialog(this, ticket, estWait);
            dialog.setVisible(true);
        }
    }

    private void handleCallSelectedTicket() {
        int row = queueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String token = (String) queueTableModel.getValueAt(row, 0);
        QueueTicket ticket = findTicketByToken(token);
        if (ticket == null || ticket.getStatus() != QueueTicket.Status.WAITING) {
            JOptionPane.showMessageDialog(this, "Selected ticket is not waiting.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String counter = (String) counterDropdown.getSelectedItem();
        try {
            new dao.TicketDAO().callTicket(ticket.getId(), counter);
            queueService.refreshMemoryFromDatabase();
            Toolkit.getDefaultToolkit().beep();
            refreshAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calling ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCompleteSelectedTicket() {
        int row = queueTable.getSelectedRow();
        if (row < 0) return;
        String token = (String) queueTableModel.getValueAt(row, 0);
        QueueTicket ticket = findTicketByToken(token);
        if (ticket != null) {
            queueService.completeTicket(ticket.getId());
            refreshAllData();
        }
    }

    private void handleCancelSelectedTicket() {
        int row = queueTable.getSelectedRow();
        if (row < 0) return;
        String token = (String) queueTableModel.getValueAt(row, 0);
        QueueTicket ticket = findTicketByToken(token);
        if (ticket != null) {
            queueService.cancelTicket(ticket.getId());
            refreshAllData();
        }
    }

    private void handleIssueTokenFromDirectory() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an entry from the directory.", "No Profile Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String custId = (String) customerTableModel.getValueAt(row, 0);
        QueueTicket ticket = queueService.issueToken(custId);
        refreshAllData();

        int estWait = queueService.getStatistics().getEstimatedWaitMinutes();
        TokenSlipDialog dialog = new TokenSlipDialog(this, ticket, estWait);
        dialog.setVisible(true);
    }

    private void handleViewCustomerDetails() {
        int row = customerTable.getSelectedRow();
        if (row < 0) return;
        String custId = (String) customerTableModel.getValueAt(row, 0);
        Customer c = queueService.getCustomerById(custId);
        if (c != null) {
            String details = String.format(
                    "ID: %s\n" +
                    "Full Name: %s\n" +
                    "Age: %d\n" +
                    "Phone: %s\n" +
                    "Category / Service: %s\n" +
                    "Priority Status: %s\n" +
                    "Active Token: %s\n" +
                    "Registered: %s",
                    c.getCustomerId(),
                    c.getName(),
                    c.getAge(),
                    c.getPhone(),
                    c.getServiceType(),
                    c.isPriorityCustomer() ? "YES (VIP / Special Assistance)" : "Standard",
                    c.getTokenNumber().isEmpty() ? "None" : c.getTokenNumber(),
                    c.getCreatedAt()
            );
            JOptionPane.showMessageDialog(this, details, isTirupatiMode ? "Devotee Profile" : "Customer Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(isTirupatiMode ? "ttd_darshan_queue_report.csv" : "queue_report.csv"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Token,Priority,ID,Name,ServiceCategory,Status,CounterGate,IssuedAt,ServedAt,CompletedAt");
                List<QueueTicket> tickets = queueService.getAllTickets();
                for (QueueTicket t : tickets) {
                    pw.printf("%s,%s,%s,\"%s\",\"%s\",%s,\"%s\",%s,%s,%s\n",
                            t.getTokenNumber(),
                            t.isPriority() ? "VIP" : "NORMAL",
                            t.getCustomerId(),
                            t.getCustomerName(),
                            t.getServiceType(),
                            t.getStatus(),
                            t.getCounterAssigned(),
                            t.getIssuedAt(),
                            t.getServedAt(),
                            t.getCompletedAt()
                    );
                }
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private QueueTicket findTicketByToken(String token) {
        for (QueueTicket t : queueService.getAllTickets()) {
            if (t.getTokenNumber().equals(token)) {
                return t;
            }
        }
        return null;
    }

    // DATA REFRESH LOGIC

    public void refreshAllData() {
        queueService.refreshMemoryFromDatabase();

        // 1. Refresh Metrics
        QueueStatistics stats = queueService.getStatistics();
        statTotalCustomersLabel.setText(String.valueOf(stats.getTotalCustomers()));
        statWaitingNormalLabel.setText(String.valueOf(stats.getWaitingNormal()));
        statWaitingPriorityLabel.setText(String.valueOf(stats.getWaitingPriority()));
        statServingLabel.setText(String.valueOf(stats.getCurrentlyServing()));
        statCompletedLabel.setText(String.valueOf(stats.getCompletedToday()));
        int totalWait = stats.getEstimatedWaitMinutes();
        String waitDisplay;
        if (totalWait >= 1440) {
            waitDisplay = "24h (Max)";
        } else if (totalWait >= 60) {
            int hrs = totalWait / 60;
            int mins = totalWait % 60;
            waitDisplay = hrs + "h" + (mins > 0 ? (" " + mins + "m") : "");
        } else {
            waitDisplay = totalWait + "m";
        }
        statEstimatedWaitLabel.setText(waitDisplay);

        // 2. Refresh Calling Station Display
        QueueTicket serving = queueService.getCurrentlyServingTicket();
        if (serving != null) {
            servingTokenLabel.setText(serving.getTokenNumber());
            servingTokenLabel.setForeground(serving.isPriority() ? ThemeUtils.WARNING_AMBER : Color.WHITE);
            servingCustomerNameLabel.setText(serving.getCustomerName() + " (" + serving.getCustomerId() + ")");
            servingServiceLabel.setText((isTirupatiMode ? "Category: " : "Service: ") + serving.getServiceType());
            servingCounterLabel.setText((isTirupatiMode ? "Proceed to Gate: " : "Please Proceed to: ") + serving.getCounterAssigned());
            servingStatusBadge.setText(isTirupatiMode ? "ENTERING SANCTUM" : "SERVING NOW");
            servingStatusBadge.setBackground(new Color(139, 92, 246));
        } else {
            servingTokenLabel.setText("---");
            servingTokenLabel.setForeground(ThemeUtils.TEXT_MUTED);
            servingCustomerNameLabel.setText(isTirupatiMode ? "No Active Devotee" : "No Active Customer");
            servingServiceLabel.setText(isTirupatiMode ? "Category: Standby" : "Service: Standby");
            servingCounterLabel.setText(isTirupatiMode ? "Gate / Line Available" : "Counter Available");
            servingStatusBadge.setText("STANDBY");
            servingStatusBadge.setBackground(new Color(55, 65, 81));
        }

        // 3. Refresh Upcoming List
        upcomingListModel.clear();
        List<QueueTicket> waiting = queueService.getWaitingTickets();
        int count = 0;
        for (QueueTicket t : waiting) {
            if (count++ >= 8) break;
            String prefix = t.isPriority() ? "★ [VIP] " : "  [NORM]";
            upcomingListModel.addElement(String.format("%s %-5s - %s (%s)", prefix, t.getTokenNumber(), t.getCustomerName(), t.getServiceType()));
        }
        if (upcomingListModel.isEmpty()) {
            upcomingListModel.addElement(isTirupatiMode ? "No upcoming devotees in VQC line." : "No upcoming customers in queue.");
        }

        // 4. Refresh Existing Profile Dropdown
        existingCustomerCombo.removeAllItems();
        List<Customer> allCust = queueService.getAllCustomers();
        for (Customer c : allCust) {
            String badge = c.isPriorityCustomer() ? "[VIP]" : "[NORM]";
            existingCustomerCombo.addItem(String.format("%s %s - %s (%s)", badge, c.getCustomerId(), c.getName(), c.getServiceType()));
        }

        // 5. Refresh Tables
        refreshQueueTable();
        refreshCustomerTable(customerSearchField != null ? customerSearchField.getText() : "");
    }

    private void refreshQueueTable() {
        queueTableModel.setRowCount(0);
        List<QueueTicket> list;
        if ("WAITING".equals(currentQueueFilter)) {
            list = queueService.getWaitingTickets();
        } else if ("SERVING".equals(currentQueueFilter)) {
            list = queueService.getServingTickets();
        } else if ("COMPLETED".equals(currentQueueFilter)) {
            list = queueService.getCompletedTickets();
        } else {
            list = queueService.getAllTickets();
        }

        for (QueueTicket t : list) {
            queueTableModel.addRow(new Object[]{
                    t.getTokenNumber(),
                    t.isPriority() ? "VIP" : "Normal",
                    t.getCustomerId(),
                    t.getCustomerName(),
                    t.getServiceType(),
                    t.getStatus().name(),
                    t.getCounterAssigned().isEmpty() ? "-" : t.getCounterAssigned(),
                    t.getIssuedAt(),
                    t.getServedAt().isEmpty() ? "-" : t.getServedAt()
            });
        }
    }

    private void refreshCustomerTable(String query) {
        customerTableModel.setRowCount(0);
        List<Customer> list = queueService.searchCustomers(query);
        for (Customer c : list) {
            customerTableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getName(),
                    c.getAge(),
                    c.getPhone(),
                    c.getServiceType(),
                    c.isPriorityCustomer() ? (isTirupatiMode ? "Special / VIP" : "VIP Priority") : "Standard",
                    c.getTokenNumber().isEmpty() ? "-" : c.getTokenNumber(),
                    c.getCreatedAt()
            });
        }
    }

    private void startClockAndAutoRefresh() {
        // Digital clock timer (every 1 second)
        Timer timer = new Timer(1000, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeUtils.applyTheme(true);
            QueueDashboardFrame frame = new QueueDashboardFrame();
            frame.setVisible(true);
        });
    }
}
