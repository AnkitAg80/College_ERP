package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Student;
import java.sql.*;
import java.util.*;

// this class is for handling all the student related queries from the database
public class StudentDAO {

    private DatabaseConnection dbConnection;

    public StudentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // this method is used to retreieve all the students that are enrolled in the same course from the database to the gradepanel.
    public List<Student> getEnrolledStudents(int sectionId) throws SQLException {
        List<Student> studentList = new ArrayList<>();

        // This query joins three tables to get complete student information
        String selectQuery = "SELECT s.user_id, s.fullName, s.roll_no, s.program, s.year, a.username AS email " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.user_id " +
                "JOIN authdb.users_auth a ON s.user_id = a.user_id " +
                "WHERE e.section_id = ? AND e.status = 'enrolled' " +
                "ORDER BY s.roll_no";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setUserId(resultSet.getInt("user_id"));
                    student.setFullName(resultSet.getString("fullName"));
                    student.setRollNo(resultSet.getString("roll_no"));
                    student.setProgram(resultSet.getString("program"));
                    student.setYear(resultSet.getInt("year"));
                    student.setEmail(resultSet.getString("email"));
                    studentList.add(student);
                }
            }
        }
        return studentList;
    }



    // this method is to create a new student record in the database
    public void create(Student student) throws SQLException {
        String insertQuery = "INSERT INTO students (user_id, fullName, roll_no, program, branch, year) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        // connect the database with the java.
        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getFullName());
            statement.setString(3, student.getRollNo());
            statement.setString(4, student.getProgram());
            statement.setString(5, student.getBranch());
            statement.setObject(6, student.getYear());
            // execute the insert query
            statement.executeUpdate();
        }
    }



    // getting the student record from the database through the unique userId
    public Student read(int userId) throws SQLException {
        String selectQuery = "SELECT s.*, a.username AS email " +
                "FROM students s " +
                "LEFT JOIN authdb.users_auth a ON s.user_id = a.user_id " +
                "WHERE s.user_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractStudentFromResultSet(resultSet);
                }
            }
        }
        return null;
    }



    // this is to show all the students in the database to the admin dashboard
    public List<Student> getAllStudents() throws SQLException {
        String selectQuery = "SELECT s.*, a.username AS email " +
                "FROM students s " +
                "LEFT JOIN authdb.users_auth a ON s.user_id = a.user_id " +
                "ORDER BY s.roll_no";
        List<Student> studentList = new ArrayList<>();

        try (Connection connection = dbConnection.getErpConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            while (resultSet.next()) {
                studentList.add(extractStudentFromResultSet(resultSet));
            }
        }
        return studentList;
    }



    // this is to update the student record in the database
    public void update(Student student) throws SQLException {
        String updateQuery = "UPDATE students SET roll_no=?, fullName=?, program=?, branch=?, " +
                "year_of_admission=?, cgpa=?, year=? WHERE user_id=?";
        // connect the erpdb with java
        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, student.getRollNo());
            statement.setString(2, student.getFullName());
            statement.setString(3, student.getProgram());
            statement.setString(4, student.getBranch());
            statement.setObject(5, student.getYearOfAdmission());
            statement.setObject(6, student.getCgpa());
            statement.setObject(7, student.getYear());
            statement.setInt(8, student.getUserId());
            //execute the update query
            statement.executeUpdate();
        }
    }



  // this method is to delete a student record from the database
    public void delete(int userId) throws SQLException {
        String deleteQuery = "DELETE FROM students WHERE user_id = ?";
        // connect the erpdb with java and execute the delete query
        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

            statement.setInt(1, userId);
            // execute the query.
            statement.executeUpdate();
        }
    }



    // this method is to convert the resultset to a student object
    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setUserId(rs.getInt("user_id"));
        student.setRollNo(rs.getString("roll_no"));
        student.setFullName(rs.getString("fullName"));
        student.setProgram(rs.getString("program"));
        student.setBranch(rs.getString("branch"));
        student.setYearOfAdmission(rs.getInt("year_of_admission"));
        student.setCgpa(rs.getBigDecimal("cgpa"));
        student.setYear(rs.getInt("year"));

        // Check if the 'email' column exists in the result set
        // This is necessary because not all queries include the email column
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if ("email".equalsIgnoreCase(metaData.getColumnName(i))) {
                student.setEmail(rs.getString("email"));
                break;
            }
        }
        return student;
    }
}