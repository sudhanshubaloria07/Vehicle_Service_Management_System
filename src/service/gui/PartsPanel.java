package service.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import service.billing.Bill;
import service.core.Part;
import service.core.PartService;
import service.core.ServiceJob;
import service.core.Validator;
import service.database.JobDAO;
import service.database.JobPartsDAO;
import service.exception.InvalidJobException;

/**
 * Spare Parts Inventory & Job Part Allocation panel.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - PartsPanel extends JPanel
 * OOP CONCEPT: Transaction Management (Unit 3) - Atomic stock reduction with SELECT ... FOR UPDATE
 * OOP CONCEPT: Math API (Unit 1) - Stock validation using Math.max()
 */
public class PartsPanel extends JPanel {

    public static class JobComboItem {
        private final ServiceJob job;
        public JobComboItem(ServiceJob job) { this.job = job; }
        public ServiceJob getJob() { return job; }
        @Override
        public String toString() {
            if (job == null) return "-- Select Active Job --";
            return job.getJobId() + " (" + job.getRegistrationNumber() + " - " + job.getJobStatus() + ")";
        }
    }

    public static class PartComboItem {
        private final Part part;
        public PartComboItem(Part part) { this.part = part; }
        public Part getPart() { return part; }
        @Override
        public String toString() {
            if (part == null) return "-- Select Spare Part --";
            return part.getPartName() + " [Stock: " + part.getQuantity() + " | Rs. " + String.format("%.2f", part.getPrice()) + "]";
        }
    }

    private final MainFrame mainFrame;
    private final PartService partService;
    private final JobDAO jobDAO;
    private final JobPartsDAO jobPartsDAO;

    // Part Form
    private JTextField txtPartId;
    private JTextField txtPartName;
    private JTextField txtPartPrice;
    private JTextField txtPartQuantity;
    private JButton btnAddPart;
    private JButton btnUpdatePart;
    private JButton btnDeletePart;
    private JButton btnClearForm;

    // Search & Inventory Table
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JTable partsTable;
    private DefaultTableModel partsTableModel;

    // Assign Part Section
    private JComboBox<JobComboItem> comboJobs;
    private JComboBox<PartComboItem> comboParts;
    private JSpinner spinnerQuantity;
    private JButton btnAssignPart;

    // Assigned Parts Table
    private JTable assignedPartsTable;
    private DefaultTableModel assignedTableModel;

    public PartsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.partService = new PartService();
        this.jobDAO = new JobDAO();
        this.jobPartsDAO = new JobPartsDAO();

        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Left Container: CRUD Form & Assign Section ---
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setOpaque(false);
        leftContainer.setPreferredSize(new Dimension(380, 600));

        // Part Form Card
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout(8, 8));

        JLabel lblFormTitle = new JLabel("Spare Part Details");
        lblFormTitle.setFont(Theme.FONT_HEADING);
        lblFormTitle.setForeground(Theme.TEXT_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        txtPartId = new JTextField();
        txtPartId.setEditable(false);
        txtPartId.setBackground(new Color(240, 240, 240));

        txtPartName = new JTextField();
        txtPartPrice = new JTextField();
        txtPartQuantity = new JTextField();

        int row = 0;
        addFormField(formGrid, gbc, "Part ID (Auto):", txtPartId, row++);
        addFormField(formGrid, gbc, "Part Name / Description *:", txtPartName, row++);
        addFormField(formGrid, gbc, "Unit Price (Rs.) *:", txtPartPrice, row++);
        addFormField(formGrid, gbc, "Stock Quantity *:", txtPartQuantity, row++);

        formCard.add(formGrid, BorderLayout.CENTER);

        // Form Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        btnPanel.setOpaque(false);
        btnAddPart = Theme.createPrimaryButton("Add Part");
        btnUpdatePart = Theme.createSuccessButton("Update");
        btnDeletePart = Theme.createDangerButton("Delete");
        btnClearForm = Theme.createSecondaryButton("Clear");

        btnAddPart.setPreferredSize(new Dimension(85, 32));
        btnUpdatePart.setPreferredSize(new Dimension(80, 32));
        btnDeletePart.setPreferredSize(new Dimension(80, 32));
        btnClearForm.setPreferredSize(new Dimension(75, 32));

        btnAddPart.addActionListener(e -> handleAddPart());
        btnUpdatePart.addActionListener(e -> handleUpdatePart());
        btnDeletePart.addActionListener(e -> handleDeletePart());
        btnClearForm.addActionListener(e -> clearForm());

        btnPanel.add(btnAddPart);
        btnPanel.add(btnUpdatePart);
        btnPanel.add(btnDeletePart);
        btnPanel.add(btnClearForm);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        // Assign Part to Job Card
        JPanel assignCard = Theme.createCardPanel();
        assignCard.setLayout(new BorderLayout(8, 8));

        JLabel lblAssignTitle = new JLabel("Assign Parts to Job (Transactional)");
        lblAssignTitle.setFont(Theme.FONT_HEADING);
        lblAssignTitle.setForeground(Theme.TEXT_DARK);
        assignCard.add(lblAssignTitle, BorderLayout.NORTH);

        JPanel assignGrid = new JPanel(new GridBagLayout());
        assignGrid.setOpaque(false);
        GridBagConstraints agbc = new GridBagConstraints();
        agbc.fill = GridBagConstraints.HORIZONTAL;
        agbc.insets = new Insets(4, 4, 4, 4);
        agbc.weightx = 1.0;

        comboJobs = new JComboBox<>();
        comboParts = new JComboBox<>();
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        comboJobs.addActionListener(e -> loadAssignedPartsForSelectedJob());

        int arow = 0;
        addFormField(assignGrid, agbc, "Select Active Job *:", comboJobs, arow++);
        addFormField(assignGrid, agbc, "Select Part *:", comboParts, arow++);
        addFormField(assignGrid, agbc, "Quantity to Assign *:", spinnerQuantity, arow++);

        assignCard.add(assignGrid, BorderLayout.CENTER);

        btnAssignPart = Theme.createPrimaryButton("+ Assign to Job");
        btnAssignPart.addActionListener(e -> handleAssignPart());
        JPanel assignBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        assignBtnPanel.setOpaque(false);
        assignBtnPanel.add(btnAssignPart);
        assignCard.add(assignBtnPanel, BorderLayout.SOUTH);

        leftContainer.add(formCard);
        leftContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        leftContainer.add(assignCard);

        // --- 2. Right Container: Inventory Table & Job Assigned Parts Table ---
        JPanel rightContainer = new JPanel(new BorderLayout(10, 10));
        rightContainer.setOpaque(false);

        // Inventory Table Card
        JPanel inventoryCard = Theme.createCardPanel();
        inventoryCard.setLayout(new BorderLayout(8, 8));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        JLabel lblSearch = new JLabel("Search Parts:");
        lblSearch.setFont(Theme.FONT_SUBTITLE);
        txtSearch = new JTextField(16);
        btnSearch = Theme.createPrimaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(85, 30));
        btnSearch.addActionListener(e -> handleSearchParts());

        btnResetSearch = Theme.createSecondaryButton("Reset");
        btnResetSearch.setPreferredSize(new Dimension(75, 30));
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            refresh();
        });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnResetSearch);

        inventoryCard.add(searchBar, BorderLayout.NORTH);

        String[] pCols = { "ID", "Part Name", "Price (Rs)", "Stock Qty", "Status" };
        partsTableModel = new DefaultTableModel(pCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        partsTable = new JTable(partsTableModel);
        Theme.styleTable(partsTable);

        // Custom Cell Renderer highlighting low stock (< 5)
        partsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String txt = (val != null) ? val.toString() : "";
                if ("LOW STOCK".equals(txt)) {
                    l.setForeground(Theme.DANGER);
                    l.setFont(Theme.FONT_BODY_BOLD);
                } else {
                    l.setForeground(Theme.SUCCESS);
                    l.setFont(Theme.FONT_BODY);
                }
                return l;
            }
        });

        // Populate form on selection
        partsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && partsTable.getSelectedRow() != -1) {
                int modelRow = partsTable.convertRowIndexToModel(partsTable.getSelectedRow());
                txtPartId.setText(partsTableModel.getValueAt(modelRow, 0).toString());
                txtPartName.setText(partsTableModel.getValueAt(modelRow, 1).toString());
                txtPartPrice.setText(partsTableModel.getValueAt(modelRow, 2).toString());
                txtPartQuantity.setText(partsTableModel.getValueAt(modelRow, 3).toString());
            }
        });

        JScrollPane scrollInventory = new JScrollPane(partsTable);
        scrollInventory.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        inventoryCard.add(scrollInventory, BorderLayout.CENTER);

        // Assigned Parts to Selected Job Card
        JPanel assignedCard = Theme.createCardPanel();
        assignedCard.setLayout(new BorderLayout(6, 6));
        assignedCard.setPreferredSize(new Dimension(500, 180));

        JLabel lblAssignedTitle = new JLabel("Parts Currently Assigned to Selected Job");
        lblAssignedTitle.setFont(Theme.FONT_HEADING);
        lblAssignedTitle.setForeground(Theme.TEXT_DARK);
        assignedCard.add(lblAssignedTitle, BorderLayout.NORTH);

        String[] asCols = { "Part Name", "Unit Price (Rs)", "Qty Used", "Line Total (Rs)" };
        assignedTableModel = new DefaultTableModel(asCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignedPartsTable = new JTable(assignedTableModel);
        Theme.styleTable(assignedPartsTable);

        JScrollPane scrollAssigned = new JScrollPane(assignedPartsTable);
        scrollAssigned.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        assignedCard.add(scrollAssigned, BorderLayout.CENTER);

        rightContainer.add(inventoryCard, BorderLayout.CENTER);
        rightContainer.add(assignedCard, BorderLayout.SOUTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, rightContainer);
        splitPane.setDividerLocation(390);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel p, GridBagConstraints gbc, String label, Component field, int row) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        field.setFont(Theme.FONT_BODY);
        p.add(field, gbc);
    }

    private void handleAddPart() {
        try {
            String name = txtPartName.getText().trim();
            double price = Double.parseDouble(txtPartPrice.getText().trim());
            int quantity = Integer.parseInt(txtPartQuantity.getText().trim());

            int newId = partService.addPart(name, price, quantity);
            if (newId > 0) {
                JOptionPane.showMessageDialog(this, "Spare part added to inventory. ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refresh();
                mainFrame.notifyDataChanged();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number and Quantity must be an integer.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdatePart() {
        if (txtPartId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a part from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtPartId.getText().trim());
            String name = txtPartName.getText().trim();
            double price = Double.parseDouble(txtPartPrice.getText().trim());
            int quantity = Integer.parseInt(txtPartQuantity.getText().trim());

            boolean ok = partService.updatePart(id, name, price, quantity);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Spare part updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
                refresh();
                mainFrame.notifyDataChanged();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for price or quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeletePart() {
        if (txtPartId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a part to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtPartId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete part " + txtPartName.getText() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = partService.deletePart(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Part deleted from inventory.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refresh();
                    mainFrame.notifyDataChanged();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAssignPart() {
        JobComboItem jobItem = (JobComboItem) comboJobs.getSelectedItem();
        PartComboItem partItem = (PartComboItem) comboParts.getSelectedItem();

        if (jobItem == null || jobItem.getJob() == null) {
            JOptionPane.showMessageDialog(this, "Please select an active job card.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (partItem == null || partItem.getPart() == null) {
            JOptionPane.showMessageDialog(this, "Please select a spare part to assign.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (Integer) spinnerQuantity.getValue();
        String jobId = jobItem.getJob().getJobId();
        int partId = partItem.getPart().getPartId();

        try {
            // OOP CONCEPT: Transaction Management (Unit 3)
            partService.assignPartToJob(jobId, partId, qty);

            JOptionPane.showMessageDialog(this,
                    "Successfully allocated " + qty + " unit(s) of '" + partItem.getPart().getPartName() + "' to Job " + jobId + "!\n"
                    + "Inventory stock updated automatically.",
                    "Part Assigned", JOptionPane.INFORMATION_MESSAGE);

            refresh();
            loadAssignedPartsForSelectedJob();
            mainFrame.notifyDataChanged();

        } catch (InvalidJobException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Assignment Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            // Catch insufficient stock or transactional error
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Stock / Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAssignedPartsForSelectedJob() {
        JobComboItem item = (JobComboItem) comboJobs.getSelectedItem();
        assignedTableModel.setRowCount(0);
        if (item == null || item.getJob() == null) {
            return;
        }

        try {
            List<Bill.LineItem> items = jobPartsDAO.getPartsForJob(item.getJob().getJobId());
            for (Bill.LineItem li : items) {
                assignedTableModel.addRow(new Object[]{
                    li.getPartName(),
                    String.format("%.2f", li.getUnitPrice()),
                    li.getQuantity(),
                    String.format("%.2f", li.getLineTotal())
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assigned parts: " + e.getMessage());
        }
    }

    private void handleSearchParts() {
        String q = txtSearch.getText().trim();
        try {
            List<Part> list = partService.searchParts(q);
            populatePartsTable(list);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtPartId.setText("");
        txtPartName.setText("");
        txtPartPrice.setText("");
        txtPartQuantity.setText("");
        partsTable.clearSelection();
    }

    public void refresh() {
        try {
            List<Part> list = partService.getAllParts();
            populatePartsTable(list);

            // Refresh parts combo
            comboParts.removeAllItems();
            for (Part p : list) {
                comboParts.addItem(new PartComboItem(p));
            }

            // Refresh active jobs combo
            comboJobs.removeAllItems();
            List<ServiceJob> jobs = jobDAO.getAllJobs();
            for (ServiceJob j : jobs) {
                if (!"Completed".equalsIgnoreCase(j.getJobStatus())) {
                    comboJobs.addItem(new JobComboItem(j));
                }
            }
            loadAssignedPartsForSelectedJob();

        } catch (SQLException e) {
            System.err.println("Error refreshing parts panel: " + e.getMessage());
        }
    }

    private void populatePartsTable(List<Part> list) {
        partsTableModel.setRowCount(0);
        for (Part p : list) {
            String status = (p.getQuantity() < 5) ? "LOW STOCK" : "In Stock";
            partsTableModel.addRow(new Object[]{
                p.getPartId(),
                p.getPartName(),
                String.format("%.2f", p.getPrice()),
                p.getQuantity(),
                status
            });
        }
    }
}
