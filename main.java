import service.QueueService;
import ui.QueueDashboardFrame;
import ui.ThemeUtils;

import javax.swing.SwingUtilities;
import java.util.InputMismatchException;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        QueueManager manager = new QueueManager();

        int choice;

        do {

            System.out.println("\n==============================================");
            System.out.println("      SMART DIGITAL QUEUE MANAGEMENT");
            System.out.println("  [Full JDBC & SQL Backend • GUI Available]   ");
            System.out.println("==============================================");
            System.out.println("1. Register Customer");
            System.out.println("2. Display Customers");
            System.out.println("3. Generate Token");
            System.out.println("4. Display Queue");
            System.out.println("5. Call Next Customer");
            System.out.println("6. Search Customer");
            System.out.println("7. Queue Statistics");
            System.out.println("8. Launch Java Swing GUI Dashboard 🖥️");
            System.out.println("9. Seed Demo Bank Customers & Tickets 🌱");
            System.out.println("10. Load 100,000 (1 Lakh) Tirupati Devotees (Stress Test) 🚀");
            System.out.println("11. Exit");
            System.out.println("==============================================");
            System.out.print("Enter Choice : ");

            try {

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:

                        System.out.print("\nEnter Customer Name : ");
                        String name = sc.nextLine();

                        System.out.print("Enter Age : ");
                        int age = sc.nextInt();
                        sc.nextLine();

                        System.out.print("Enter Phone Number : ");
                        String phone = sc.nextLine();

                        System.out.print("Enter Service Required : ");
                        String service = sc.nextLine();

                        System.out.print("Priority Customer (true/false) : ");
                        boolean priority = sc.nextBoolean();
                        sc.nextLine();

                        Customer customer = new Customer(
                                name,
                                age,
                                phone,
                                service,
                                priority
                        );

                        manager.registerCustomer(customer);

                        break;

                    case 2:

                        manager.displayCustomers();

                        break;

                    case 3:

                        System.out.print("Enter Customer ID : ");

                        String id = sc.nextLine();

                        manager.generateToken(id);

                        break;

                    case 4:

                        manager.displayQueue();

                        break;

                    case 5:

                        manager.callNextCustomer();

                        break;

                    case 6:

                        System.out.print("Enter Customer ID : ");

                        String search = sc.nextLine();

                        manager.searchCustomer(search);

                        break;

                    case 7:

                        manager.showStatistics();

                        break;

                    case 8:

                        System.out.println("\nLaunching Java Swing GUI Dashboard in background...");
                        SwingUtilities.invokeLater(() -> {
                            ThemeUtils.applyTheme(true);
                            QueueDashboardFrame frame = new QueueDashboardFrame();
                            frame.setVisible(true);
                        });
                        break;

                    case 9:

                        System.out.println("\nSeeding realistic demo bank queue data...");
                        QueueService.getInstance().seedSampleData();
                        manager.syncFromDatabase();
                        System.out.println("Demo data loaded into SQL Database!");
                        manager.showStatistics();
                        break;

                    case 10:

                        System.out.println("\nExecuting High-Speed JDBC Batch Insertion for 100,000 Devotees...");
                        long t0 = System.currentTimeMillis();
                        QueueService.getInstance().seedOneLakhDevotees();
                        manager.syncFromDatabase();
                        long dur = System.currentTimeMillis() - t0;
                        System.out.println("100,000 Devotees loaded into SQLite Database in " + (dur / 1000.0) + " seconds!");
                        manager.showStatistics();
                        break;

                    case 11:

                        System.out.println("\nThank you for using Smart Queue Management System.");

                        break;

                    default:

                        System.out.println("\nInvalid Choice.");

                }

            }

            catch (InputMismatchException e) {

                System.out.println("\nInvalid Input!");

                sc.nextLine();

                choice = 0;
            }

        } while (choice != 11);

        sc.close();

    }

}