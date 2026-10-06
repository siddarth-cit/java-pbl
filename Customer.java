import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Demonstrates:
 * - Abstract Class
 * - Inheritance
 * - Encapsulation
 * - Constructors
 * - Polymorphism
 * - Static Members
 * - Comparable Interface
 */

abstract class Person {

    protected String name;
    protected int age;
    protected String phone;

    public Person(String name, int age, String phone) {
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    // Abstract Method
    public abstract void displayDetails();
}

public class Customer extends Person implements Comparable<Customer> {

    // Static Member for auto-incrementing ID
    private static int customerCounter = 1001;

    private String customerId;
    private String serviceType;
    private boolean priorityCustomer;
    private String tokenNumber;
    private String createdAt;

    // Primary Constructor
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

    // Overloaded Constructor for Database loading
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

        try {
            if (customerId != null && customerId.startsWith("C")) {
                int idNum = Integer.parseInt(customerId.substring(1));
                if (idNum >= customerCounter) {
                    customerCounter = idNum + 1;
                }
            }
        } catch (NumberFormatException ignored) {}
    }

    public static int getCustomerCounter() {
        return customerCounter;
    }

    public static void setCustomerCounter(int counter) {
        customerCounter = counter;
    }

    // Getters

    public String getCustomerId() {
        return customerId;
    }

    public boolean isPriorityCustomer() {
        return priorityCustomer;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setPriorityCustomer(boolean priorityCustomer) {
        this.priorityCustomer = priorityCustomer;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
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
        builder.append("Priority    : ");

        if (priorityCustomer)
            builder.append("YES (VIP / Priority)");
        else
            builder.append("NO (Standard)");

        builder.append("\nToken       : ").append(tokenNumber.isEmpty() ? "None" : tokenNumber);
        builder.append("\nRegistered  : ").append(createdAt);
        builder.append("\n--------------------------------------");

        System.out.println(builder);
    }

    // Comparable for PriorityQueue

    @Override
    public int compareTo(Customer other) {

        if (this.priorityCustomer && !other.priorityCustomer)
            return -1;

        if (!this.priorityCustomer && other.priorityCustomer)
            return 1;

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