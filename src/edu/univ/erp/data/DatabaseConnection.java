package edu.univ.erp.data;

import java.sql.*;
import java.util.Properties; //Used to read configuration files
import java.io.InputStream; // this is used to read contents from database.properties
import java.io.IOException;

// DatabaseConnection class is present to manage the connections of the erpdb and the authdb databases which are made in MYSQL.
public class DatabaseConnection {
    // this is to create an instance of the DatabaseConnection class.
    private static DatabaseConnection instance;
    // each of the below variables are used to hold the connection of the respective databases.
    private Connection erpConnection;
    private Connection authConnection;

    // This path searches the 'resources' folder
    // this is to import all the database configs from database.properties file.
    private static final String CONFIG_FILE = "database.properties";

    //erpurl and authurl are used to hold the urls of the respective mysql databases.
    private String erpUrl;
    private String authUrl;
    // password and username is for authenticating the user to access the databases.
    private String username;
    private String password;
    private String driver;

    // I created a private constructor so that no other class can instantiate this class directly.
    private DatabaseConnection() {
        loadConfiguration();
        initializeConnections();
    }

    // this constructor is made to return the instance of the DatabaseConnection class.
    // this will only create one instance of the database connection class, and synchronised helps in this as well.
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    private void loadConfiguration() {
        Properties properties = new Properties();
        // trying to retreive the database configs from database.properties file.
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Configuration file " + CONFIG_FILE + " not found! Using defaults.");
                setDefaultProperties();
                return;
            }
            properties.load(input);

            this.erpUrl = properties.getProperty("jdbc.erp.url");
            this.authUrl = properties.getProperty("jdbc.auth.url");
            this.username = properties.getProperty("jdbc.user");
            this.password = properties.getProperty("jdbc.password").trim();

            this.driver = properties.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");

            if (this.erpUrl == null || this.authUrl == null || this.username == null) {
                System.err.println("One or more properties missing (jdbc.erp.url, jdbc.auth.url, jdbc.user). Using defaults.");
                setDefaultProperties();
            } else {
                System.out.println("Database configuration loaded successfully");
            }

        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }

    // for defualt values of the mysql configs in case the database.properties file is not found.
    private void setDefaultProperties() {
        this.driver = "com.mysql.cj.jdbc.Driver";
        this.erpUrl = "jdbc:mysql://localhost:3306/erpdb?serverTimezone=UTC";
        this.authUrl = "jdbc:mysql://localhost:3306/authdb?serverTimezone=UTC";
        this.username = "root";
        this.password = "Padro#5252";
    }


    private void initializeConnections() {
        try {
            // this will force java to load mysql driver in memory.
            Class.forName(driver);
            System.out.println("MySQL JDBC Driver loaded.");

            erpConnection = DriverManager.getConnection(erpUrl, username, password);
            System.out.println("ERP Database (erpdb) connection established.");

            authConnection = DriverManager.getConnection(authUrl, username, password);
            System.out.println("Auth Database (authdb) connection established.");

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection(s): " + e.getMessage());
            System.err.println("URL: " + erpUrl);
            System.err.println("User: " + username);
        }
    }

    public Connection getErpConnection() throws SQLException {
        if (erpConnection == null || erpConnection.isClosed()) {
            System.out.println("ERP Connection was closed, re-establishing...");
            initializeConnections();
        }
        return erpConnection;
    }

    public Connection getAuthConnection() throws SQLException {
        if (authConnection == null || authConnection.isClosed()) {
            System.out.println("Auth Connection was closed, re-establishing...");
            initializeConnections();
        }
        return authConnection;
    }


    public void closeConnections() {
        try {
            if (erpConnection != null && !erpConnection.isClosed()) {
                erpConnection.close();
                System.out.println("ERP Database connection closed.");
            }
            if (authConnection != null && !authConnection.isClosed()) {
                authConnection.close();
                System.out.println("Auth Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}