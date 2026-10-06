import ui.QueueDashboardFrame;
import ui.ThemeUtils;

import javax.swing.SwingUtilities;

/**
 * Main application launcher for Smart Digital Queue Management System.
 *
 * Usage:
 *   java -cp "lib/*:." App         -> Launches Modern Java Swing GUI
 *   java -cp "lib/*:." App --cli   -> Launches Terminal Console CLI
 */
public class App {

    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            main.main(args);
            return;
        }

        // Default: Launch rich Java Swing GUI
        System.out.println("=============================================================");
        System.out.println(" Starting Smart Digital Queue Management System (Swing GUI)...");
        System.out.println(" Database: SQLite JDBC (queue_system.db)");
        System.out.println(" Pass '--cli' argument to run in interactive terminal mode.");
        System.out.println("=============================================================");

        SwingUtilities.invokeLater(() -> {
            ThemeUtils.applyTheme(true);
            QueueDashboardFrame frame = new QueueDashboardFrame();
            frame.setVisible(true);
        });
    }
}
