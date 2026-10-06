import dao.CustomerDAO;
import dao.TicketDAO;
import db.DatabaseManager;
import model.QueueStatistics;
import model.QueueTicket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueManager {

    // ArrayList to store all customers
    private ArrayList<Customer> customers;

    // Normal Queue
    private Queue<Customer> normalQueue;

    // Priority Queue
    private PriorityQueue<Customer> priorityQueue;

    // Static Token Counter
    private static int tokenCounter = 101;

    // JDBC DAOs
    private final CustomerDAO customerDAO;
    private final TicketDAO ticketDAO;

    // Constructor
    public QueueManager() {

        customers = new ArrayList<>();
        normalQueue = new LinkedList<>();
        priorityQueue = new PriorityQueue<>();

        // Initialize JDBC backend
        DatabaseManager.getInstance();
        this.customerDAO = new CustomerDAO();
        this.ticketDAO = new TicketDAO();

        // Load existing state from Database
        syncFromDatabase();
    }

    /**
     * Synchronizes memory queues with the SQL database on start.
     */
    public void syncFromDatabase() {
        customers.clear();
        normalQueue.clear();
        priorityQueue.clear();

        List<model.Customer> dbCusts = customerDAO.findAll();
        for (model.Customer mc : dbCusts) {
            Customer c = new Customer(
                    mc.getCustomerId(),
                    mc.getName(),
                    mc.getAge(),
                    mc.getPhone(),
                    mc.getServiceType(),
                    mc.isPriorityCustomer(),
                    mc.getTokenNumber(),
                    mc.getCreatedAt()
            );
            customers.add(c);
        }

        // Load waiting tickets
        List<QueueTicket> waiting = ticketDAO.getWaitingTickets();
        for (QueueTicket t : waiting) {
            Customer cust = findCustomerInMemory(t.getCustomerId());
            if (cust != null) {
                cust.setTokenNumber(t.getTokenNumber());
                if (t.isPriority()) {
                    priorityQueue.offer(cust);
                } else {
                    normalQueue.offer(cust);
                }
            }
        }
    }

    // Register Customer

    public void registerCustomer(Customer customer) {

        customers.add(customer);

        // Persist to SQL Database via JDBC
        model.Customer modelCust = new model.Customer(
                customer.getCustomerId(),
                customer.getName(),
                customer.getAge(),
                customer.getPhone(),
                customer.getServiceType(),
                customer.isPriorityCustomer(),
                customer.getTokenNumber(),
                customer.getCreatedAt()
        );
        customerDAO.save(modelCust);

        System.out.println("\nCustomer Registered Successfully (Saved to SQL Database).");
        System.out.println("Customer ID : " + customer.getCustomerId());
    }

    // Display All Customers

    public void displayCustomers() {

        if (customers.isEmpty()) {

            System.out.println("\nNo Customers Registered.");
            return;
        }

        System.out.println("\n==========================================================================");
        System.out.printf("%-8s %-15s %-15s %-10s %-10s\n",
                "ID",
                "Name",
                "Service",
                "Priority",
                "Token");

        System.out.println("==========================================================================");

        for (Customer customer : customers) {

            System.out.println(customer);
        }

        System.out.println("==========================================================================");
    }

    // Generate Token

    public void generateToken(String customerId) {

        Customer foundCustomer = null;

        for (Customer customer : customers) {

            if (customer.getCustomerId().equalsIgnoreCase(customerId)) {
                foundCustomer = customer;
                break;
            }
        }

        if (foundCustomer != null) {

            if (!foundCustomer.getTokenNumber().equals("")) {

                System.out.println("Token Already Generated for this customer: " + foundCustomer.getTokenNumber());
                return;
            }

            // Persist Ticket into SQL via JDBC
            QueueTicket ticket = ticketDAO.issueTicket(
                    foundCustomer.getCustomerId(),
                    foundCustomer.getServiceType(),
                    foundCustomer.isPriorityCustomer()
            );

            String token = (ticket != null) ? ticket.getTokenNumber() : ("T" + tokenCounter++);
            foundCustomer.setTokenNumber(token);

            if (foundCustomer.isPriorityCustomer()) {

                priorityQueue.offer(foundCustomer);

            } else {

                normalQueue.offer(foundCustomer);
            }

            System.out.println("\nToken Generated Successfully (Persisted in SQL Database).");
            System.out.println("Token Number : " + token);

        } else {

            System.out.println("Customer Not Found with ID: " + customerId);
        }
    }

    // Display Queue

    public void displayQueue() {

        System.out.println("\n=========== PRIORITY QUEUE (VIP) ===========");

        if (priorityQueue.isEmpty()) {

            System.out.println("Empty");

        } else {

            for (Customer customer : priorityQueue) {

                System.out.println(customer);
            }
        }

        System.out.println("\n============ NORMAL QUEUE ============");

        if (normalQueue.isEmpty()) {

            System.out.println("Empty");

        } else {

            for (Customer customer : normalQueue) {

                System.out.println(customer);
            }
        }
    }

    // Call Next Customer

    public void callNextCustomer() {

        Customer customer;
        String counter = "Counter 1 - General Banking";

        if (!priorityQueue.isEmpty()) {

            customer = priorityQueue.poll();
            counter = "Counter 4 - Priority & VIP Desk";

        } else if (!normalQueue.isEmpty()) {

            customer = normalQueue.poll();

        } else {

            System.out.println("\nNo Customers Waiting in Queue.");
            return;
        }

        // Update database ticket status to SERVING
        QueueTicket ticket = ticketDAO.getNextTicketToServe();
        if (ticket != null) {
            ticketDAO.callTicket(ticket.getId(), counter);
        }

        System.out.println("\n=====================================");
        System.out.println("NOW SERVING at " + counter);
        System.out.println("=====================================");

        customer.displayDetails();
    }

    // Search Customer

    public void searchCustomer(String customerId) {

        for (Customer customer : customers) {

            if (customer.getCustomerId().equalsIgnoreCase(customerId)) {

                customer.displayDetails();
                return;
            }
        }

        // Check database fallback
        model.Customer dbCust = customerDAO.findById(customerId);
        if (dbCust != null) {
            dbCust.displayDetails();
            return;
        }

        System.out.println("Customer Not Found with ID: " + customerId);
    }

    // Queue Statistics

    public void showStatistics() {

        QueueStatistics stats = ticketDAO.getStatistics();

        StringBuilder report = new StringBuilder();

        report.append("\n========== QUEUE STATISTICS (LIVE SQL DB) ==========\n");
        report.append("Database Status : CONNECTED (").append(DatabaseManager.getInstance().getDatabaseType()).append(")\n");
        report.append("Total Customers : ").append(stats.getTotalCustomers()).append("\n");
        report.append("Priority Queue  : ").append(stats.getWaitingPriority()).append("\n");
        report.append("Normal Queue    : ").append(stats.getWaitingNormal()).append("\n");
        report.append("Currently Serving: ").append(stats.getCurrentlyServing()).append("\n");
        report.append("Completed Today : ").append(stats.getCompletedToday()).append("\n");
        int waitMins = stats.getEstimatedWaitMinutes();
        String waitStr = (waitMins >= 1440) ? "24 hours (Max Peak)" :
                         (waitMins >= 60) ? ((waitMins / 60) + " hrs " + (waitMins % 60) + " mins") :
                         (waitMins + " mins");
        report.append("Est. Wait Time  : ").append(waitStr).append("\n");
        report.append("=====================================================");

        System.out.println(report);
    }

    private Customer findCustomerInMemory(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(customerId)) {
                return c;
            }
        }
        return null;
    }

}