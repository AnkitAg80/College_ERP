package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.CourseOffering;
import java.sql.*;
import java.util.*;

// this class handles all database operations for course offerings.
public class CourseOfferingDAO {

    private DatabaseConnection dbConnection;

    public CourseOfferingDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // this method is used to create a new course offering in the erpdb
    public void create(CourseOffering offering) throws SQLException {
        String insertQuery = "INSERT INTO course_offerings (branch_id, semester, course_id) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, offering.getBranchId());
            statement.setInt(2, offering.getSemester());
            statement.setInt(3, offering.getCourseId());
            statement.executeUpdate();

            // Get the newly generated offering ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    offering.setOfferingId(generatedKeys.getInt(1));
                }
            }
        }
    }



    // this method is used to retrieve a course offering by its id from the erpdb
    public CourseOffering read(int offeringId) throws SQLException {
        String selectQuery = "SELECT * FROM course_offerings WHERE offering_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, offeringId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCourseOfferingFromResultSet(resultSet);
                }
            }
        }
        return null;
    }



    // this method is used to update an existing course offering
    public void update(CourseOffering offering) throws SQLException {
        String updateQuery = "UPDATE course_offerings SET branch_id=?, semester=?, course_id=? WHERE offering_id=?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, offering.getBranchId());
            statement.setInt(2, offering.getSemester());
            statement.setInt(3, offering.getCourseId());
            statement.setInt(4, offering.getOfferingId());
            statement.executeUpdate();
        }
    }

    // this method is used to delete a course offering from the database
    public void delete(int offeringId) throws SQLException {
        String deleteQuery = "DELETE FROM course_offerings WHERE offering_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

            statement.setInt(1, offeringId);
            statement.executeUpdate();
        }
    }
    // this method is used to convert a ResultSet row to a CourseOffering object
    private CourseOffering extractCourseOfferingFromResultSet(ResultSet rs) throws SQLException {
        CourseOffering offering = new CourseOffering();
        offering.setOfferingId(rs.getInt("offering_id"));
        offering.setSemester(rs.getInt("semester"));
        offering.setCourseId(rs.getInt("course_id"));
        offering.setBranchId(rs.getInt("branch_id"));
        return offering;
    }
}