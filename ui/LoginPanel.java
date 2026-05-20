package com.pharmacy.ui;

import com.pharmacy.generics.Result;
import com.pharmacy.model.User;
import com.pharmacy.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;

/**
 * Login screen with branded side panel, decorative background, and styled form card.
 */
public class LoginPanel extends JPanel {

    private final UserService userService = UserService.getInstance();
    private final Runnable onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JPanel errorBanner;
    private JButton loginButton;

    public LoginPanel(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(232, 246, 250),
                w, h, SwingUtils.BG);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(20, 83, 114, 18));
        g2.fill(new Ellipse2D.Double(-80, -60, 280, 280));
        g2.setColor(new Color(15, 118, 110, 14));
        g2.fill(new Ellipse2D.Double(w - 200, h - 220, 320, 320));
        g2.setColor(new Color(234, 88, 12, 10));
        g2.fill(new Ellipse2D.Double(w * 0.45, -40, 160, 160));

        g2.dispose();
    }

    private void buildUI() {
        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormArea(), BorderLayout.CENTER);
    }

    /** Left branded strip with gradient and feature highlights. */
    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, SwingUtils.PRIMARY_2,
                        getWidth(), getHeight(), SwingUtils.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(300, 0));
        brand.setMinimumSize(new Dimension(240, 0));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(48, 32, 48, 32));

        JLabel logo = new JLabel("Rx", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(SwingUtils.PRIMARY);
        logo.setOpaque(true);
        logo.setBackground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(new EmptyBorder(14, 22, 14, 22));
        logo.setMaximumSize(new Dimension(64, 64));

        JLabel name = new JLabel("MediCare");
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Pharmacy Management");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(255, 255, 255, 210));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logo);
        brand.add(Box.createVerticalStrut(20));
        brand.add(name);
        brand.add(Box.createVerticalStrut(4));
        brand.add(tagline);
        brand.add(Box.createVerticalStrut(32));
        brand.add(featureLine("Inventory & stock control"));
        brand.add(Box.createVerticalStrut(10));
        brand.add(featureLine("Point of sale & billing"));
        brand.add(Box.createVerticalStrut(10));
        brand.add(featureLine("Prescriptions & patient records"));
        brand.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Secure staff access only");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(new Color(255, 255, 255, 160));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(footer);
        return brand;
    }

    private JLabel featureLine(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(255, 255, 255, 230));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Center area with elevated login card. */
    private JPanel buildFormArea() {
        JPanel area = new JPanel(new GridBagLayout());
        area.setOpaque(false);
        area.setBorder(new EmptyBorder(24, 24, 24, 32));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(0, 0, 28, 0)));
        card.setPreferredSize(new Dimension(380, 460));
        card.setMaximumSize(new Dimension(420, 520));

        // Accent top strip
        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                        0, 0, SwingUtils.PRIMARY_2, getWidth(), 0, SwingUtils.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(0, 5));
        accent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        accent.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(accent);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(28, 36, 0, 36));
        inner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel signInTitle = new JLabel("Sign in");
        signInTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        signInTitle.setForeground(SwingUtils.TEXT_DARK);
        signInTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signInSub = new JLabel("Enter your credentials to continue");
        signInSub.setFont(SwingUtils.FONT_BODY);
        signInSub.setForeground(SwingUtils.TEXT_GRAY);
        signInSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = SwingUtils.styledField("Username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JPanel userFieldWrap = fieldWrap(usernameField);

        passwordField = SwingUtils.styledPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JPanel passFieldWrap = fieldWrap(passwordField);

        errorBanner = new JPanel(new BorderLayout());
        errorBanner.setBackground(new Color(254, 242, 242));
        errorBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        errorBanner.setVisible(false);
        errorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(SwingUtils.DANGER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorBanner.add(errorLabel, BorderLayout.CENTER);

        loginButton = SwingUtils.primaryButton("Sign In");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel hint = new JLabel("Use your assigned pharmacy account");
        hint.setFont(SwingUtils.FONT_SMALL);
        hint.setForeground(SwingUtils.TEXT_GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(signInTitle);
        inner.add(Box.createVerticalStrut(4));
        inner.add(signInSub);
        inner.add(Box.createVerticalStrut(24));
        inner.add(wrapLabeled("Username", userFieldWrap));
        inner.add(Box.createVerticalStrut(14));
        inner.add(wrapLabeled("Password", passFieldWrap));
        inner.add(Box.createVerticalStrut(12));
        inner.add(errorBanner);
        inner.add(Box.createVerticalStrut(14));
        inner.add(loginButton);
        inner.add(Box.createVerticalStrut(16));
        inner.add(hint);

        card.add(inner);

        area.add(card);

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        return area;
    }

    private JPanel fieldWrap(JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(2, 4, 2, 4)));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(field, BorderLayout.CENTER);

        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrap.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SwingUtils.PRIMARY, 2, true),
                        new EmptyBorder(2, 4, 2, 4)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrap.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                        new EmptyBorder(2, 4, 2, 4)));
            }
        });
        return wrap;
    }

    private JPanel wrapLabeled(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JLabel lbl = SwingUtils.fieldLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private void showError(String msg) {
        if (msg == null || msg.isBlank() || " ".equals(msg)) {
            errorBanner.setVisible(false);
            errorLabel.setText(" ");
        } else {
            errorLabel.setText(msg);
            errorBanner.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        SwingWorker<Result<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<User> doInBackground() {
                return userService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                try {
                    Result<User> result = get();
                    if (result.isSuccess()) {
                        passwordField.setText("");
                        showError(null);
                        onLoginSuccess.run();
                    } else {
                        showError(result.getErrorMessage());
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
