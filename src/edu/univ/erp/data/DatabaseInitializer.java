package edu.univ.erp.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseInitializer {

    private static String baseUrl;
    private static String username;
    private static String password;

    public static void initializeDatabases() {
        System.out.println("=== Database Initialization Starting ===");

        loadConfig();

        // Check if databases exist
        if (!databaseExists("authdb") || !databaseExists("erpdb")) {
            System.out.println("Databases not found. Creating databases and tables...");
            executeSchemaFile();
            System.out.println("✓ Database initialization complete!");
        } else {
            System.out.println("✓ Databases already exist. Skipping initialization.");
        }

        // Test connections
        testConnections();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseInitializer.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                // Fallback to defaults
                baseUrl = "jdbc:mysql://localhost:3306/";
                username = "root";
                password = "Padro#5252";
                System.out.println("⚠ Using default database configuration");
                return;
            }

            properties.load(input);

            // Extract base URL (without database name)
            String erpUrl = properties.getProperty("jdbc.erp.url");
            baseUrl = erpUrl.substring(0, erpUrl.lastIndexOf("/") + 1);

            username = properties.getProperty("jdbc.user");
            password = properties.getProperty("jdbc.password", "").trim();

            System.out.println("✓ Configuration loaded from database.properties");

        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean databaseExists(String dbName) {
        String checkUrl = baseUrl + "?serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(checkUrl, username, password);
             Statement stmt = conn.createStatement()) {

            String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dbName + "'";
            var rs = stmt.executeQuery(query);
            return rs.next();

        } catch (Exception e) {
            System.err.println("Error checking database existence: " + e.getMessage());
            return false;
        }
    }

    private static void executeSchemaFile() {
        String schemaFile = "DATABASE_SCHEMA.sql";
        String connectUrl = baseUrl + "?allowMultiQueries=true&serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(connectUrl, username, password);
             InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(schemaFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println(" ERROR: " + schemaFile + " not found in resources folder!");
                System.err.println("Please ensure DATABASE_SCHEMA.sql is in src/resources/");
                return;
            }

            StringBuilder sql = new StringBuilder();
            String line;

            // Read entire SQL file
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                sql.append(line).append(" ");

                // Execute when we hit a semicolon
                if (line.endsWith(";")) {
                    String statement = sql.toString().trim();

                    if (!statement.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(statement);
                        } catch (Exception e) {
                            // Print but continue (some statements might fail if already exist)
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println(" Warning executing: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                                System.err.println("  " + e.getMessage());
                            }
                        }
                    }

                    sql.setLength(0); // Clear for next statement
                }
            }

            System.out.println("✓ Schema file executed successfully");

        } catch (Exception e) {
            System.err.println(" Error executing schema file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConnections() {
        try {
            // Test authdb
            String authUrl = baseUrl + "authdb?serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(authUrl, username, password)) {
                System.out.println("✓ Auth DB connection: OK");
            }

            // Test erpdb
            String erpUrl = baseUrl + "erpdb?serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(erpUrl, username, password)) {
                System.out.println("✓ ERP DB connection: OK");
            }

        } catch (Exception e) {
            System.err.println(" Connection test failed: " + e.getMessage());
        }
    }
}