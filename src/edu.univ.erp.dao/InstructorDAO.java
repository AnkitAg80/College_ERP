package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Instructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class manages all database operations for our instructor profiles, it handles creating, reading, and updating instructor information in the system.
public class InstructorDAO {

    // this method is used to create a new instructor profile in erpdb
    public void create(Instructor instructor) throws SQLException {
        String addInstructorQuery = "INSERT INTO instructors (user_id, fullName, department, email) VALUES (?, ?, ?, ?)";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement addInstructorStatement = dbConnection.prepareStatement(addInstructorQuery)) {

            addInstructorStatement.setInt(1, instructor.getUserId());
            addInstructorStatement.setString(2, instructor.getFullName());
            addInstructorStatement.setString(3, instructor.getDepartment());
            addInstructorStatement.setString(4, instructor.getEmail());

            addInstructorStatement.executeUpdate();
        }
    }



    // this method is used to retrieve all instructors from our erpdb from instructors table, sorted alphabetically by name
    public List<Instructor> getAllInstructors() throws SQLException {
        String getAllInstructorsQuery = "SELECT i.user_id, i.fullName, i.department, i.email " +
                "FROM instructors i " +
                "ORDER BY i.fullName";
        List<Instructor> instructorList = new ArrayList<>();

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             Statement getAllStatement = dbConnection.createStatement();
             ResultSet instructorResults = getAllStatement.executeQuery(getAllInstructorsQuery)) {

            while (instructorResults.next()) {
                instructorList.add(convertResultSetToInstructor(instructorResults));
            }
        }
        return instructorList;
    }



    // this method is used to find a specific instructor using their user ID
    public Instructor getInstructorByUserId(int userId) throws SQLException {
        String findInstructorQuery = "SELECT i.user_id, i.fullName, i.department, i.email " +
                "FROM instructors i " +
                "WHERE i.user_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement findInstructorStatement = dbConnection.prepareStatement(findInstructorQuery)) {

            findInstructorStatement.setInt(1, userId);

            try (ResultSet instructorResults = findInstructorStatement.executeQuery()) {
                if (instructorResults.next()) {
                    return convertResultSetToInstructor(instructorResults);
                }
            }
        }
        return null; // No instructor found with this user ID
    }


    // this method is used to retrieve an instructor by their instructor ID
    public Instructor read(int instructorId) throws SQLException {
        return getInstructorByUserId(instructorId);
    }

    // this method is used to convert a database result set row into an instructor object
    private Instructor convertResultSetToInstructor(ResultSet instructorData) throws SQLException {
        Instructor newInstructor = new Instructor();
        newInstructor.setUserId(instructorData.getInt("user_id"));
        newInstructor.setFullName(instructorData.getString("fullName"));
        newInstructor.setDepartment(instructorData.getString("department"));
        newInstructor.setEmail(instructorData.getString("email"));
        return newInstructor;
    }
}