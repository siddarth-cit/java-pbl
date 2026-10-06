package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a Token/Ticket issued in the queue system.
 * Manages ticket lifecycle states: WAITING -> SERVING -> COMPLETED / CANCELLED.
 */
public class QueueTicket implements Comparable<QueueTicket> {

    public enum Status {
        WAITING,
        SERVING,
        COMPLETED,
        CANCELLED
    }

    private int id;
    private String tokenNumber;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String serviceType;
    private boolean priority;
    private Status status;
    private String counterAssigned;
    private String issuedAt;
    private String servedAt;
    private String completedAt;

    public QueueTicket(String tokenNumber,
                       String customerId,
                       String customerName,
                       String customerPhone,
                       String serviceType,
                       boolean priority) {
        this.tokenNumber = tokenNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceType = serviceType;
        this.priority = priority;
        this.status = Status.WAITING;
        this.counterAssigned = "";
        this.issuedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.servedAt = "";
        this.completedAt = "";
    }

    public QueueTicket(int id,
                       String tokenNumber,
                       String customerId,
                       String customerName,
                       String customerPhone,
                       String serviceType,
                       boolean priority,
                       Status status,
                       String counterAssigned,
                       String issuedAt,
                       String servedAt,
                       String completedAt) {
        this.id = id;
        this.tokenNumber = tokenNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceType = serviceType;
        this.priority = priority;
        this.status = status;
        this.counterAssigned = (counterAssigned == null) ? "" : counterAssigned;
        this.issuedAt = (issuedAt == null) ? "" : issuedAt;
        this.servedAt = (servedAt == null) ? "" : servedAt;
        this.completedAt = (completedAt == null) ? "" : completedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCounterAssigned() {
        return counterAssigned;
    }

    public void setCounterAssigned(String counterAssigned) {
        this.counterAssigned = counterAssigned;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getServedAt() {
        return servedAt;
    }

    public void setServedAt(String servedAt) {
        this.servedAt = servedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public int compareTo(QueueTicket other) {
        // Priority tickets always come before standard tickets
        if (this.priority && !other.priority) {
            return -1;
        }
        if (!this.priority && other.priority) {
            return 1;
        }
        // If equal priority, compare by token number / ID
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] Token: %-6s | Customer: %-12s | Service: %-15s | Status: %-9s | Counter: %s",
                priority ? "VIP" : "NORM",
                tokenNumber,
                customerName,
                serviceType,
                status,
                counterAssigned.isEmpty() ? "-" : counterAssigned
        );
    }
}
