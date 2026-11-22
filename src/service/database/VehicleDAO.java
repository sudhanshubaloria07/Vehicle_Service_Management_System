package service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import service.core.Customer;
import service.vehicle.Bike;
import service.vehicle.Car;
import service.vehicle.Vehicle;

/**
 * Data Access Object (DAO) for Vehicle and Customer operations.
 * 
 * OOP CONCEPT: Encapsulation & Separation of Concerns (Unit 1 & 2)
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Instantiates Car or Bike polymorphically via factory method
 * OOP CONCEPT: Exception Handling (Unit 3) - PreparedStatement and try-catch-finally
 */
public class VehicleDAO {

    /**
     * Polymorphic Factory Method: creates a Car or Bike instance dynamically
     * based on the vehicle_type column value read from the database.
     * 
     * OOP CONCEPT: Runtime Polymorphism (Unit 2)
     */
    public static Vehicle createVehicleInstance(int id, Customer customer, String regNo, String brand, String model, String type) {
        if ("Car".equalsIgnoreCase(type)) {
            // OOP CONCEPT: Polymorphism - assigning Car to Vehicle reference
            return new Car(id, customer, regNo, brand, model);
        } else {
            // OOP CONCEPT: Polymorphism - assigning Bike to Vehicle reference
            return new Bike(id, customer, regNo, brand, model);
        }
    }

    /**
     * Retrieves all registered vehicles sorted by ID descending.
     */
    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, customer_name, phone, registration_number, brand, model, vehicle_type "
                   + "FROM Vehicle ORDER BY vehicle_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer cust = new Customer(rs.getString("customer_name"), rs.getString("phone"));
                Vehicle v = createVehicleInstance(
                    rs.getInt("vehicle_id"),
                    cust,
                    rs.getString("registration_number"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("vehicle_type")
                );
                list.add(v);
            }
        }
        return list;
    }

    /**
     * Finds a vehicle by primary key ID.
     */
    public Vehicle getVehicleById(int vehicleId) throws SQLException {
        String sql = "SELECT vehicle_id, customer_name, phone, registration_number, brand, model, vehicle_type "
                   + "FROM Vehicle WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer cust = new Customer(rs.getString("customer_name"), rs.getString("phone"));
                    return createVehicleInstance(
                        rs.getInt("vehicle_id"),
                        cust,
                        rs.getString("registration_number"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("vehicle_type")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Checks if a registration number is already registered to another vehicle.
     */
    public boolean isRegistrationDuplicate(String regNo, int excludeVehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicle WHERE UPPER(registration_number) = UPPER(?) AND vehicle_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            ps.setInt(2, excludeVehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a new Vehicle and associated Customer into the database.
     * 
     * @param v Polymorphic Vehicle object
     * @return Generated auto-increment vehicle_id
     */
    public int addVehicle(Vehicle v) throws SQLException {
        if (isRegistrationDuplicate(v.getRegistrationNumber(), 0)) {
            throw new SQLException("Registration number '" + v.getRegistrationNumber() + "' is already registered.");
        }

        String sql = "INSERT INTO Vehicle (customer_name, phone, registration_number, brand, model, vehicle_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getCustomer().getName());
            ps.setString(2, v.getCustomer().getPhone());
            ps.setString(3, v.getRegistrationNumber().toUpperCase().trim());
            ps.setString(4, v.getBrand());
            ps.setString(5, v.getModel());
            ps.setString(6, v.getVehicleType());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    v.setVehicleId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing vehicle and customer details.
     */
    public boolean updateVehicle(Vehicle v) throws SQLException {
        if (isRegistrationDuplicate(v.getRegistrationNumber(), v.getVehicleId())) {
            throw new SQLException("Cannot update: Registration number '" + v.getRegistrationNumber() + "' is already in use by another vehicle.");
        }

        String sql = "UPDATE Vehicle SET customer_name = ?, phone = ?, registration_number = ?, "
                   + "brand = ?, model = ?, vehicle_type = ? WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getCustomer().getName());
            ps.setString(2, v.getCustomer().getPhone());
            ps.setString(3, v.getRegistrationNumber().toUpperCase().trim());
            ps.setString(4, v.getBrand());
            ps.setString(5, v.getModel());
            ps.setString(6, v.getVehicleType());
            ps.setInt(7, v.getVehicleId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a vehicle by ID.
     * Catches foreign key constraint violation if associated jobs exist.
     */
    public boolean deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM Vehicle WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // MySQL error code 1451: Cannot delete or update a parent row: a foreign key constraint fails
            if (e.getErrorCode() == 1451 || e.getMessage().toLowerCase().contains("foreign key")) {
                throw new SQLException("Cannot delete vehicle: It has service jobs linked to it. Please remove or complete those jobs first.");
            }
            throw e;
        }
    }

    /**
     * Searches vehicles across customer name, phone, registration number, brand, or model.
     */
    public List<Vehicle> searchVehicles(String query) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllVehicles();
        }

        String searchPattern = "%" + query.trim() + "%";
        String sql = "SELECT vehicle_id, customer_name, phone, registration_number, brand, model, vehicle_type "
                   + "FROM Vehicle "
                   + "WHERE customer_name LIKE ? OR phone LIKE ? OR registration_number LIKE ? "
                   + "   OR brand LIKE ? OR model LIKE ? "
                   + "ORDER BY vehicle_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer cust = new Customer(rs.getString("customer_name"), rs.getString("phone"));
                    Vehicle v = createVehicleInstance(
                        rs.getInt("vehicle_id"),
                        cust,
                        rs.getString("registration_number"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("vehicle_type")
                    );
                    list.add(v);
                }
            }
        }
        return list;
    }
}
