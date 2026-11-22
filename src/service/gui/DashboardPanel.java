package service.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import service.core.ServiceJob;
import service.database.DashboardDAO;

/**
 * Modern dashboard panel showcasing live system KPIs, quick actions,
 * and a recent service appointments table.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - DashboardPanel extends JPanel
 * OOP CONCEPT: Math API (Unit 1) - Calculation of service completion rates
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final DashboardDAO dashboardDAO;

    // Stat cards
    private StatCard cardVehicles;
    private StatCard cardBookings;
    private StatCard cardPending;
    private StatCard cardInProgress;
    private StatCard cardCompleted;
    private StatCard cardLowStock;

    // Recent jobs table
    private JTable recentJobsTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardDAO = new DashboardDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Top Section: 6 Stat Cards ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 6, 12, 0));
        statsPanel.setOpaque(false);

        cardVehicles   = new StatCard("Total Vehicles", "0", "Registered Units", Theme.BLUE_ACCENT);
        cardBookings   = new StatCard("Total Bookings", "0", "All-time Jobs", new Color(0x6F, 0x42, 0xC1));
        cardPending    = new StatCard("Pending Jobs",   "0", "Awaiting Service", Theme.WARNING);
        cardInProgress = new StatCard("In Progress",    "0", "Under Repair", Theme.BLUE_ACCENT);
        cardCompleted  = new StatCard("Completed",      "0", "Ready for Delivery", Theme.SUCCESS);
        cardLowStock   = new StatCard("Low Stock Parts","0", "Reorder Required (<5)", Theme.DANGER);

        statsPanel.add(cardVehicles);
        statsPanel.add(cardBookings);
        statsPanel.add(cardPending);
        statsPanel.add(cardInProgress);
        statsPanel.add(cardCompleted);
        statsPanel.add(cardLowStock);

        // --- 2. Middle Section: Quick Action Buttons ---
        JPanel quickActionsCard = Theme.createCardPanel();
        quickActionsCard.setLayout(new BorderLayout(10, 10));

        JLabel lblQuickActions = new JLabel("Quick Actions & Workflows");
        lblQuickActions.setFont(Theme.FONT_HEADING);
        lblQuickActions.setForeground(Theme.TEXT_DARK);
        quickActionsCard.add(lblQuickActions, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        btnRow.setOpaque(false);

        JButton btnBook = Theme.createPrimaryButton("+ Book Service");
        btnBook.addActionListener(e -> mainFrame.showScreen("BOOKING"));

        JButton btnRegister = Theme.createCustomButton("+ Register Vehicle", Theme.NAVY, new Color(0x1B, 0x3B, 0x6E), Color.WHITE);
        btnRegister.addActionListener(e -> mainFrame.showScreen("VEHICLES"));

        JButton btnJobCard = Theme.createCustomButton("Job Card Tracker", new Color(0x6F, 0x42, 0xC1), new Color(0x59, 0x33, 0x9D), Color.WHITE);
        btnJobCard.addActionListener(e -> mainFrame.showScreen("JOBS"));

        JButton btnInventory = Theme.createCustomButton("Spare Parts", new Color(0x00, 0x96, 0x88), new Color(0x00, 0x79, 0x6B), Color.WHITE);
        btnInventory.addActionListener(e -> mainFrame.showScreen("PARTS"));

        JButton btnBilling = Theme.createSuccessButton("Generate Invoice");
        btnBilling.addActionListener(e -> mainFrame.showScreen("BILLING"));

        btnRow.add(btnBook);
        btnRow.add(btnRegister);
        btnRow.add(btnJobCard);
        btnRow.add(btnInventory);
        btnRow.add(btnBilling);

        quickActionsCard.add(btnRow, BorderLayout.CENTER);

        // --- 3. Bottom Section: Recent Jobs Table Card ---
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblRecent = new JLabel("Recent Service Appointments & Status");
        lblRecent.setFont(Theme.FONT_HEADING);
        lblRecent.setForeground(Theme.TEXT_DARK);
        tableCard.add(lblRecent, BorderLayout.NORTH);

        String[] cols = { "Job ID", "Registration No", "Customer Name", "Service Type", "Booking Date", "Status", "Labour (Rs)" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // Non-editable JTable
            }
        };

        recentJobsTable = new JTable(tableModel);
        Theme.styleTable(recentJobsTable);
        recentJobsTable.getColumnModel().getColumn(5).setCellRenderer(Theme.createStatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(recentJobsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Combine into layout
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(statsPanel);
        topContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        topContainer.add(quickActionsCard);

        add(topContainer, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    /**
     * Refreshes dashboard metrics and recent appointments table from database.
     */
    public void refresh() {
        try {
            int vehicles = dashboardDAO.getTotalVehicles();
            int bookings = dashboardDAO.getTotalBookings();
            int pending = dashboardDAO.getPendingJobsCount();
            int inProgress = dashboardDAO.getInProgressJobsCount();
            int completed = dashboardDAO.getCompletedJobsCount();
            int lowStock = dashboardDAO.getLowStockPartsCount();

            cardVehicles.setValue(vehicles);
            cardBookings.setValue(bookings);
            cardPending.setValue(pending);
            cardInProgress.setValue(inProgress);
            cardCompleted.setValue(completed);
            cardLowStock.setValue(lowStock);

            // Populate recent jobs table
            tableModel.setRowCount(0);
            List<ServiceJob> recent = dashboardDAO.getRecentJobs(10);
            for (ServiceJob j : recent) {
                tableModel.addRow(new Object[]{
                    j.getJobId(),
                    j.getRegistrationNumber(),
                    j.getCustomerName(),
                    j.getServiceType(),
                    j.getBookingDate(),
                    j.getJobStatus(),
                    String.format("%.2f", j.getLabourCharges())
                });
            }
        } catch (SQLException e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }
}
