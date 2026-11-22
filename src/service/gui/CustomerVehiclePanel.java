package service.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import service.core.Customer;
import service.core.Validator;
import service.database.VehicleDAO;
import service.vehicle.Vehicle;

/**
 * Customer & Vehicle Registration screen for managing automobile inventory.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - CustomerVehiclePanel extends JPanel
 * OOP CONCEPT: Polymorphism (Unit 2) - Treats Car and Bike polymorphically via Vehicle base class
 * OOP CONCEPT: Exception Handling (Unit 3) - User friendly error feedback and validation
 */
public class CustomerVehiclePanel extends JPanel {

    private final MainFrame mainFrame;
    private final VehicleDAO vehicleDAO;

    // Form fields
    private JTextField txtId;
    private JTextField txtCustomerName;
    private JTextField txtPhone;
    private JTextField txtRegistration;
    private JTextField txtBrand;
    private JTextField txtModel;
    private JComboBox<String> comboType;

    // Action buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Search bar
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;

    // Table
    private JTable vehicleTable;
    private DefaultTableModel tableModel;

    public CustomerVehiclePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.vehicleDAO = new VehicleDAO();

        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initComponents();
        refresh();
    }

    private void initComponents() {
        // --- 1. Left Form Panel ---
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 600));

        JLabel lblFormTitle = new JLabel("Vehicle & Customer Details");
        lblFormTitle.setFont(Theme.FONT_HEADING);
        lblFormTitle.setForeground(Theme.TEXT_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(240, 240, 240));

        txtCustomerName = new JTextField();
        txtPhone = new JTextField();
        txtRegistration = new JTextField();
        txtBrand = new JTextField();
        txtModel = new JTextField();
        comboType = new JComboBox<>(new String[]{ "Car", "Bike" });

        int row = 0;
        addFormField(fieldsPanel, gbc, "Vehicle ID (Auto):", txtId, row++);
        addFormField(fieldsPanel, gbc, "Customer Name *:", txtCustomerName, row++);
        addFormField(fieldsPanel, gbc, "Mobile Phone (10 digits) *:", txtPhone, row++);
        addFormField(fieldsPanel, gbc, "Registration No (e.g. DL01AB1234) *:", txtRegistration, row++);
        addFormField(fieldsPanel, gbc, "Manufacturer Brand *:", txtBrand, row++);
        addFormField(fieldsPanel, gbc, "Model Name *:", txtModel, row++);
        addFormField(fieldsPanel, gbc, "Vehicle Category *:", comboType, row++);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons row
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsPanel.setOpaque(false);

        btnAdd = Theme.createPrimaryButton("Add Vehicle");
        btnUpdate = Theme.createSuccessButton("Update");
        btnDelete = Theme.createDangerButton("Delete");
        btnClear = Theme.createSecondaryButton("Clear Form");

        btnAdd.addActionListener(e -> handleAddVehicle());
        btnUpdate.addActionListener(e -> handleUpdateVehicle());
        btnDelete.addActionListener(e -> handleDeleteVehicle());
        btnClear.addActionListener(e -> clearForm());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        formCard.add(buttonsPanel, BorderLayout.SOUTH);

        // --- 2. Right Table Panel ---
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        // Search Bar at Top of Table
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Vehicle:");
        lblSearch.setFont(Theme.FONT_SUBTITLE);
        lblSearch.setForeground(Theme.TEXT_DARK);

        txtSearch = new JTextField(20);
        btnSearch = Theme.createPrimaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(90, 32));
        btnSearch.addActionListener(e -> handleSearch());

        btnResetSearch = Theme.createSecondaryButton("Reset");
        btnResetSearch.setPreferredSize(new Dimension(80, 32));
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            refresh();
        });

        searchBar.add(lblSearch);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnResetSearch);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "ID", "Customer Name", "Phone", "Reg Number", "Brand", "Model", "Type", "Multiplier" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        vehicleTable = new JTable(tableModel);
        Theme.styleTable(vehicleTable);

        // Row selection populates form
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vehicleTable.getSelectedRow() != -1) {
                int selectedRow = vehicleTable.getSelectedRow();
                int modelRow = vehicleTable.convertRowIndexToModel(selectedRow);
                txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
                txtCustomerName.setText(tableModel.getValueAt(modelRow, 1).toString());
                txtPhone.setText(tableModel.getValueAt(modelRow, 2).toString());
                txtRegistration.setText(tableModel.getValueAt(modelRow, 3).toString());
                txtBrand.setText(tableModel.getValueAt(modelRow, 4).toString());
                txtModel.setText(tableModel.getValueAt(modelRow, 5).toString());
                comboType.setSelectedItem(tableModel.getValueAt(modelRow, 6).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(370);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, java.awt.Component field, int row) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        field.setFont(Theme.FONT_BODY);
        panel.add(field, gbc);
    }

    private void handleAddVehicle() {
        try {
            Validator.validateCustomerName(txtCustomerName.getText());
            Validator.validatePhone(txtPhone.getText());
            String reg = Validator.normalizeAndValidateRegNo(txtRegistration.getText());
            Validator.validateBrandAndModel(txtBrand.getText(), txtModel.getText());

            Customer cust = new Customer(txtCustomerName.getText(), txtPhone.getText());
            String type = comboType.getSelectedItem().toString();

            // OOP CONCEPT: Polymorphic instantiation via Factory method
            Vehicle vehicle = VehicleDAO.createVehicleInstance(0, cust, reg, txtBrand.getText(), txtModel.getText(), type);

            int newId = vehicleDAO.addVehicle(vehicle);
            if (newId > 0) {
                JOptionPane.showMessageDialog(this, "Vehicle registered successfully! ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refresh();
                mainFrame.notifyDataChanged();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateVehicle() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            Validator.validateCustomerName(txtCustomerName.getText());
            Validator.validatePhone(txtPhone.getText());
            String reg = Validator.normalizeAndValidateRegNo(txtRegistration.getText());
            Validator.validateBrandAndModel(txtBrand.getText(), txtModel.getText());

            Customer cust = new Customer(txtCustomerName.getText(), txtPhone.getText());
            String type = comboType.getSelectedItem().toString();

            Vehicle vehicle = VehicleDAO.createVehicleInstance(id, cust, reg, txtBrand.getText(), txtModel.getText(), type);

            boolean ok = vehicleDAO.updateVehicle(vehicle);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Vehicle updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
                mainFrame.notifyDataChanged();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteVehicle() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete vehicle " + txtRegistration.getText() + " (ID: " + id + ")?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = vehicleDAO.deleteVehicle(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Vehicle deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    refresh();
                    mainFrame.notifyDataChanged();
                }
            } catch (SQLException ex) {
                // Friendly foreign key message handled in DAO
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        try {
            List<Vehicle> list = vehicleDAO.searchVehicles(query);
            populateTable(list);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtCustomerName.setText("");
        txtPhone.setText("");
        txtRegistration.setText("");
        txtBrand.setText("");
        txtModel.setText("");
        comboType.setSelectedIndex(0);
        vehicleTable.clearSelection();
    }

    public void refresh() {
        try {
            List<Vehicle> list = vehicleDAO.getAllVehicles();
            populateTable(list);
        } catch (SQLException e) {
            System.err.println("Failed to fetch vehicles: " + e.getMessage());
        }
    }

    private void populateTable(List<Vehicle> list) {
        tableModel.setRowCount(0);
        for (Vehicle v : list) {
            tableModel.addRow(new Object[]{
                v.getVehicleId(),
                v.getCustomer().getName(),
                v.getCustomer().getPhone(),
                v.getRegistrationNumber(),
                v.getBrand(),
                v.getModel(),
                v.getVehicleType(),
                // OOP CONCEPT: Polymorphic call to getLabourMultiplier()
                v.getLabourMultiplier() + "x"
            });
        }
    }
}
