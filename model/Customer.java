package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Demonstrates:
 * - Inheritance (extends Person)
 * - Polymorphism (overrides displayDetails)
 * - Encapsulation (private fields with accessors)
 * - Comparable interface (prioritization logic)
 * - Static member for auto-incrementing ID
 */
public class Customer extends Person implements Comparable<Customer> {

    private static int customerCounter = 1001;

    private String customerId;
    private String serviceType;
    private boolean priorityCustomer;
    private String tokenNumber;
    private String createdAt;

    /**
     * Standard constructor for newly registered customer
     */
    public Customer(String name,
                    int age,
                    String phone,
                    String serviceType,
                    boolean priorityCustomer) {
        super(name, age, phone);
        this.customerId = "C" + customerCounter++;
        this.serviceType = serviceType;
        this.priorityCustomer = priorityCustomer;
        this.tokenNumber = "";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Overloaded constructor for restoring customer from Database / existing ID
     */
    public Customer(String customerId,
                    String name,
                    int age,
                    String phone,
                    String serviceType,
                    boolean priorityCustomer,
                    String tokenNumber,
                    String createdAt) {
        super(name, age, phone);
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.priorityCustomer = priorityCustomer;
        this.tokenNumber = (tokenNumber == null) ? "" : tokenNumber;
        this.createdAt = (createdAt == null)
                ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : createdAt;

        // Synchronize static counter if higher ID loaded
        try {
            if (customerId != null && customerId.startsWith("C")) {
                int idNum = Integer.parseInt(customerId.substring(1));
                if (idNum >= customerCounter) {
                    customerCounter = idNum + 1;
                }
            }
        } catch (NumberFormatException ignored) {}
    }

    // Static counter getter & setter for DB synchronization
    public static int getCustomerCounter() {
        return customerCounter;
    }

    public static void setCustomerCounter(int counter) {
        customerCounter = counter;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isPriorityCustomer() {
        return priorityCustomer;
    }

    public void setPriorityCustomer(boolean priorityCustomer) {
        this.priorityCustomer = priorityCustomer;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Method Overriding (Polymorphism)
    @Override
    public void displayDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n--------------------------------------\n");
        builder.append("Customer ID : ").append(customerId).append("\n");
        builder.append("Name        : ").append(name).append("\n");
        builder.append("Age         : ").append(age).append("\n");
        builder.append("Phone       : ").append(phone).append("\n");
        builder.append("Service     : ").append(serviceType).append("\n");
        builder.append("Priority    : ").append(priorityCustomer ? "YES (VIP / Priority)" : "NO (Standard)").append("\n");
        builder.append("Token       : ").append(tokenNumber.isEmpty() ? "None" : tokenNumber).append("\n");
        builder.append("Registered  : ").append(createdAt).append("\n");
        builder.append("--------------------------------------");
        System.out.println(builder);
    }

    // Comparable implementation for PriorityQueue ordering
    @Override
    public int compareTo(Customer other) {
        if (this.priorityCustomer && !other.priorityCustomer) {
            return -1; // this comes first
        }
        if (!this.priorityCustomer && other.priorityCustomer) {
            return 1;  // other comes first
        }
        // If both have same priority flag, order by customer ID (FIFO order)
        return this.customerId.compareTo(other.customerId);
    }

    @Override
    public String toString() {
        return String.format(
                "%-8s %-15s %-15s %-10s %-10s",
                customerId,
                name,
                serviceType,
                priorityCustomer ? "YES" : "NO",
                tokenNumber.isEmpty() ? "-" : tokenNumber
        );
    }
}
