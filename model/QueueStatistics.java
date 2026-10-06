package model;

/**
 * Encapsulates real-time metrics for queue operations.
 */
public class QueueStatistics {

    private int totalCustomers;
    private int totalTicketsToday;
    private int waitingNormal;
    private int waitingPriority;
    private int currentlyServing;
    private int completedToday;
    private int cancelledToday;

    public QueueStatistics(int totalCustomers,
                           int totalTicketsToday,
                           int waitingNormal,
                           int waitingPriority,
                           int currentlyServing,
                           int completedToday,
                           int cancelledToday) {
        this.totalCustomers = totalCustomers;
        this.totalTicketsToday = totalTicketsToday;
        this.waitingNormal = waitingNormal;
        this.waitingPriority = waitingPriority;
        this.currentlyServing = currentlyServing;
        this.completedToday = completedToday;
        this.cancelledToday = cancelledToday;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public int getTotalTicketsToday() {
        return totalTicketsToday;
    }

    public int getWaitingNormal() {
        return waitingNormal;
    }

    public int getWaitingPriority() {
        return waitingPriority;
    }

    public int getTotalWaiting() {
        return waitingNormal + waitingPriority;
    }

    public int getCurrentlyServing() {
        return currentlyServing;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public int getCancelledToday() {
        return cancelledToday;
    }

    public int getEstimatedWaitMinutes() {
        int waiting = getTotalWaiting();
        if (waiting <= 0) return 0;

        // Small queues (< 30 people, e.g., bank counter)
        if (waiting <= 30) {
            return waiting * 3;
        }

        // High-capacity queues (e.g. Tirupati VQC compartments, ~4,000 to 4,500 devotees cleared/hr)
        // Believable progression: 2 hours to a hard maximum of 24 hours (1440 minutes)
        int minutes = 120 + (int) ((waiting / 100000.0) * 1200);

        // Strict 24-hour maximum cap
        if (minutes > 1440) {
            minutes = 1440;
        }

        return minutes;
    }

    public double getCompletionRate() {
        int total = completedToday + currentlyServing + getTotalWaiting();
        if (total == 0) return 100.0;
        return (completedToday * 100.0) / total;
    }
}
