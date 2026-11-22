package service.core;

import java.sql.SQLException;
import java.util.List;
import service.database.JobPartsDAO;
import service.database.PartDAO;
import service.exception.InvalidJobException;

/**
 * Service orchestrator for spare parts inventory and allocation.
 * 
 * OOP CONCEPT: Separation of Concerns (Unit 1 & 2)
 * OOP CONCEPT: Exception Handling (Unit 3)
 */
public class PartService {

    private final PartDAO partDAO;
    private final JobPartsDAO jobPartsDAO;

    public PartService() {
        this.partDAO = new PartDAO();
        this.jobPartsDAO = new JobPartsDAO();
    }

    public List<Part> getAllParts() throws SQLException {
        return partDAO.getAllParts();
    }

    public Part getPartById(int partId) throws SQLException {
        return partDAO.getPartById(partId);
    }

    public int addPart(String name, double price, int quantity) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Part name cannot be empty.");
        }
        Validator.validatePartPrice(price);
        Validator.validatePartQuantity(quantity);

        Part p = new Part(name, price, quantity);
        return partDAO.addPart(p);
    }

    public boolean updatePart(int partId, String name, double price, int quantity) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Part name cannot be empty.");
        }
        Validator.validatePartPrice(price);
        Validator.validatePartQuantity(quantity);

        Part p = new Part(partId, name, price, quantity);
        return partDAO.updatePart(p);
    }

    public boolean deletePart(int partId) throws SQLException {
        return partDAO.deletePart(partId);
    }

    public List<Part> getLowStockParts(int threshold) throws SQLException {
        return partDAO.getLowStockParts(threshold);
    }

    public List<Part> searchParts(String query) throws SQLException {
        return partDAO.searchParts(query);
    }

    /**
     * Allocates a spare part to a job with atomic stock deduction.
     */
    public void assignPartToJob(String jobId, int partId, int quantity) throws InvalidJobException, SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        jobPartsDAO.assignPartToJob(jobId, partId, quantity);
    }
}
