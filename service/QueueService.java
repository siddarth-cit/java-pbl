package service;

import dao.CustomerDAO;
import dao.TicketDAO;
import model.Customer;
import model.QueueStatistics;
import model.QueueTicket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * QueueService serves as the core business logic hub.
 * Integrates Java in-memory Data Structures (PriorityQueue, FIFO Queue, ArrayList)
 * with robust SQL/JDBC persistence through DAOs.
 */
public class QueueService {

    private static QueueService instance;

    private final CustomerDAO customerDAO;
    private final TicketDAO ticketDAO;

    // In-memory collections matching original OOP requirements
    private final ArrayList<Customer> customerCache;
    private final Queue<Customer> normalQueue;
    private final PriorityQueue<Customer> priorityQueue;

    private QueueTicket currentlyServingTicket;

    private QueueService() {
        this.customerDAO = new CustomerDAO();
        this.ticketDAO = new TicketDAO();
        this.customerCache = new ArrayList<>();
        this.normalQueue = new LinkedList<>();
        this.priorityQueue = new PriorityQueue<>();

        refreshMemoryFromDatabase();
    }

    public static synchronized QueueService getInstance() {
        if (instance == null) {
            instance = new QueueService();
        }
        return instance;
    }

    /**
     * Synchronizes in-memory collections with the database.
     */
    public synchronized void refreshMemoryFromDatabase() {
        customerCache.clear();
        normalQueue.clear();
        priorityQueue.clear();

        // 1. Load all customers
        List<Customer> dbCustomers = customerDAO.findAll();
        customerCache.addAll(dbCustomers);

        // Adjust static customer counter if needed
        int nextSeq = customerDAO.getNextCustomerSequence();
        Customer.setCustomerCounter(nextSeq);

        // 2. Load waiting tickets and populate in-memory queues
        List<QueueTicket> waitingTickets = ticketDAO.getWaitingTickets();
        for (QueueTicket ticket : waitingTickets) {
            Customer cust = customerDAO.findById(ticket.getCustomerId());
            if (cust != null) {
                cust.setTokenNumber(ticket.getTokenNumber());
                if (ticket.isPriority()) {
                    priorityQueue.offer(cust);
                } else {
                    normalQueue.offer(cust);
                }
            }
        }

        // 3. Check currently serving ticket
        List<QueueTicket> serving = ticketDAO.getServingTickets();
        if (!serving.isEmpty()) {
            currentlyServingTicket = serving.get(0);
        } else {
            currentlyServingTicket = null;
        }
    }

    /**
     * Registers a new customer into the database and in-memory cache.
     */
    public synchronized Customer registerCustomer(String name, int age, String phone, String serviceType, boolean isPriority) {
        // Enforce sequence from DB
        int nextIdNum = customerDAO.getNextCustomerSequence();
        Customer.setCustomerCounter(nextIdNum);

        Customer customer = new Customer(name, age, phone, serviceType, isPriority);
        boolean saved = customerDAO.save(customer);
        if (saved) {
            customerCache.add(0, customer);
        }
        return customer;
    }

    /**
     * Issues a token for an existing customer.
     */
    public synchronized QueueTicket issueToken(String customerId) {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            return null;
        }

        QueueTicket ticket = ticketDAO.issueTicket(
                customer.getCustomerId(),
                customer.getServiceType(),
                customer.isPriorityCustomer()
        );

        if (ticket != null) {
            customer.setTokenNumber(ticket.getTokenNumber());

            // Add to in-memory queues
            if (customer.isPriorityCustomer()) {
                priorityQueue.offer(customer);
            } else {
                normalQueue.offer(customer);
            }
        }

        return ticket;
    }

    /**
     * Calls the next customer from the priority queue or normal queue to a counter.
     */
    public synchronized QueueTicket callNextCustomer(String counterName) {
        // Check database next waiting ticket
        QueueTicket nextTicket = ticketDAO.getNextTicketToServe();
        if (nextTicket == null) {
            currentlyServingTicket = null;
            return null;
        }

        // Advance database record
        ticketDAO.callTicket(nextTicket.getId(), counterName);
        nextTicket.setStatus(QueueTicket.Status.SERVING);
        nextTicket.setCounterAssigned(counterName);
        currentlyServingTicket = nextTicket;

        // Synchronize in-memory queues
        if (!priorityQueue.isEmpty()) {
            priorityQueue.poll();
        } else if (!normalQueue.isEmpty()) {
            normalQueue.poll();
        }

        return nextTicket;
    }

    /**
     * Marks a ticket as completed.
     */
    public synchronized boolean completeTicket(int ticketId) {
        boolean ok = ticketDAO.completeTicket(ticketId);
        if (ok && currentlyServingTicket != null && currentlyServingTicket.getId() == ticketId) {
            currentlyServingTicket = null;
        }
        return ok;
    }

    /**
     * Cancels a ticket.
     */
    public synchronized boolean cancelTicket(int ticketId) {
        boolean ok = ticketDAO.cancelTicket(ticketId);
        refreshMemoryFromDatabase();
        return ok;
    }

    /**
     * Search customer by query (ID, Name, Phone).
     */
    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return customerDAO.findAll();
        }
        return customerDAO.search(query.trim());
    }

    public Customer getCustomerById(String customerId) {
        return customerDAO.findById(customerId);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<QueueTicket> getWaitingTickets() {
        return ticketDAO.getWaitingTickets();
    }

    public List<QueueTicket> getServingTickets() {
        return ticketDAO.getServingTickets();
    }

    public List<QueueTicket> getCompletedTickets() {
        return ticketDAO.getCompletedTickets();
    }

    public List<QueueTicket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    public QueueStatistics getStatistics() {
        return ticketDAO.getStatistics();
    }

    public QueueTicket getCurrentlyServingTicket() {
        return currentlyServingTicket;
    }

    public List<Customer> getNormalQueueList() {
        return new ArrayList<>(normalQueue);
    }

    public List<Customer> getPriorityQueueList() {
        return new ArrayList<>(priorityQueue);
    }

    /**
     * Seeds realistic demo data (customers and queue tickets) for quick evaluation.
     */
    public synchronized void seedSampleData() {
        ticketDAO.clearAll();
        currentlyServingTicket = null;

        // Sample Indian Customers
        Customer c1 = registerCustomer("Venkatesh Kumar", 68, "+91-98401-11223", "Retirement & Pension Desk", true);
        Customer c2 = registerCustomer("Bala Subramanian", 34, "+91-98402-22334", "Cash Deposit & Withdrawal", false);
        Customer c3 = registerCustomer("Hari Prasad", 29, "+91-98403-33445", "Loan & Mortgage Consultation", false);
        Customer c4 = registerCustomer("Jaikumar Raman", 55, "+91-98404-44556", "Wealth Management (VIP)", true);
        Customer c5 = registerCustomer("Vijayakumar Natarajan", 42, "+91-98405-55667", "Credit Card & Account Opening", false);
        Customer c6 = registerCustomer("Subash Chandran", 38, "+91-98406-66778", "Support & Dispute Resolution", false);
        Customer c7 = registerCustomer("Sathish Kumar", 31, "+91-98407-77889", "General Banking Inquiry", false);

        // Issue tokens
        issueToken(c1.getCustomerId());
        issueToken(c2.getCustomerId());
        issueToken(c3.getCustomerId());
        issueToken(c4.getCustomerId());
        issueToken(c5.getCustomerId());
        issueToken(c6.getCustomerId());
        issueToken(c7.getCustomerId());

        // Call the first VIP customer
        callNextCustomer("Counter 4 - Priority & VIP Desk");

        refreshMemoryFromDatabase();
    }

    /**
     * Seeds realistic Tirumala Tirupati Devasthanams (TTD) pilgrimage demo data.
     */
    public synchronized void seedTirupatiDemoData() {
        // Reset old tickets first for a clean showcase
        ticketDAO.clearAll();
        currentlyServingTicket = null;

        // Sample Tirupati Pilgrims with Indian names across devotee categories
        Customer p1 = registerCustomer("Venkatesh Prasad (Senior Citizen)", 74, "+91-98480-12345", "Divyangjan & Senior Citizens Darshan", true);
        Customer p2 = registerCustomer("Bala Murugan & Family", 35, "+91-94401-23456", "Special Entry Darshan (SED ₹300)", false);
        Customer p3 = registerCustomer("Jaikumar (Protocol Officer)", 58, "+91-98200-34567", "VIP Break Darshan (Protocol L1/L2)", true);
        Customer p4 = registerCustomer("Subash & Group (4 Pilgrims)", 41, "+91-98850-45678", "Sarva Darshan (Free Slotted SSD)", false);
        Customer p5 = registerCustomer("Hariharan S (Footpath Pilgrim)", 27, "+91-94440-56789", "Divya Darshan (Alipiri Footpath)", false);
        Customer p6 = registerCustomer("Vijayakumar & Padmavathi (Infant)", 30, "+91-99880-67890", "Infant (<1 yr) & Parents Priority", true);
        Customer p7 = registerCustomer("Sathish Rajan", 33, "+91-97000-11223", "Arjitha Seva (Kalyanotsavam Pass)", false);

        // Issue tokens for pilgrims
        issueToken(p1.getCustomerId());
        issueToken(p2.getCustomerId());
        issueToken(p3.getCustomerId());
        issueToken(p4.getCustomerId());
        issueToken(p5.getCustomerId());
        issueToken(p6.getCustomerId());
        issueToken(p7.getCustomerId());

        // Call the senior citizen / VIP pilgrim to Supadham gate
        callNextCustomer("Supadham Entry (Senior Citizen & Infant)");

        refreshMemoryFromDatabase();
    }

    /**
     * High-speed bulk insertion of 100,000 (1 Lakh) Tirupati pilgrims using JDBC batching.
     */
    public synchronized void seedOneLakhDevotees() {
        ticketDAO.clearAll();
        customerDAO.clearAll();
        currentlyServingTicket = null;

        String[] firstNames = {
            "Venkatesh", "Bala", "Hari", "Subash", "Jaikumar", "Vijayakumar", "Sathish",
            "Srinivasan", "Govindan", "Murugan", "Ramanathan", "Sundaram", "Karthik",
            "Narayanan", "Padmavathi", "Meenakshi", "Lakshmi", "Kavitha", "Geetha", "Radha"
        };
        String[] lastNames = {
            "Prasad", "Kumar", "Murugan", "Chandran", "Natarajan", "Raman", "Sharma",
            "Rao", "Reddy", "Iyer", "Iyengar", "Goud", "Naidu", "Pillai", "Chettiar"
        };
        String[] services = {
            "Sarva Darshan (Free Slotted SSD)",
            "Special Entry Darshan (SED ₹300)",
            "Divyangjan & Senior Citizens Darshan",
            "VIP Break Darshan (Protocol L1/L2)",
            "Infant (<1 yr) & Parents Priority",
            "Divya Darshan (Alipiri Footpath)",
            "Arjitha Seva (Kalyanotsavam Pass)"
        };

        db.DatabaseManager dbManager = db.DatabaseManager.getInstance();
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String custSql = "INSERT INTO customers (customer_id, name, age, phone, service_type, is_priority, token_number, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String ticketSql = "INSERT INTO queue_tickets (token_number, customer_id, customer_name, customer_phone, service_type, is_priority, status, counter_assigned, issued_at, served_at, completed_at) VALUES (?, ?, ?, ?, ?, ?, 'WAITING', '', ?, '', '')";

        try (java.sql.Connection conn = dbManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (java.sql.Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA synchronous = OFF;");
                pragmaStmt.execute("PRAGMA journal_mode = MEMORY;");
            } catch (Exception ignored) {}

            try (java.sql.PreparedStatement custPstmt = conn.prepareStatement(custSql);
                 java.sql.PreparedStatement ticketPstmt = conn.prepareStatement(ticketSql)) {

                int totalCount = 100000;
                for (int i = 1; i <= totalCount; i++) {
                    String custId = "C" + (100000 + i);
                    String name = firstNames[i % firstNames.length] + " " + lastNames[i % lastNames.length];
                    int age = 18 + (i % 68);
                    String phone = String.format("+91-%05d-%05d", 90000 + (i % 9999), 10000 + (i % 89999));

                    // ~15% priority pilgrims (Senior citizens, Divyangjan, VIP protocol, Infant)
                    boolean isPriority = (i % 7 == 0);
                    String service = isPriority
                            ? ((i % 14 == 0) ? "VIP Break Darshan (Protocol L1/L2)" : "Divyangjan & Senior Citizens Darshan")
                            : services[i % services.length];

                    String token = (isPriority ? "VIP-" : "SSD-") + (100000 + i);

                    // 1. Customer record
                    custPstmt.setString(1, custId);
                    custPstmt.setString(2, name);
                    custPstmt.setInt(3, age);
                    custPstmt.setString(4, phone);
                    custPstmt.setString(5, service);
                    custPstmt.setInt(6, isPriority ? 1 : 0);
                    custPstmt.setString(7, token);
                    custPstmt.setString(8, now);
                    custPstmt.addBatch();

                    // 2. Queue ticket record
                    ticketPstmt.setString(1, token);
                    ticketPstmt.setString(2, custId);
                    ticketPstmt.setString(3, name);
                    ticketPstmt.setString(4, phone);
                    ticketPstmt.setString(5, service);
                    ticketPstmt.setInt(6, isPriority ? 1 : 0);
                    ticketPstmt.setString(7, now);
                    ticketPstmt.addBatch();

                    if (i % 10000 == 0) {
                        custPstmt.executeBatch();
                        ticketPstmt.executeBatch();
                    }
                }

                custPstmt.executeBatch();
                ticketPstmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

            try (java.sql.Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA synchronous = NORMAL;");
                pragmaStmt.execute("PRAGMA journal_mode = WAL;");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("[QueueService] Error batch inserting 100k devotees: " + e.getMessage());
        }

        // Call the first VIP devotee into Supadham gate
        callNextCustomer("Supadham Entry (Senior Citizen & Infant)");

        refreshMemoryFromDatabase();
    }

    /**
     * Resets all tickets.
     */
    public synchronized void resetQueue() {
        ticketDAO.clearAll();
        currentlyServingTicket = null;
        refreshMemoryFromDatabase();
    }
}
