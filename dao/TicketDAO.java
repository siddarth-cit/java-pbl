package dao;

import db.DatabaseManager;
import model.Customer;
import model.QueueStatistics;
import model.QueueTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Queue Tickets.
 * Handles queuing transactions, priority ordering, token generation, and metrics.
 */
public class TicketDAO {

    private final DatabaseManager dbManager;
    private final CustomerDAO customerDAO;

    public TicketDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Issues a new ticket for a registered customer.
     */
    public QueueTicket issueTicket(String customerId, String serviceType, boolean isPriority) {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            System.err.println("[TicketDAO] Cannot issue ticket: Customer " + customerId + " not found.");
            return null;
        }

        String token = generateNextToken(isPriority);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "INSERT INTO queue_tickets (token_number, customer_id, customer_name, customer_phone, " +
                     "service_type, is_priority, status, counter_assigned, issued_at, served_at, completed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'WAITING', '', ?, '', '')";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, token);
            pstmt.setString(2, customer.getCustomerId());
            pstmt.setString(3, customer.getName());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, (serviceType != null && !serviceType.isEmpty()) ? serviceType : customer.getServiceType());
            pstmt.setInt(6, isPriority ? 1 : 0);
            pstmt.setString(7, now);

            pstmt.executeUpdate();

            int generatedId = 0;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }

            // Also update customer's latest token number
            customerDAO.updateTokenNumber(customer.getCustomerId(), token);

            return new QueueTicket(
                    generatedId,
                    token,
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhone(),
                    serviceType,
                    isPriority,
                    QueueTicket.Status.WAITING,
                    "",
                    now,
                    "",
                    ""
            );
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error issuing ticket: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the next customer to serve according to Priority Queue policy:
     * Priority customers first (FIFO among priority), then standard customers (FIFO among standard).
     */
    public QueueTicket getNextTicketToServe() {
        String sql = "SELECT * FROM queue_tickets WHERE status = 'WAITING' ORDER BY is_priority DESC, id ASC LIMIT 1";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error getting next ticket: " + e.getMessage());
        }
        return null;
    }

    /**
     * Marks a ticket as currently SERVING at a specified counter.
     */
    public boolean callTicket(int ticketId, String counterName) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE queue_tickets SET status = 'SERVING', counter_assigned = ?, served_at = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, counterName);
            pstmt.setString(2, now);
            pstmt.setInt(3, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error calling ticket: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks a ticket as COMPLETED.
     */
    public boolean completeTicket(int ticketId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE queue_tickets SET status = 'COMPLETED', completed_at = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, now);
            pstmt.setInt(2, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error completing ticket: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancels a ticket.
     */
    public boolean cancelTicket(int ticketId) {
        String sql = "UPDATE queue_tickets SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error cancelling ticket: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns list of currently waiting tickets ordered by priority first, then ID.
     */
    public List<QueueTicket> getWaitingTickets() {
        return getTicketsByCondition("WHERE status = 'WAITING' ORDER BY is_priority DESC, id ASC LIMIT 500");
    }

    /**
     * Returns list of tickets currently being served.
     */
    public List<QueueTicket> getServingTickets() {
        return getTicketsByCondition("WHERE status = 'SERVING' ORDER BY served_at DESC LIMIT 500");
    }

    /**
     * Returns list of completed tickets.
     */
    public List<QueueTicket> getCompletedTickets() {
        return getTicketsByCondition("WHERE status = 'COMPLETED' ORDER BY completed_at DESC LIMIT 500");
    }

    /**
     * Returns all tickets.
     */
    public List<QueueTicket> getAllTickets() {
        return getTicketsByCondition("ORDER BY id DESC LIMIT 500");
    }

    /**
     * Fetch ticket by Token Number string.
     */
    public QueueTicket findByTokenNumber(String token) {
        String sql = "SELECT * FROM queue_tickets WHERE token_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error finding ticket by token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Calculates real-time system statistics directly from SQL aggregation queries.
     */
    public QueueStatistics getStatistics() {
        int totalCustomers = customerDAO.count();
        int totalTickets = 0;
        int waitingNormal = 0;
        int waitingPriority = 0;
        int serving = 0;
        int completed = 0;
        int cancelled = 0;

        String sql = "SELECT " +
                     "  COUNT(*) AS total, " +
                     "  SUM(CASE WHEN status = 'WAITING' AND is_priority = 0 THEN 1 ELSE 0 END) AS wait_norm, " +
                     "  SUM(CASE WHEN status = 'WAITING' AND is_priority = 1 THEN 1 ELSE 0 END) AS wait_prio, " +
                     "  SUM(CASE WHEN status = 'SERVING' THEN 1 ELSE 0 END) AS serving_cnt, " +
                     "  SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS comp_cnt, " +
                     "  SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS canc_cnt " +
                     "FROM queue_tickets";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                totalTickets = rs.getInt("total");
                waitingNormal = rs.getInt("wait_norm");
                waitingPriority = rs.getInt("wait_prio");
                serving = rs.getInt("serving_cnt");
                completed = rs.getInt("comp_cnt");
                cancelled = rs.getInt("canc_cnt");
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error generating statistics: " + e.getMessage());
        }

        return new QueueStatistics(
                totalCustomers,
                totalTickets,
                waitingNormal,
                waitingPriority,
                serving,
                completed,
                cancelled
        );
    }

    /**
     * Clears all queue tickets (for demonstration and reset purposes).
     */
    public void clearAll() {
        String sql = "DELETE FROM queue_tickets";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error clearing tickets: " + e.getMessage());
        }
    }

    private List<QueueTicket> getTicketsByCondition(String conditionClause) {
        List<QueueTicket> list = new ArrayList<>();
        String sql = "SELECT * FROM queue_tickets " + conditionClause;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error fetching tickets with condition: " + e.getMessage());
        }
        return list;
    }

    private QueueTicket mapResultSetToTicket(ResultSet rs) throws SQLException {
        QueueTicket.Status status = QueueTicket.Status.WAITING;
        try {
            status = QueueTicket.Status.valueOf(rs.getString("status"));
        } catch (Exception ignored) {}

        return new QueueTicket(
                rs.getInt("id"),
                rs.getString("token_number"),
                rs.getString("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_phone"),
                rs.getString("service_type"),
                rs.getInt("is_priority") == 1,
                status,
                rs.getString("counter_assigned"),
                rs.getString("issued_at"),
                rs.getString("served_at"),
                rs.getString("completed_at")
        );
    }

    /**
     * Determines next token identifier (e.g., T101, T102... or P101, P102...).
     */
    private String generateNextToken(boolean isPriority) {
        String prefix = isPriority ? "P" : "T";
        String sql = "SELECT token_number FROM queue_tickets WHERE token_number LIKE ? ORDER BY id DESC LIMIT 1";
        int nextNum = 101;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastToken = rs.getString("token_number");
                    try {
                        int num = Integer.parseInt(lastToken.replaceAll("[^0-9]", ""));
                        nextNum = num + 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error getting token sequence: " + e.getMessage());
        }

        return prefix + nextNum;
    }
}
