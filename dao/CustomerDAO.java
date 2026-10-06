package dao;

import db.DatabaseManager;
import model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Customer entity.
 * Uses PreparedStatements for all SQL queries to prevent SQL Injection.
 */
public class CustomerDAO {

    private final DatabaseManager dbManager;

    public CustomerDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Inserts a new customer into the database.
     */
    public boolean save(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, name, age, phone, service_type, is_priority, token_number, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getCustomerId());
            pstmt.setString(2, customer.getName());
            pstmt.setInt(3, customer.getAge());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getServiceType());
            pstmt.setInt(6, customer.isPriorityCustomer() ? 1 : 0);
            pstmt.setString(7, customer.getTokenNumber());
            pstmt.setString(8, customer.getCreatedAt());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error saving customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates customer record.
     */
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, age = ?, phone = ?, service_type = ?, is_priority = ?, token_number = ? " +
                     "WHERE customer_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setInt(2, customer.getAge());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getServiceType());
            pstmt.setInt(5, customer.isPriorityCustomer() ? 1 : 0);
            pstmt.setString(6, customer.getTokenNumber());
            pstmt.setString(7, customer.getCustomerId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error updating customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates customer's token number upon token generation.
     */
    public boolean updateTokenNumber(String customerId, String tokenNumber) {
        String sql = "UPDATE customers SET token_number = ? WHERE customer_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tokenNumber);
            pstmt.setString(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error updating token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes customer by ID.
     */
    public boolean delete(String customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves customer by ID.
     */
    public Customer findById(String customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error finding customer by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves customer by phone number.
     */
    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error finding customer by phone: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all registered customers ordered by creation time descending.
     */
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY created_at DESC LIMIT 500";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error fetching all customers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Search customers across ID, Name, or Phone number.
     */
    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(customer_id) LIKE ? OR LOWER(name) LIKE ? OR phone LIKE ? ORDER BY created_at DESC LIMIT 200";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword.toLowerCase().trim() + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error searching customers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates the next customer ID sequence number based on existing database entries.
     */
    public int getNextCustomerSequence() {
        String sql = "SELECT MAX(CAST(SUBSTR(customer_id, 2) AS INTEGER)) FROM customers";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int max = rs.getInt(1);
                return (max >= 1000) ? (max + 1) : 1001;
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error calculating next sequence: " + e.getMessage());
        }
        return 1001;
    }

    /**
     * Total count of customers in database.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error counting customers: " + e.getMessage());
        }
        return 0;
    }

    public void clearAll() {
        String sql = "DELETE FROM customers";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error clearing customers: " + e.getMessage());
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("phone"),
                rs.getString("service_type"),
                rs.getInt("is_priority") == 1,
                rs.getString("token_number"),
                rs.getString("created_at")
        );
    }
}
