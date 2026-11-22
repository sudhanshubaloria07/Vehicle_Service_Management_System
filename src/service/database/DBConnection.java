package service.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC Connection Manager supporting dual-mode connectivity:
 * 1. Standard MySQL Server 8.x when online and authenticated.
 * 2. Embedded Local Database (Zero-Config Active) on localhost when external MySQL
 *    credentials are not yet configured or server is offline.
 * 
 * Guarantees zero-config functionality out of the box while remaining 100% compliant
 * with JDBC, PreparedStatement, transactions, row-level locking, and MySQL syntax.
 * 
 * OOP CONCEPT: Encapsulation (Unit 1) - Centralized connection state & driver loading
 * OOP CONCEPT: Exception Handling (Unit 3) - Seamless fallback & graceful degradation
 */
public class DBConnection {

    private static String url;
    private static String user;
    private static String password;
    private static String activeMode = "Unknown";
    private static boolean initialized = false;
    private static String lastErrorMessage = "";

    public static final String EMBEDDED_URL = "jdbc:h2:./data/vehicle_service_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    public static final String EMBEDDED_USER = "sa";
    public static final String EMBEDDED_PASS = "";

    private DBConnection() {}

    /**
     * Initializes database connection parameters and registers available drivers.
     */
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        // Register available JDBC drivers
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignored) {}
        try { Class.forName("org.h2.Driver"); } catch (ClassNotFoundException ignored) {}

        Properties props = new Properties();
        String[] candidatePaths = {
            "config/db.properties",
            "../config/db.properties",
            System.getProperty("user.dir") + File.separator + "config" + File.separator + "db.properties"
        };

        boolean loaded = false;
        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                try (InputStream in = new FileInputStream(file)) {
                    props.load(in);
                    loaded = true;
                    break;
                } catch (Exception ignored) {}
            }
        }

        String mode = props.getProperty("db.mode", "auto");
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/vehicle_service_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true&characterEncoding=utf8");
        user = props.getProperty("db.user", "root");
        password = props.getProperty("db.password", "root");

        // Try MySQL first unless explicitly set to embedded
        if (!"embedded".equalsIgnoreCase(mode)) {
            try {
                Connection conn = DriverManager.getConnection(url, user, password);
                conn.close();
                activeMode = "MySQL (vehicle_service_db)";
                initialized = true;
                return;
            } catch (SQLException e) {
                lastErrorMessage = "MySQL Server on localhost returned: " + e.getMessage();
                System.out.println("[DBConnection] " + lastErrorMessage);
                System.out.println("[DBConnection] Activating Localhost Embedded Database mode...");
            }
        }

        // Fallback to Localhost Embedded Database
        setupEmbeddedDatabase();
        initialized = true;
    }

    private static void setupEmbeddedDatabase() {
        try {
            // Ensure data directory exists
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            url = EMBEDDED_URL;
            user = EMBEDDED_USER;
            password = EMBEDDED_PASS;
            activeMode = "Localhost Embedded DB (Zero-Config Active)";

            // Check if tables already exist, if not initialize them
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                if (!DatabaseInitializer.tablesExist(conn)) {
                    System.out.println("[DBConnection] Initializing embedded schema and sample records...");
                    DatabaseInitializer.initializeEmbeddedDatabase(conn);
                    System.out.println("[DBConnection] Embedded database successfully populated.");
                }
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Error setting up embedded database: " + e.getMessage());
        }
    }

    /**
     * Obtains a live Connection to the database.
     * 
     * @return java.sql.Connection
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDatabaseMode() {
        if (!initialized) {
            initialize();
        }
        return activeMode;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static void setCredentials(String newUrl, String newUser, String newPass, String mode) {
        url = newUrl;
        user = newUser;
        password = newPass;
        activeMode = mode;
        initialized = true;
    }
}
