package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DatabaseManager implements the Singleton pattern for JDBC connectivity.
 * Supports SQLite (embedded, zero-configuration out-of-the-box) and MySQL.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private String databaseType = "SQLite";
    private String dbUrl = "jdbc:sqlite:queue_system.db";
    private String dbUser = "";
    private String dbPassword = "";

    private DatabaseManager() {
        loadConfiguration();
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Attempts to read db.properties if it exists, otherwise defaults to local SQLite.
     */
    private void loadConfiguration() {
        File configFile = new File("db.properties");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(input);

                String type = props.getProperty("db.type", "sqlite").trim().toLowerCase();
                if (type.equals("mysql")) {
                    databaseType = "MySQL";
                    String host = props.getProperty("mysql.host", "localhost");
                    String port = props.getProperty("mysql.port", "3306");
                    String database = props.getProperty("mysql.database", "queue_system");
                    dbUser = props.getProperty("mysql.user", "root");
                    dbPassword = props.getProperty("mysql.password", "");
                    dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
                } else {
                    databaseType = "SQLite";
                    String dbPath = props.getProperty("sqlite.path", "queue_system.db");
                    dbUrl = "jdbc:sqlite:" + dbPath;
                }
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Could not read db.properties, falling back to SQLite: " + e.getMessage());
                databaseType = "SQLite";
                dbUrl = "jdbc:sqlite:queue_system.db";
            }
        } else {
            databaseType = "SQLite";
            dbUrl = "jdbc:sqlite:queue_system.db";
        }
    }

    /**
     * Establishes or returns the active JDBC Connection.
     */
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                if ("MySQL".equalsIgnoreCase(databaseType)) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                    } catch (ClassNotFoundException e) {
                        System.err.println("MySQL Driver not found in classpath.");
                    }
                    connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                } else {
                    try {
                        Class.forName("org.sqlite.JDBC");
                    } catch (ClassNotFoundException e) {
                        System.err.println("SQLite Driver not found in classpath.");
                    }
                    connection = DriverManager.getConnection(dbUrl);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Connection failure: " + e.getMessage());
            throw e;
        }
        return connection;
    }

    /**
     * Initializes tables and constraints on startup.
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Customers Table
            String createCustomersTable =
                    "CREATE TABLE IF NOT EXISTS customers (" +
                    "  customer_id VARCHAR(20) PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL," +
                    "  age INTEGER NOT NULL," +
                    "  phone VARCHAR(20) NOT NULL," +
                    "  service_type VARCHAR(50) NOT NULL," +
                    "  is_priority INTEGER NOT NULL DEFAULT 0," +
                    "  token_number VARCHAR(20) DEFAULT ''," +
                    "  created_at VARCHAR(30) NOT NULL" +
                    ");";
            stmt.execute(createCustomersTable);

            // 2. Queue Tickets Table
            String autoInc = "MySQL".equalsIgnoreCase(databaseType) ? "AUTO_INCREMENT" : "AUTOINCREMENT";
            String createTicketsTable =
                    "CREATE TABLE IF NOT EXISTS queue_tickets (" +
                    "  id INTEGER PRIMARY KEY " + autoInc + "," +
                    "  token_number VARCHAR(20) NOT NULL," +
                    "  customer_id VARCHAR(20) NOT NULL," +
                    "  customer_name VARCHAR(100) NOT NULL," +
                    "  customer_phone VARCHAR(20) NOT NULL," +
                    "  service_type VARCHAR(50) NOT NULL," +
                    "  is_priority INTEGER NOT NULL DEFAULT 0," +
                    "  status VARCHAR(20) NOT NULL DEFAULT 'WAITING'," +
                    "  counter_assigned VARCHAR(50) DEFAULT ''," +
                    "  issued_at VARCHAR(30) NOT NULL," +
                    "  served_at VARCHAR(30) DEFAULT ''," +
                    "  completed_at VARCHAR(30) DEFAULT ''" +
                    ");";
            stmt.execute(createTicketsTable);

            // 3. Service Counters Table
            String createCountersTable =
                    "CREATE TABLE IF NOT EXISTS service_counters (" +
                    "  counter_id INTEGER PRIMARY KEY," +
                    "  name VARCHAR(50) NOT NULL UNIQUE," +
                    "  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'" +
                    ");";
            stmt.execute(createCountersTable);

            // Performance Indexes for 100k+ scale
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tickets_status ON queue_tickets(status, is_priority, id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tickets_cust ON queue_tickets(customer_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cust_phone ON customers(phone);");

            // Seed default counters if empty
            stmt.execute("INSERT OR IGNORE INTO service_counters (counter_id, name, status) VALUES " +
                    "(1, 'Counter 1 - General Banking', 'AVAILABLE'), " +
                    "(2, 'Counter 2 - Cashier & Deposits', 'AVAILABLE'), " +
                    "(3, 'Counter 3 - Loans & Mortgages', 'AVAILABLE'), " +
                    "(4, 'Counter 4 - Priority & VIP Desk', 'AVAILABLE');");

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] DB Schema Initialization Error: " + e.getMessage());
        }
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
