package edu.univ.erp.test;

import edu.univ.erp.data.DatabaseConnection; // Make sure this import is correct
import java.sql.Connection;

// this file is to test whether the database is connected or not while logging in.
public class TestDatabaseConnection {

    public static void main(String[] args) {
        System.out.println("Testing Database Connections");

        // this is to check whether the erp database is connected or not.
        System.out.println("\n[1] Attempting to connect to ERP Database (erpdb)...");
        try (Connection conn = DatabaseConnection.getInstance().getErpConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: ErpDB Connection is valid.");
            } else {
                System.err.println("FAILED: ErpDB Connection is null or closed.");
            }
        } catch (Exception e) {
            System.err.println("FAILED: Could not connect to ErpDB.");
            e.printStackTrace();
        }

        // testing the database connectin with auth database.
        System.out.println("\n[2] Attempting to connect to Auth Database (authdb)...");
        try (Connection conn = DatabaseConnection.getInstance().getAuthConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(" SUCCESS: AuthDB Connection is valid.");
            } else {
                System.err.println(" FAILED: AuthDB Connection is null or closed.");
            }
        } catch (Exception e) {
            System.err.println("   ✗ FAILED: Could not connect to AuthDB.");
            e.printStackTrace();
        }

        System.out.println("\nTest Complete");
    }
}