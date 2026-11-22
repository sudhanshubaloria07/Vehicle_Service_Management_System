package service.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility helper to test MySQL credentials, update config/db.properties,
 * and automatically execute sql/schema.sql and sql/sample_data.sql over JDBC.
 * 
 * Supports both MySQL and Localhost Embedded Database mode.
 * 
 * OOP CONCEPT: Encapsulation & Separation of Concerns (Unit 1 & 2)
 * OOP CONCEPT: Exception Handling (Unit 3) - Resource cleanup and error reporting
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {}

    /**
     * Tests whether a database server is accessible with the provided credentials.
     */
    public static boolean testCredentials(String url, String user, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return conn != null && !conn.isClosed();
        }
    }

    /**
     * Checks if tables exist in the current connection.
     */
    public static boolean tablesExist(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Jobs")) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initializes the local embedded database by running schema and sample data.
     */
    public static void initializeEmbeddedDatabase(Connection conn) throws Exception {
        executeSqlFile(conn, "sql/schema.sql", true);
        executeSqlFile(conn, "sql/sample_data.sql", true);
    }

    /**
     * Connects to MySQL, executes schema and sample data scripts, and updates db.properties.
     */
    public static void initializeDatabase(String host, int port, String user, String password) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 1. Connect to MySQL server without database specified to create vehicle_service_db
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=utf8";
        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `vehicle_service_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        }

        // 2. Connect to vehicle_service_db and execute schema.sql and sample_data.sql
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/vehicle_service_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=utf8";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            executeSqlFile(conn, "sql/schema.sql", false);
            executeSqlFile(conn, "sql/sample_data.sql", false);
        }

        // 3. Persist updated configuration into config/db.properties
        saveProperties(dbUrl, user, password, "mysql");

        // 4. Update live DBConnection state
        DBConnection.setCredentials(dbUrl, user, password, "MySQL (vehicle_service_db)");
    }

    /**
     * Reads and executes a multi-statement SQL script file.
     * 
     * @param conn Connection instance
     * @param relativePath Path to SQL file
     * @param isEmbedded True if running on H2 embedded engine (skips CREATE DATABASE / USE)
     */
    public static void executeSqlFile(Connection conn, String relativePath, boolean isEmbedded) throws Exception {
        File file = new File(relativePath);
        if (!file.exists()) {
            file = new File("../" + relativePath);
        }
        if (!file.exists()) {
            file = new File(System.getProperty("user.dir") + File.separator + relativePath.replace("/", File.separator));
        }

        if (!file.exists()) {
            throw new Exception("SQL script file not found at: " + relativePath);
        }

        try (InputStream in = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.startsWith("/*")) {
                    continue;
                }
                // Skip MySQL specific database statements if embedded
                if (isEmbedded && (trimmed.toUpperCase().startsWith("CREATE DATABASE") || trimmed.toUpperCase().startsWith("USE "))) {
                    continue;
                }

                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            // Suppress drop table errors if not existing yet
                            if (!sql.toUpperCase().startsWith("DROP TABLE")) {
                                throw ex;
                            }
                        }
                    }
                    sb.setLength(0); // Reset buffer
                }
            }
        }
    }

    /**
     * Saves updated database credentials into config/db.properties.
     */
    public static void saveProperties(String url, String user, String password, String mode) {
        String[] candidatePaths = {
            "config/db.properties",
            "../config/db.properties",
            System.getProperty("user.dir") + File.separator + "config" + File.separator + "db.properties"
        };

        File target = null;
        for (String p : candidatePaths) {
            File f = new File(p);
            if (f.exists()) {
                target = f;
                break;
            }
        }
        if (target == null) {
            target = new File("config/db.properties");
            target.getParentFile().mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(target)) {
            Properties props = new Properties();
            props.setProperty("db.url", url);
            props.setProperty("db.user", user);
            props.setProperty("db.password", password);
            props.setProperty("db.mode", mode);
            props.store(out, "Database Configuration for Vehicle Service Management System (VSMS)");
        } catch (Exception e) {
            System.err.println("Could not save config/db.properties: " + e.getMessage());
        }
    }
}
