package service.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import service.billing.Bill;
import service.core.BillingService;
import service.core.ServiceJob;
import service.database.JobDAO;
import service.exception.InvalidJobException;

/**
 * Service Billing & Tax Invoice Generator screen.
 * Generates an itemized bill for completed jobs with 18% GST and Math.round() precision.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - BillingPanel extends JPanel
 * OOP CONCEPT: Interface Usage (Unit 2) - Bill implements Billable
 * OOP CONCEPT: Math API (Unit 1) - Accurate monetary calculations and rounding
 * OOP CONCEPT: Custom Exception Handling (Unit 3) - Blocks billing on non-completed jobs
 */
public class BillingPanel extends JPanel {

    public static class CompletedJobItem {
        private final ServiceJob job;
        public CompletedJobItem(ServiceJob job) { this.job = job; }
        public ServiceJob getJob() { return job; }
        @Override
        public String toString() {
            if (job == null) return "-- Select Completed Job --";
            return job.getJobId() + " [" + job.getRegistrationNumber() + " - " + job.getCustomerName() + " (" + job.getServiceType() + ")]";
        }
    }

    private final MainFrame mainFrame;
    private final BillingService billingService;
    private final JobDAO jobDAO;

    // Controls
    private JComboBox<CompletedJobItem> comboCompletedJobs;
    private JTextField txtManualJobId;
    private JButton btnGenerateBill;
    private JButton btnSaveBill;
    private JButton btnPrintBill;

    // Preview
    private JTextArea txtInvoicePreview;
    private JLabel lblGrandTotalBadge;

    private Bill currentBill = null;

    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.billingService = new BillingService();
        this.jobDAO = new JobDAO();

        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Top Section: Job Selector Bar ---
        JPanel topCard = Theme.createCardPanel();
        topCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JLabel lblSelect = new JLabel("Completed Jobs:");
        lblSelect.setFont(Theme.FONT_SUBTITLE);

        comboCompletedJobs = new JComboBox<>();
        comboCompletedJobs.setPreferredSize(new Dimension(320, 32));

        JLabel lblOr = new JLabel("OR Job ID:");
        lblOr.setFont(Theme.FONT_SMALL);
        lblOr.setForeground(Theme.TEXT_MUTED);

        txtManualJobId = new JTextField(12);
        txtManualJobId.setFont(Theme.FONT_BODY);

        btnGenerateBill = Theme.createPrimaryButton("Generate Bill");
        btnGenerateBill.setPreferredSize(new Dimension(130, 32));
        btnGenerateBill.addActionListener(e -> handleGenerateBill());

        btnSaveBill = Theme.createSuccessButton("Save as .txt");
        btnSaveBill.setPreferredSize(new Dimension(120, 32));
        btnSaveBill.addActionListener(e -> handleSaveBillAsText());

        btnPrintBill = Theme.createCustomButton("Print", Theme.NAVY, new java.awt.Color(0x1B, 0x3B, 0x6E), java.awt.Color.WHITE);
        btnPrintBill.setPreferredSize(new Dimension(80, 32));
        btnPrintBill.addActionListener(e -> handlePrintBill());

        topCard.add(lblSelect);
        topCard.add(comboCompletedJobs);
        topCard.add(lblOr);
        topCard.add(txtManualJobId);
        topCard.add(btnGenerateBill);
        topCard.add(btnSaveBill);
        topCard.add(btnPrintBill);

        add(topCard, BorderLayout.NORTH);

        // --- 2. Center Section: Monospaced Invoice Preview ---
        JPanel invoiceCard = Theme.createCardPanel();
        invoiceCard.setLayout(new BorderLayout(10, 10));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblPreviewTitle = new JLabel("Official Tax Invoice / Bill Preview");
        lblPreviewTitle.setFont(Theme.FONT_HEADING);
        lblPreviewTitle.setForeground(Theme.TEXT_DARK);

        lblGrandTotalBadge = new JLabel("Grand Total: Rs. 0.00");
        lblGrandTotalBadge.setFont(Theme.FONT_HEADING);
        lblGrandTotalBadge.setForeground(Theme.SUCCESS);

        headerRow.add(lblPreviewTitle, BorderLayout.WEST);
        headerRow.add(lblGrandTotalBadge, BorderLayout.EAST);
        invoiceCard.add(headerRow, BorderLayout.NORTH);

        txtInvoicePreview = new JTextArea();
        txtInvoicePreview.setEditable(false);
        txtInvoicePreview.setFont(Theme.FONT_MONO);
        txtInvoicePreview.setBackground(Theme.CARD_WHITE);
        txtInvoicePreview.setForeground(Theme.TEXT_DARK);
        txtInvoicePreview.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        txtInvoicePreview.setText(
            "\n" +
            "  Please select a Completed Job and click 'Generate Bill' to inspect\n" +
            "  the itemized invoice, tax calculations, and printable receipt.\n"
        );

        JScrollPane scrollPane = new JScrollPane(txtInvoicePreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        invoiceCard.add(scrollPane, BorderLayout.CENTER);

        add(invoiceCard, BorderLayout.CENTER);
    }

    private void handleGenerateBill() {
        String jobId = txtManualJobId.getText().trim();
        if (jobId.isEmpty()) {
            CompletedJobItem item = (CompletedJobItem) comboCompletedJobs.getSelectedItem();
            if (item != null && item.getJob() != null) {
                jobId = item.getJob().getJobId();
            }
        }

        if (jobId == null || jobId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter a Job ID.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // OOP CONCEPT: Interface & Exception Handling
            currentBill = billingService.generateBill(jobId);
            String invoiceText = currentBill.generateInvoiceText();
            txtInvoicePreview.setText(invoiceText);
            txtInvoicePreview.setCaretPosition(0);

            lblGrandTotalBadge.setText(String.format("Grand Total: Rs. %.2f", currentBill.getGrandTotal()));

        } catch (InvalidJobException ex) {
            // Friendly error for non-completed job or invalid ID
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Billing Constraint Violated", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveBillAsText() {
        if (currentBill == null) {
            JOptionPane.showMessageDialog(this, "Please generate an invoice before saving.", "No Bill Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Tax Invoice");
        fileChooser.setSelectedFile(new File("Invoice_" + currentBill.getJob().getJobId() + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(currentBill.generateInvoiceText());
                JOptionPane.showMessageDialog(this, "Invoice successfully saved to:\n" + fileToSave.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePrintBill() {
        if (currentBill == null) {
            JOptionPane.showMessageDialog(this, "Please generate an invoice first.", "No Bill Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean complete = txtInvoicePreview.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing completed.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        try {
            comboCompletedJobs.removeAllItems();
            List<ServiceJob> jobs = jobDAO.getAllJobs();
            for (ServiceJob j : jobs) {
                if ("Completed".equalsIgnoreCase(j.getJobStatus())) {
                    comboCompletedJobs.addItem(new CompletedJobItem(j));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing completed jobs combo: " + e.getMessage());
        }
    }
}
