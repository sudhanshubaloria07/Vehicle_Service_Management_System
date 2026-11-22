package service.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import service.database.DBConnection;
import service.database.DatabaseInitializer;

/**
 * Main application window for the Vehicle Service Management System (VSMS).
 * Provides a fixed dark navy sidebar, digital ticking clock header, card layout workspace,
 * and live database status footer.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - MainFrame extends JFrame
 * OOP CONCEPT: Multithreading & Event Handling (Unit 3) - javax.swing.Timer for live clock on EDT
 */
public class MainFrame extends JFrame {

    // Sidebar buttons
    private SidebarButton btnDashboard;
    private SidebarButton btnVehicles;
    private SidebarButton btnBooking;
    private SidebarButton btnJobs;
    private SidebarButton btnParts;
    private SidebarButton btnBilling;

    // Header labels
    private JLabel lblHeaderTitle;
    private JLabel lblDigitalClock;

    // Footer label
    private JLabel lblDbStatus;

    // Card Layout
    private JPanel centerCardContainer;
    private CardLayout cardLayout;

    // Screen panels
    private DashboardPanel dashboardPanel;
    private CustomerVehiclePanel customerVehiclePanel;
    private ServiceBookingPanel serviceBookingPanel;
    private JobCardPanel jobCardPanel;
    private PartsPanel partsPanel;
    private BillingPanel billingPanel;

    public MainFrame() {
        setTitle("VSMS - AutoCare Vehicle Service Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null); // Centered on screen

        initComponents();
        setupClockTimer();
        checkDatabaseStatus();
        showScreen("DASHBOARD");
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BACKGROUND);

        // --- 1. LEFT SIDEBAR (240px Dark Navy) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.NAVY_DARK);
        sidebar.setPreferredSize(new Dimension(240, getHeight()));

        // Logo / Title Header
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Theme.NAVY_DARK);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        logoPanel.setMaximumSize(new Dimension(240, 90));

        JLabel lblLogo = new JLabel("AutoCare VSMS");
        lblLogo.setFont(Theme.FONT_TITLE);
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSubLogo = new JLabel("Automotive Service Suite");
        lblSubLogo.setFont(Theme.FONT_SMALL);
        lblSubLogo.setForeground(Theme.BLUE_ACCENT);

        logoPanel.add(lblLogo, BorderLayout.NORTH);
        logoPanel.add(lblSubLogo, BorderLayout.SOUTH);

        sidebar.add(logoPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Navigation Buttons
        btnDashboard = new SidebarButton("  Dashboard Overview");
        btnVehicles  = new SidebarButton("  Customers & Vehicles");
        btnBooking   = new SidebarButton("  Service Booking");
        btnJobs      = new SidebarButton("  Job Card Tracking");
        btnParts     = new SidebarButton("  Spare Parts & Stock");
        btnBilling   = new SidebarButton("  Service Billing");
        SidebarButton btnDbSetupNav = new SidebarButton("  ⚙ Database Setup");

        btnDashboard.addActionListener(e -> showScreen("DASHBOARD"));
        btnVehicles.addActionListener(e -> showScreen("VEHICLES"));
        btnBooking.addActionListener(e -> showScreen("BOOKING"));
        btnJobs.addActionListener(e -> showScreen("JOBS"));
        btnParts.addActionListener(e -> showScreen("PARTS"));
        btnBilling.addActionListener(e -> showScreen("BILLING"));
        btnDbSetupNav.addActionListener(e -> showDatabaseSetupDialog());

        sidebar.add(btnDashboard);
        sidebar.add(btnVehicles);
        sidebar.add(btnBooking);
        sidebar.add(btnJobs);
        sidebar.add(btnParts);
        sidebar.add(btnBilling);
        sidebar.add(btnDbSetupNav);

        sidebar.add(Box.createVerticalGlue());

        // Sidebar Footer Credits
        JPanel credPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        credPanel.setOpaque(false);
        credPanel.setMaximumSize(new Dimension(240, 45));
        JLabel lblCred = new JLabel("B.Tech CSE - OOPJ 2026");
        lblCred.setFont(Theme.FONT_SMALL);
        lblCred.setForeground(Theme.TEXT_MUTED);
        credPanel.add(lblCred);
        sidebar.add(credPanel);

        rootPanel.add(sidebar, BorderLayout.WEST);

        // --- 2. TOP HEADER BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.CARD_WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        lblHeaderTitle = new JLabel("Dashboard Overview");
        lblHeaderTitle.setFont(Theme.FONT_TITLE);
        lblHeaderTitle.setForeground(Theme.TEXT_DARK);

        lblDigitalClock = new JLabel();
        lblDigitalClock.setFont(Theme.FONT_BODY_BOLD);
        lblDigitalClock.setForeground(Theme.TEXT_MUTED);

        JButton btnSetupDbTop = Theme.createCustomButton("⚙ Database Setup", Theme.NAVY, new Color(0x1B, 0x3B, 0x6E), Color.WHITE);
        btnSetupDbTop.setPreferredSize(new Dimension(145, 32));
        btnSetupDbTop.addActionListener(e -> showDatabaseSetupDialog());

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        topRight.setOpaque(false);
        topRight.add(lblDigitalClock);
        topRight.add(btnSetupDbTop);

        topBar.add(lblHeaderTitle, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        rootPanel.add(topBar, BorderLayout.NORTH);

        // --- 3. CENTER SCREENS (CardLayout) ---
        cardLayout = new CardLayout();
        centerCardContainer = new JPanel(cardLayout);
        centerCardContainer.setOpaque(false);

        dashboardPanel = new DashboardPanel(this);
        customerVehiclePanel = new CustomerVehiclePanel(this);
        serviceBookingPanel = new ServiceBookingPanel(this);
        jobCardPanel = new JobCardPanel(this);
        partsPanel = new PartsPanel(this);
        billingPanel = new BillingPanel(this);

        centerCardContainer.add(dashboardPanel, "DASHBOARD");
        centerCardContainer.add(customerVehiclePanel, "VEHICLES");
        centerCardContainer.add(serviceBookingPanel, "BOOKING");
        centerCardContainer.add(jobCardPanel, "JOBS");
        centerCardContainer.add(partsPanel, "PARTS");
        centerCardContainer.add(billingPanel, "BILLING");

        rootPanel.add(centerCardContainer, BorderLayout.CENTER);

        // --- 4. BOTTOM STATUS BAR ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.CARD_WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 24, 6, 24)
        ));

        JPanel statusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusLeft.setOpaque(false);

        lblDbStatus = new JLabel("Checking MySQL Database connection...");
        lblDbStatus.setFont(Theme.FONT_SMALL);

        JButton btnSetupDb = Theme.createCustomButton("⚙ Database Setup", Theme.NAVY, new Color(0x1B, 0x3B, 0x6E), Color.WHITE);
        btnSetupDb.setPreferredSize(new Dimension(135, 26));
        btnSetupDb.setFont(Theme.FONT_SMALL);
        btnSetupDb.addActionListener(e -> showDatabaseSetupDialog());

        statusLeft.add(lblDbStatus);
        statusLeft.add(btnSetupDb);

        JLabel lblVersion = new JLabel("Vehicle Service Management System v2.0 (Java Swing + MySQL)");
        lblVersion.setFont(Theme.FONT_SMALL);
        lblVersion.setForeground(Theme.TEXT_MUTED);

        statusBar.add(statusLeft, BorderLayout.WEST);
        statusBar.add(lblVersion, BorderLayout.EAST);

        rootPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Multithreaded live digital clock updating every 1000ms.
     * OOP CONCEPT: Multithreading (Unit 3) - javax.swing.Timer runs on EDT
     */
    private void setupClockTimer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  |  hh:mm:ss a");
        Timer clockTimer = new Timer(1000, e -> {
            lblDigitalClock.setText(LocalDateTime.now().format(dtf));
        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    /**
     * Checks database connectivity and updates status bar accordingly.
     */
    public void checkDatabaseStatus() {
        boolean connected = DBConnection.testConnection();
        if (connected) {
            lblDbStatus.setText("● Connected to " + DBConnection.getDatabaseMode());
            lblDbStatus.setForeground(Theme.SUCCESS);
        } else {
            lblDbStatus.setText("● Database Offline - Click ⚙ Database Setup");
            lblDbStatus.setForeground(Theme.DANGER);
        }
    }

    /**
     * Switches between panels in the CardLayout and highlights active button.
     * 
     * @param screenKey "DASHBOARD", "VEHICLES", "BOOKING", "JOBS", "PARTS", "BILLING"
     */
    public void showScreen(String screenKey) {
        cardLayout.show(centerCardContainer, screenKey);

        // Reset button states
        btnDashboard.setActive(false);
        btnVehicles.setActive(false);
        btnBooking.setActive(false);
        btnJobs.setActive(false);
        btnParts.setActive(false);
        btnBilling.setActive(false);

        switch (screenKey) {
            case "DASHBOARD":
                btnDashboard.setActive(true);
                lblHeaderTitle.setText("Dashboard Overview & System KPIs");
                dashboardPanel.refresh();
                break;
            case "VEHICLES":
                btnVehicles.setActive(true);
                lblHeaderTitle.setText("Customer & Vehicle Registration");
                customerVehiclePanel.refresh();
                break;
            case "BOOKING":
                btnBooking.setActive(true);
                lblHeaderTitle.setText("Service Booking & Polymorphic Labour Estimator");
                serviceBookingPanel.refresh();
                break;
            case "JOBS":
                btnJobs.setActive(true);
                lblHeaderTitle.setText("Job Card Tracking & Multithreaded Simulation");
                jobCardPanel.refresh();
                break;
            case "PARTS":
                btnParts.setActive(true);
                lblHeaderTitle.setText("Spare Parts Inventory & Transactional Job Allocation");
                partsPanel.refresh();
                break;
            case "BILLING":
                btnBilling.setActive(true);
                lblHeaderTitle.setText("Service Billing & Tax Invoice Generator");
                billingPanel.refresh();
                break;
        }
    }

    /**
     * Invoked whenever data changes anywhere in the app to synchronize all panels.
     */
    public void notifyDataChanged() {
        dashboardPanel.refresh();
        customerVehiclePanel.refresh();
        serviceBookingPanel.refresh();
        jobCardPanel.refresh();
        partsPanel.refresh();
        billingPanel.refresh();
        checkDatabaseStatus();
    }

    /**
     * Interactive database setup and initialization dialog allowing users to configure
     * MySQL credentials, test connectivity, and run schema.sql / sample_data.sql.
     */
    public void showDatabaseSetupDialog() {
        JDialog dialog = new JDialog(this, "MySQL Database Setup & Initializer", true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtHost = new JTextField("localhost");
        JTextField txtPort = new JTextField("3306");
        JTextField txtUser = new JTextField("root");
        JPasswordField txtPass = new JPasswordField();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("MySQL Host:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtHost, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtPort, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtPass, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));

        JButton btnTest = Theme.createCustomButton("Test Connection", Theme.NAVY, new Color(0x1B, 0x3B, 0x6E), Color.WHITE);
        btnTest.setPreferredSize(new Dimension(130, 32));
        btnTest.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txtPort.getText().trim());
                String testUrl = "jdbc:mysql://" + txtHost.getText().trim() + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
                boolean ok = DatabaseInitializer.testCredentials(testUrl, txtUser.getText().trim(), new String(txtPass.getPassword()));
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Connection successful! MySQL server is reachable.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Connection could not be established.", "Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnInit = Theme.createPrimaryButton("Initialize & Connect");
        btnInit.setPreferredSize(new Dimension(160, 32));
        btnInit.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txtPort.getText().trim());
                String host = txtHost.getText().trim();
                String user = txtUser.getText().trim();
                String pass = new String(txtPass.getPassword());

                DatabaseInitializer.initializeDatabase(host, port, user, pass);
                JOptionPane.showMessageDialog(dialog,
                        "Database 'vehicle_service_db' created & initialized with sample data successfully!\n"
                        + "Configuration saved to config/db.properties.",
                        "Initialization Complete", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                notifyDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Database initialization failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnTest);
        btnPanel.add(btnInit);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
