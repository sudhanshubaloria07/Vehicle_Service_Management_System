package service.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import service.core.JobService;
import service.core.ServiceJob;
import service.database.VehicleDAO;
import service.exception.InvalidJobException;
import service.vehicle.Vehicle;

/**
 * Service Booking screen where customer vehicles are booked for service jobs.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - ServiceBookingPanel extends JPanel
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Auto-calculates labour charges based on dynamic vehicle type
 * OOP CONCEPT: Exception Handling (Unit 3) - Input validation & transactional job creation
 */
public class ServiceBookingPanel extends JPanel {

    /**
     * Helper wrapper for displaying vehicles inside a JComboBox.
     */
    public static class VehicleItem {
        private final Vehicle vehicle;

        public VehicleItem(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        @Override
        public String toString() {
            if (vehicle == null) return "-- Select Registered Vehicle --";
            return vehicle.getRegistrationNumber() + " - " 
                    + vehicle.getBrand() + " " + vehicle.getModel() 
                    + " (" + vehicle.getCustomer().getName() + " | " + vehicle.getVehicleType() + ")";
        }
    }

    private final MainFrame mainFrame;
    private final JobService jobService;
    private final VehicleDAO vehicleDAO;

    // Form inputs
    private JComboBox<VehicleItem> comboVehicles;
    private JComboBox<String> comboServiceType;
    private JTextField txtBookingDate;
    private JLabel lblLabourCalcDetails;

    // Table
    private JTable bookingsTable;
    private DefaultTableModel tableModel;

    public ServiceBookingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.jobService = new JobService();
        this.vehicleDAO = new VehicleDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Top Section: Booking Form Card ---
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout(12, 12));

        JLabel lblTitle = new JLabel("Book Service Job & Auto-Calculate Labour");
        lblTitle.setFont(Theme.FONT_HEADING);
        lblTitle.setForeground(Theme.TEXT_DARK);
        formCard.add(lblTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboVehicles = new JComboBox<>();
        comboServiceType = new JComboBox<>(new String[]{
            "General Service", "Engine Repair", "Oil Change", "Brake Inspection"
        });
        txtBookingDate = new JTextField(LocalDate.now().toString());

        lblLabourCalcDetails = new JLabel("Select a vehicle and service type to compute labour charges.");
        lblLabourCalcDetails.setFont(Theme.FONT_BODY_BOLD);
        lblLabourCalcDetails.setForeground(Theme.BLUE_ACCENT);

        // Update labour calculation dynamically when vehicle or service type changes
        comboVehicles.addActionListener(e -> updateCalculatedLabour());
        comboServiceType.addActionListener(e -> updateCalculatedLabour());

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        grid.add(createLabel("Registered Vehicle *:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        grid.add(createLabel("Service Category *:"), gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        grid.add(comboVehicles, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        grid.add(comboServiceType, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        grid.add(createLabel("Booking Date (YYYY-MM-DD) *:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        grid.add(createLabel("Polymorphic Labour Estimate:"), gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3;
        grid.add(txtBookingDate, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        grid.add(lblLabourCalcDetails, gbc);

        formCard.add(grid, BorderLayout.CENTER);

        // Book Button Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnBook = Theme.createPrimaryButton("+ Confirm & Book Job");
        btnBook.setPreferredSize(new Dimension(190, 36));
        btnBook.addActionListener(e -> handleBookService());

        btnPanel.add(btnBook);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        // --- 2. Bottom Section: All Bookings Table Card ---
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblTableTitle = new JLabel("All Service Bookings & Active Job Cards");
        lblTableTitle.setFont(Theme.FONT_HEADING);
        lblTableTitle.setForeground(Theme.TEXT_DARK);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = { "Job ID", "Registration No", "Customer Name", "Service Type", "Booking Date", "Status", "Labour Fee (Rs)" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        Theme.styleTable(bookingsTable);
        bookingsTable.getColumnModel().getColumn(5).setCellRenderer(Theme.createStatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(formCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        return lbl;
    }

    /**
     * Updates the labour charge display in real time using runtime polymorphism.
     * 
     * OOP CONCEPT: Runtime Polymorphism (Unit 2)
     * Calls jobService.calculateLabourCharge(vehicle, serviceType)
     * which calls vehicle.calculateLabourSurcharge(baseLabour), dynamically executing
     * either Car.getLabourMultiplier() or Bike.getLabourMultiplier().
     */
    private void updateCalculatedLabour() {
        VehicleItem item = (VehicleItem) comboVehicles.getSelectedItem();
        if (item == null || item.getVehicle() == null) {
            lblLabourCalcDetails.setText("Select a vehicle to estimate charges.");
            lblLabourCalcDetails.setForeground(Theme.TEXT_MUTED);
            return;
        }

        Vehicle v = item.getVehicle();
        String serviceType = comboServiceType.getSelectedItem().toString();
        double baseRate = JobService.getBaseLabourRate(serviceType);
        // OOP CONCEPT: Polymorphic call
        double finalLabour = jobService.calculateLabourCharge(v, serviceType);

        String text = String.format("Base: Rs. %.2f | Multiplier: %.1fx (%s) | Final Labour: Rs. %.2f",
                baseRate, v.getLabourMultiplier(), v.getVehicleType(), finalLabour);
        lblLabourCalcDetails.setText(text);
        lblLabourCalcDetails.setForeground(Theme.BLUE_ACCENT);
    }

    private void handleBookService() {
        VehicleItem item = (VehicleItem) comboVehicles.getSelectedItem();
        if (item == null || item.getVehicle() == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered vehicle for booking.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String serviceType = comboServiceType.getSelectedItem().toString();
        String dateStr = txtBookingDate.getText().trim();

        try {
            // Service booking creates unique sequential ID inside transaction
            String jobId = jobService.bookService(item.getVehicle().getVehicleId(), serviceType, dateStr);

            JOptionPane.showMessageDialog(this,
                    "Service appointment successfully booked!\n\n"
                    + "Generated Job ID: " + jobId + "\n"
                    + "Vehicle: " + item.getVehicle().getRegistrationNumber() + "\n"
                    + "Service Category: " + serviceType + "\n"
                    + "Initial Status: Pending",
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

            refresh();
            mainFrame.notifyDataChanged();

        } catch (IllegalArgumentException | InvalidJobException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        // Refresh vehicles dropdown
        try {
            comboVehicles.removeAllItems();
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            for (Vehicle v : vehicles) {
                comboVehicles.addItem(new VehicleItem(v));
            }
            updateCalculatedLabour();
        } catch (SQLException e) {
            System.err.println("Error populating vehicle combo: " + e.getMessage());
        }

        // Refresh bookings table
        try {
            tableModel.setRowCount(0);
            List<ServiceJob> jobs = jobService.getAllJobs();
            for (ServiceJob j : jobs) {
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
            System.err.println("Error loading bookings table: " + e.getMessage());
        }
    }
}
