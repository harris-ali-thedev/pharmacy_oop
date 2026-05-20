package com.pharmacy.ui;

import com.pharmacy.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // Content panel IDs (used as CardLayout keys)
    private static final String CARD_DASHBOARD   = "dashboard";
    private static final String CARD_POS         = "pos";
    private static final String CARD_INVENTORY   = "inventory";
    private static final String CARD_PATIENTS    = "patients";
    private static final String CARD_PRESCRIPTIONS = "prescriptions";
    private static final String CARD_SUPPLIERS   = "suppliers";
    private static final String CARD_REPORTS     = "reports";
    private static final String CARD_USERS       = "users";
    private static final String CARD_AUDIT       = "audit";

    private CardLayout  cardLayout;
    private JPanel      contentArea;
    private JLabel      currentUserLabel;
    private String      activeCard = CARD_DASHBOARD;

    // Sidebar buttons — kept for highlighting
    private final java.util.Map<String, JButton> navButtons = new java.util.LinkedHashMap<>();

    public MainFrame() {
        setTitle("MediCare Pharmacy Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1080, 480);
        setMinimumSize(new Dimension(720, 540));
        setLocationRelativeTo(null);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleExit(); }
        });

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // ── Sidebar ──────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content area (CardLayout) ─────────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(SwingUtils.BG);

        contentArea.add(new DashboardPanel(),       CARD_DASHBOARD);
        contentArea.add(new POSPanel(),             CARD_POS);
        contentArea.add(new InventoryPanel(),       CARD_INVENTORY);
        contentArea.add(new PatientPanel(),         CARD_PATIENTS);
        contentArea.add(new PrescriptionPanel(),    CARD_PRESCRIPTIONS);
        contentArea.add(new SupplierPanel(),        CARD_SUPPLIERS);
        contentArea.add(new SalesReportPanel(),     CARD_REPORTS);
        contentArea.add(new UserManagementPanel(),  CARD_USERS);
        contentArea.add(new AuditLogPanel(),        CARD_AUDIT);

        add(contentArea, BorderLayout.CENTER);

        // Show dashboard by default
        navigate(CARD_DASHBOARD);
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SwingUtils.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(238, 0));

        // Logo block
        JPanel logoBlock = new JPanel();
        logoBlock.setLayout(new BoxLayout(logoBlock, BoxLayout.Y_AXIS));
        logoBlock.setBackground(SwingUtils.SIDEBAR);
        logoBlock.setBorder(BorderFactory.createEmptyBorder(22, 22, 20, 22));

        JLabel logo = new JLabel("MediCare");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Pharmacy Management Suite");
        sub.setFont(SwingUtils.FONT_SMALL);
        sub.setForeground(new Color(169, 215, 217));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoBlock.add(logo);
        logoBlock.add(Box.createVerticalStrut(4));
        logoBlock.add(sub);
        sidebar.add(logoBlock);

        // Navigation items
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(navItem("Dashboard",   CARD_DASHBOARD, true));
        sidebar.add(navItem("Point of Sale", CARD_POS, true));
        sidebar.add(navItem("Inventory",   CARD_INVENTORY, true));
        sidebar.add(navItem("Patients",    CARD_PATIENTS, true));
        sidebar.add(navItem("Prescriptions", CARD_PRESCRIPTIONS, true));
        sidebar.add(navItem("Suppliers",   CARD_SUPPLIERS, true));
        sidebar.add(navItem("Sales Reports", CARD_REPORTS, true));

        // Admin-only section
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (isAdmin) {
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(238, 1));
            sep.setForeground(new Color(26, 73, 91));
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(sep);
            sidebar.add(Box.createVerticalStrut(8));

            JLabel adminLabel = new JLabel("  ADMIN");
            adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            adminLabel.setForeground(new Color(126, 171, 177));
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(adminLabel);
            sidebar.add(Box.createVerticalStrut(4));
            sidebar.add(navItem("Users",    CARD_USERS, true));
            sidebar.add(navItem("Audit Log", CARD_AUDIT, true));
        }

        // Push logout to bottom
        sidebar.add(Box.createVerticalGlue());

        // Current user info block
        JPanel userBlock = new JPanel();
        userBlock.setLayout(new BoxLayout(userBlock, BoxLayout.Y_AXIS));
        userBlock.setBackground(new Color(6, 30, 42));
        userBlock.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        userBlock.setMaximumSize(new Dimension(238, 112));

        currentUserLabel = new JLabel(SessionManager.getInstance().getCurrentUser().getFullName());
        currentUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentUserLabel.setForeground(Color.WHITE);
        currentUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(SessionManager.getInstance().getCurrentUser().getRole().getLabel());
        roleLabel.setFont(SwingUtils.FONT_SMALL);
        roleLabel.setForeground(new Color(169, 215, 217));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("Segue UI", Font.PLAIN, 12));
        logoutBtn.setForeground(new Color(148, 163, 184));
        logoutBtn.setBackground(new Color(6, 30, 42));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());

        userBlock.add(currentUserLabel);
        userBlock.add(Box.createVerticalStrut(2));
        userBlock.add(roleLabel);
        userBlock.add(Box.createVerticalStrut(6));
        userBlock.add(logoutBtn);
        sidebar.add(userBlock);

        return sidebar;
    }

    private JButton navItem(String label, String cardId, boolean visible) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(190, 218, 221));
        btn.setBackground(SwingUtils.SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(11, 24, 11, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(238, 44));
        btn.setVisible(visible);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!cardId.equals(activeCard))
                    btn.setBackground(SwingUtils.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!cardId.equals(activeCard))
                    btn.setBackground(SwingUtils.SIDEBAR);
            }
        });
        btn.addActionListener(e -> navigate(cardId));

        navButtons.put(cardId, btn);
        return btn;
    }

    public void navigate(String cardId) {
        // Un-highlight previous
        JButton prev = navButtons.get(activeCard);
        if (prev != null) {
            prev.setBackground(SwingUtils.SIDEBAR);
            prev.setForeground(new Color(190, 218, 221));
        }
        // Highlight new
        JButton next = navButtons.get(cardId);
        if (next != null) {
            next.setBackground(SwingUtils.PRIMARY);
            next.setForeground(Color.WHITE);
        }
        activeCard = cardId;
        cardLayout.show(contentArea, cardId);

        // Refresh dashboard when navigating to it
        if (cardId.equals(CARD_DASHBOARD)) {
            Component c = getCardComponent(CARD_DASHBOARD);
            if (c instanceof DashboardPanel dp) dp.refresh();
        }
    }

    private Component getCardComponent(String cardId) {
        for (Component c : contentArea.getComponents()) {
            if (cardId.equals(c.getName())) return c;
        }
        // Use the card manager's visible component approach
        for (Component c : contentArea.getComponents()) {
            if (c.isVisible()) return c;
        }
        return null;
    }

    // ── Lifecycle ─────────────────────────────────────────────

    private void handleLogout() {
        if (SwingUtils.confirm(this, "Sign out of MediCare PMS?")) {
            SessionManager.getInstance().logout();
            dispose();
            PharmacyApp.showLogin();
        }
    }

    private void handleExit() {
        if (SwingUtils.confirm(this, "Exit MediCare PMS?")) {
            SessionManager.getInstance().logout();
            System.exit(0);
        }
    }
}
