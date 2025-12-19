package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Enrollment;
import java.sql.*;
import java.util.*;

// this class is responsible for managing database operations related to student enrollments in course sections.
public class EnrollmentDAO {

    private DatabaseConnection dbConnection;

    public EnrollmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }



    // this method is used to create a new enrollment record of the student in a particular section
    public void create(Enrollment enrollment) throws SQLException {
        String insertQuery = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";

        //making the connection
        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, enrollment.getStudentId());
            statement.setInt(2, enrollment.getSectionId());
            statement.setString(3, enrollment.getStatus());
            // executing the query
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    enrollment.setEnrollmentId(generatedKeys.getInt(1));
                }
            }
        }
    }



   // method to read an enrollment record by its enrollment_id
    public Enrollment read(int enrollmentId) throws SQLException {
        String selectQuery = "SELECT * FROM enrollments WHERE enrollment_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, enrollmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractEnrollmentFromResultSet(resultSet);
                }
            }
        }
        return null;
    }



   // this method is used to update an existing enrollment record
    public void update(Enrollment enrollment) throws SQLException {
        String updateQuery = "UPDATE enrollments SET student_id=?, section_id=?, status=? WHERE enrollment_id=?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, enrollment.getStudentId());
            statement.setInt(2, enrollment.getSectionId());
            statement.setString(3, enrollment.getStatus());
            statement.setInt(4, enrollment.getEnrollmentId());
            statement.executeUpdate();
        }
    }



    // this method is used to delete an enrollment record by its enrollment_id
    public void delete(int enrollmentId) throws SQLException {
        String deleteQuery = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

            statement.setInt(1, enrollmentId);
            statement.executeUpdate();
        }
    }



    // this method is used to retrieve all enrollments for a specific student basically this tells the total courses of a student.
    public List<Enrollment> getEnrollmentsByStudent(int studentId) throws SQLException {
        String selectQuery = "SELECT * FROM enrollments WHERE student_id = ?";
        List<Enrollment> enrollmentList = new ArrayList<>();

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollmentList.add(extractEnrollmentFromResultSet(resultSet));
                }
            }
        }
        return enrollmentList;
    }



    // this method is used to retrieve all enrollments for a specific section
    // this is used by the instructor to see all students enrolled in their section
    public List<Enrollment> getEnrollmentsBySection(int sectionId) throws SQLException {
        String selectQuery = "SELECT * FROM enrollments WHERE section_id = ?";
        List<Enrollment> enrollmentList = new ArrayList<>();

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollmentList.add(extractEnrollmentFromResultSet(resultSet));
                }
            }
        }
        return enrollmentList;
    }



    // this method returns the count of students enrolled in a specific section
    public int getEnrollmentCountForSection(int sectionId) throws SQLException {
        String countQuery = "SELECT COUNT(*) as count FROM enrollments WHERE section_id = ? AND status = 'enrolled'";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(countQuery)) {

            statement.setInt(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count");
                }
            }
        }
        return 0;
    }



    // this method checks if a student is already enrolled in a specific section
    public boolean isStudentEnrolledInSection(int studentId, int sectionId) throws SQLException {
        String checkQuery = "SELECT COUNT(*) as count FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'enrolled'";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(checkQuery)) {

            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") > 0;
                }
            }
        }
        return false;
    }



    // this method is used to update the final grade and CGPA for an enrollment
    public void updateFinalGrade(int enrollmentId, String finalGrade, Double cgpa) throws SQLException {
        String updateQuery = "UPDATE enrollments SET final_grade = ?, cgpa = ? WHERE enrollment_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, finalGrade);
            // cgpa can be null if not calculated yet
            if (cgpa != null) {
                statement.setDouble(2, cgpa);
            } else {
                statement.setNull(2, java.sql.Types.DOUBLE);
            }
            statement.setInt(3, enrollmentId);
            statement.executeUpdate();
        }
    }



    // this method is used to convert a ResultSet row to an Enrollment object
    private Enrollment extractEnrollmentFromResultSet(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSectionId(rs.getInt("section_id"));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setFinalGrade(rs.getString("final_grade"));
        Double cgpaValue = rs.getObject("cgpa", Double.class);
        enrollment.setCgpa(cgpaValue);
        return enrollment;
    }
}