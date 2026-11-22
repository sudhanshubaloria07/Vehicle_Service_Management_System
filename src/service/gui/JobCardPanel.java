package service.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import service.billing.Bill;
import service.core.JobProgressThread;
import service.core.JobService;
import service.core.ProgressListener;
import service.core.ServiceJob;
import service.database.JobPartsDAO;
import service.database.VehicleDAO;
import service.exception.InvalidJobException;
import service.vehicle.Vehicle;

/**
 * Job Card Tracking panel providing real-time job status tracking, polymorphic vehicle
 * inspection, parts-used inspection, and multithreaded service simulation.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - JobCardPanel extends JPanel
 * OOP CONCEPT: Multithreading (Unit 3) - Launches JobProgressThread; updates GUI via SwingUtilities.invokeLater
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Displays vehicle.getVehicleDetails()
 * OOP CONCEPT: Custom Exception Handling (Unit 3) - Handles InvalidJobException on search and invalid transitions
 */
public class JobCardPanel extends JPanel implements ProgressListener {

    private final MainFrame mainFrame;
    private final JobService jobService;
    private final VehicleDAO vehicleDAO;
    private final JobPartsDAO jobPartsDAO;

    // Search
    private JTextField txtSearchJobId;
    private JButton btnSearch;
    private JButton btnViewAll;

    // Left: Table
    private JTable jobsTable;
    private DefaultTableModel tableModel;

    // Right: Selected Job Card details
    private JLabel lblCardJobId;
    private JLabel lblCardVehicleInfo;
    private JLabel lblCardCustomer;
    private JLabel lblCardServiceType;
    private JLabel lblCardDate;
    private JLabel lblCardStatus;
    private JLabel lblCardLabour;

    // Status management & Simulation
    private JComboBox<String> comboStatus;
    private JButton btnUpdateStatus;
    private JButton btnStartSimulation;
    private JProgressBar progressBar;
    private JLabel lblSimulationStage;

    // Parts used table
    private JTable partsUsedTable;
    private DefaultTableModel partsTableModel;

    private ServiceJob selectedJob = null;

    public JobCardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.jobService = new JobService();
        this.vehicleDAO = new VehicleDAO();
        this.jobPartsDAO = new JobPartsDAO();

        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Top Bar: Search by Job ID ---
        JPanel topBar = Theme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JLabel lblSearch = new JLabel("Search Job Card:");
        lblSearch.setFont(Theme.FONT_SUBTITLE);
        lblSearch.setForeground(Theme.TEXT_DARK);

        txtSearchJobId = new JTextField(16);
        txtSearchJobId.setFont(Theme.FONT_BODY);

        btnSearch = Theme.createPrimaryButton("Search Job");
        btnSearch.setPreferredSize(new Dimension(110, 32));
        btnSearch.addActionListener(e -> handleSearchJob());

        btnViewAll = Theme.createSecondaryButton("View All");
        btnViewAll.setPreferredSize(new Dimension(90, 32));
        btnViewAll.addActionListener(e -> {
            txtSearchJobId.setText("");
            refresh();
        });

        topBar.add(lblSearch);
        topBar.add(txtSearchJobId);
        topBar.add(btnSearch);
        topBar.add(btnViewAll);

        add(topBar, BorderLayout.NORTH);

        // --- 2. Left: Jobs Table ---
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JLabel lblTableTitle = new JLabel("Service Job Registry");
        lblTableTitle.setFont(Theme.FONT_HEADING);
        lblTableTitle.setForeground(Theme.TEXT_DARK);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = { "Job ID", "Reg No", "Customer", "Service Type", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        jobsTable = new JTable(tableModel);
        Theme.styleTable(jobsTable);
        jobsTable.getColumnModel().getColumn(4).setCellRenderer(Theme.createStatusCellRenderer());

        jobsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobsTable.getSelectedRow() != -1) {
                int modelRow = jobsTable.convertRowIndexToModel(jobsTable.getSelectedRow());
                String jobId = tableModel.getValueAt(modelRow, 0).toString();
                loadJobDetails(jobId);
            }
        });

        JScrollPane scrollTable = new JScrollPane(jobsTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // --- 3. Right: Detailed Job Card Viewer ---
        JPanel detailCard = Theme.createCardPanel();
        detailCard.setLayout(new BorderLayout(12, 12));
        detailCard.setPreferredSize(new Dimension(460, 600));

        JLabel lblDetailTitle = new JLabel("Active Job Card Inspector");
        lblDetailTitle.setFont(Theme.FONT_HEADING);
        lblDetailTitle.setForeground(Theme.TEXT_DARK);
        detailCard.add(lblDetailTitle, BorderLayout.NORTH);

        // Middle container: info grid + simulation controls
        JPanel detailContent = new JPanel();
        detailContent.setLayout(new BoxLayout(detailContent, BoxLayout.Y_AXIS));
        detailContent.setOpaque(false);

        // Info Grid
        JPanel infoGrid = new JPanel(new GridBagLayout());
        infoGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        lblCardJobId = new JLabel("Select a job to view details");
        lblCardJobId.setFont(Theme.FONT_HEADING);
        lblCardJobId.setForeground(Theme.BLUE_ACCENT);

        lblCardVehicleInfo = new JLabel("-");
        lblCardCustomer = new JLabel("-");
        lblCardServiceType = new JLabel("-");
        lblCardDate = new JLabel("-");
        lblCardStatus = new JLabel("-");
        lblCardLabour = new JLabel("-");

        int r = 0;
        addDetailRow(infoGrid, gbc, "Job ID:", lblCardJobId, r++);
        addDetailRow(infoGrid, gbc, "Vehicle Info (Polymorphic):", lblCardVehicleInfo, r++);
        addDetailRow(infoGrid, gbc, "Customer & Contact:", lblCardCustomer, r++);
        addDetailRow(infoGrid, gbc, "Service Category:", lblCardServiceType, r++);
        addDetailRow(infoGrid, gbc, "Booking Date:", lblCardDate, r++);
        addDetailRow(infoGrid, gbc, "Current Status:", lblCardStatus, r++);
        addDetailRow(infoGrid, gbc, "Labour Charges:", lblCardLabour, r++);

        detailContent.add(infoGrid);
        detailContent.add(Box.createRigidArea(new Dimension(0, 10)));

        // Status update controls
        JPanel statusUpdatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusUpdatePanel.setOpaque(false);
        comboStatus = new JComboBox<>(new String[]{ "Pending", "In Progress", "Completed" });
        btnUpdateStatus = Theme.createPrimaryButton("Update Status");
        btnUpdateStatus.setPreferredSize(new Dimension(130, 32));
        btnUpdateStatus.addActionListener(e -> handleUpdateStatus());

        statusUpdatePanel.add(new JLabel("Change Status:"));
        statusUpdatePanel.add(comboStatus);
        statusUpdatePanel.add(btnUpdateStatus);
        detailContent.add(statusUpdatePanel);
        detailContent.add(Box.createRigidArea(new Dimension(0, 8)));

        // Multithreaded Service Simulation Section
        JPanel simPanel = new JPanel();
        simPanel.setLayout(new BoxLayout(simPanel, BoxLayout.Y_AXIS));
        simPanel.setOpaque(false);
        simPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR), "Automated Service Simulation (Multithreaded)"));

        btnStartSimulation = Theme.createCustomButton("Start Service Simulation", Theme.NAVY, new Color(0x1B, 0x3B, 0x6E), Color.WHITE);
        btnStartSimulation.setPreferredSize(new Dimension(220, 34));
        btnStartSimulation.setMaximumSize(new Dimension(300, 34));
        btnStartSimulation.addActionListener(e -> handleStartSimulation());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Theme.BLUE_ACCENT);
        progressBar.setPreferredSize(new Dimension(380, 24));

        lblSimulationStage = new JLabel("Status: Ready to simulate service stages.");
        lblSimulationStage.setFont(Theme.FONT_SMALL);
        lblSimulationStage.setForeground(Theme.TEXT_MUTED);

        JPanel btnSimWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnSimWrapper.setOpaque(false);
        btnSimWrapper.add(btnStartSimulation);

        simPanel.add(btnSimWrapper);
        simPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        simPanel.add(progressBar);
        simPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        simPanel.add(lblSimulationStage);

        detailContent.add(simPanel);
        detailContent.add(Box.createRigidArea(new Dimension(0, 10)));

        // Parts Used Table
        JLabel lblPartsUsed = new JLabel("Spare Parts Allocated to this Job:");
        lblPartsUsed.setFont(Theme.FONT_SUBTITLE);
        lblPartsUsed.setForeground(Theme.TEXT_DARK);
        detailContent.add(lblPartsUsed);
        detailContent.add(Box.createRigidArea(new Dimension(0, 4)));

        String[] pCols = { "Part Name", "Price (Rs)", "Qty", "Total (Rs)" };
        partsTableModel = new DefaultTableModel(pCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        partsUsedTable = new JTable(partsTableModel);
        Theme.styleTable(partsUsedTable);

        JScrollPane scrollParts = new JScrollPane(partsUsedTable);
        scrollParts.setPreferredSize(new Dimension(420, 110));
        scrollParts.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        detailContent.add(scrollParts);

        detailCard.add(detailContent, BorderLayout.CENTER);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, detailCard);
        splitPane.setDividerLocation(420);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    private void addDetailRow(JPanel p, GridBagConstraints gbc, String label, JLabel val, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.65;
        val.setFont(Theme.FONT_BODY_BOLD);
        p.add(val, gbc);
    }

    private void handleSearchJob() {
        String query = txtSearchJobId.getText().trim();
        try {
            // OOP CONCEPT: Throws and catches custom InvalidJobException
            ServiceJob job = jobService.getJobDetails(query);
            loadJobDetails(job.getJobId());
        } catch (InvalidJobException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Job Not Found", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadJobDetails(String jobId) {
        try {
            selectedJob = jobService.getJobDetails(jobId);
            lblCardJobId.setText(selectedJob.getJobId());

            // OOP CONCEPT: Polymorphic call to getVehicleDetails()
            Vehicle v = vehicleDAO.getVehicleById(selectedJob.getVehicleId());
            if (v != null) {
                lblCardVehicleInfo.setText(v.getVehicleDetails());
                lblCardCustomer.setText(v.getCustomer().getName() + " (" + v.getCustomer().getPhone() + ")");
            } else {
                lblCardVehicleInfo.setText(selectedJob.getVehicleDetails());
                lblCardCustomer.setText(selectedJob.getCustomerName() + " (" + selectedJob.getCustomerPhone() + ")");
            }

            lblCardServiceType.setText(selectedJob.getServiceType());
            lblCardDate.setText(selectedJob.getBookingDate());
            lblCardStatus.setText(selectedJob.getJobStatus());
            lblCardLabour.setText(String.format("Rs. %.2f", selectedJob.getLabourCharges()));

            comboStatus.setSelectedItem(selectedJob.getJobStatus());

            // Load assigned parts
            partsTableModel.setRowCount(0);
            List<Bill.LineItem> parts = jobPartsDAO.getPartsForJob(jobId);
            for (Bill.LineItem item : parts) {
                partsTableModel.addRow(new Object[]{
                    item.getPartName(),
                    String.format("%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("%.2f", item.getLineTotal())
                });
            }

            // Reset simulation display
            progressBar.setValue("Completed".equalsIgnoreCase(selectedJob.getJobStatus()) ? 100 : 0);
            lblSimulationStage.setText("Status: Job is " + selectedJob.getJobStatus());

        } catch (InvalidJobException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Loading Job", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateStatus() {
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus = comboStatus.getSelectedItem().toString();
        try {
            // Validates state transition
            boolean ok = jobService.updateJobStatus(selectedJob.getJobId(), newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Job status successfully updated to: " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                refresh();
                loadJobDetails(selectedJob.getJobId());
                mainFrame.notifyDataChanged();
            }
        } catch (InvalidJobException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Illegal Status Transition", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Starts multithreaded service simulation.
     * 
     * OOP CONCEPT: Multithreading (Unit 3)
     * Creates JobProgressThread (implements Runnable) and starts it inside a new Thread.
     */
    private void handleStartSimulation() {
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job from the table to simulate.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Completed".equalsIgnoreCase(selectedJob.getJobStatus())) {
            JOptionPane.showMessageDialog(this, "Job " + selectedJob.getJobId() + " is already Completed.", "Cannot Simulate", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (JobProgressThread.isJobActive(selectedJob.getJobId())) {
            JOptionPane.showMessageDialog(this, "Simulation is already actively running for this job.", "Busy", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnStartSimulation.setEnabled(false);
        progressBar.setValue(0);
        lblSimulationStage.setText("Starting simulation...");

        // OOP CONCEPT: Multithreading - Runnable wrapped in Thread
        JobProgressThread simulationTask = new JobProgressThread(selectedJob.getJobId(), this);
        Thread simThread = new Thread(simulationTask, "SimThread-" + selectedJob.getJobId());
        simThread.start();
    }

    // ProgressListener Callbacks (Safely executed on the EDT)
    @Override
    public void onStageChanged(String stageName, int percent) {
        progressBar.setValue(percent);
        lblSimulationStage.setText("Stage: " + stageName + " (" + percent + "%)");
    }

    @Override
    public void onFinished() {
        btnStartSimulation.setEnabled(true);
        progressBar.setValue(100);
        lblSimulationStage.setText("Simulation Finished: Job Marked as Completed.");
        JOptionPane.showMessageDialog(this,
                "Service simulation completed successfully!\n"
                + "Job " + (selectedJob != null ? selectedJob.getJobId() : "") + " is now Completed.",
                "Service Done", JOptionPane.INFORMATION_MESSAGE);

        refresh();
        if (selectedJob != null) {
            loadJobDetails(selectedJob.getJobId());
        }
        mainFrame.notifyDataChanged();
    }

    @Override
    public void onError(String errorMessage) {
        btnStartSimulation.setEnabled(true);
        lblSimulationStage.setText("Simulation Error: " + errorMessage);
        JOptionPane.showMessageDialog(this, errorMessage, "Simulation Error", JOptionPane.ERROR_MESSAGE);
    }

    public void refresh() {
        try {
            tableModel.setRowCount(0);
            List<ServiceJob> jobs = jobService.getAllJobs();
            for (ServiceJob j : jobs) {
                tableModel.addRow(new Object[]{
                    j.getJobId(),
                    j.getRegistrationNumber(),
                    j.getCustomerName(),
                    j.getServiceType(),
                    j.getJobStatus()
                });
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing jobs table: " + e.getMessage());
        }
    }
}
