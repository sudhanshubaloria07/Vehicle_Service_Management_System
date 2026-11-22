package service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import service.core.Part;

/**
 * Data Access Object (DAO) for managing spare parts inventory.
 * 
 * OOP CONCEPT: Encapsulation (Unit 1)
 * OOP CONCEPT: Exception Handling (Unit 3) - Foreign key RESTRICT checking
 */
public class PartDAO {

    /**
     * Retrieves all parts in inventory.
     */
    public List<Part> getAllParts() throws SQLException {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT part_id, part_name, price, quantity FROM Parts ORDER BY part_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Part p = new Part(
                    rs.getInt("part_id"),
                    rs.getString("part_name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                );
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Finds a part by primary key ID.
     */
    public Part getPartById(int partId) throws SQLException {
        String sql = "SELECT part_id, part_name, price, quantity FROM Parts WHERE part_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, partId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Part(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new spare part into the inventory.
     */
    public int addPart(Part part) throws SQLException {
        String sql = "INSERT INTO Parts (part_name, price, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, part.getPartName());
            ps.setDouble(2, part.getPrice());
            ps.setInt(3, part.getQuantity());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    part.setPartId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing spare part.
     */
    public boolean updatePart(Part part) throws SQLException {
        String sql = "UPDATE Parts SET part_name = ?, price = ?, quantity = ? WHERE part_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, part.getPartName());
            ps.setDouble(2, part.getPrice());
            ps.setInt(3, part.getQuantity());
            ps.setInt(4, part.getPartId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a part by ID.
     * Protects foreign key integrity if part has been assigned to jobs.
     */
    public boolean deletePart(int partId) throws SQLException {
        String sql = "DELETE FROM Parts WHERE part_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, partId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451 || e.getMessage().toLowerCase().contains("foreign key")) {
                throw new SQLException("Cannot delete part: It is currently recorded in one or more job cards.");
            }
            throw e;
        }
    }

    /**
     * Returns parts whose stock is below the critical threshold (e.g., < 5).
     */
    public List<Part> getLowStockParts(int threshold) throws SQLException {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT part_id, part_name, price, quantity FROM Parts WHERE quantity < ? ORDER BY quantity ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Part(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Searches parts by name.
     */
    public List<Part> searchParts(String query) throws SQLException {
        List<Part> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllParts();
        }

        String sql = "SELECT part_id, part_name, price, quantity FROM Parts WHERE part_name LIKE ? ORDER BY part_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + query.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Part(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                    ));
                }
            }
        }
        return list;
    }
}
