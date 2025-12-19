package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class handles all database operations for student grades, it manages the scores that instructors assign for various assessments.
public class GradeDAO {

    // this method is used to retrieve all grades for students in a particular section
    public List<Grade> getGradesForSection(int sectionId) throws SQLException {
        List<Grade> gradeList = new ArrayList<>();
        // this query joins grades with enrollments to filter grades by section
        String getSectionGradesQuery = "SELECT g.* FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement queryStatement = dbConnection.prepareStatement(getSectionGradesQuery)) {

            queryStatement.setInt(1, sectionId);
            try (ResultSet gradeResults = queryStatement.executeQuery()) {
                while (gradeResults.next()) {
                    gradeList.add(convertResultSetToGrade(gradeResults));
                }
            }
        }
        return gradeList;
    }



    // this method is used to save a new grade or update an existing one
    // it uses a smart query that inserts if the grade doesn't exist or updates if it does
    public void saveOrUpdate(Grade grade) throws SQLException {
        // if the grade already exists, we update it if not, we insert a new one.
        String saveGradeQuery = "INSERT INTO grades (enrollment_id, assessment_id, score) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement updateStatement = dbConnection.prepareStatement(saveGradeQuery)) {

            System.out.println("Saving grade - Enrollment ID: " + grade.getEnrollmentId() +
                    ", Assessment ID: " + grade.getAssessmentId() +
                    ", Score: " + grade.getScore());

            updateStatement.setInt(1, grade.getEnrollmentId());
            updateStatement.setInt(2, grade.getAssessmentId());

            // the score can be null as well if the instructor hasnt entered marks yet
            if (grade.getScore() == null) {
                updateStatement.setNull(3, Types.DECIMAL); // for the INSERT part
                updateStatement.setNull(4, Types.DECIMAL); // for the UPDATE part
            }
            else {
                updateStatement.setBigDecimal(3, grade.getScore());
                updateStatement.setBigDecimal(4, grade.getScore());
            }

            int rowsChanged = updateStatement.executeUpdate();
            System.out.println("Grade save operation affected " + rowsChanged + " row(s)");
        }
        catch (SQLException dbError) {
            System.err.println("Oops! Something went wrong while saving the grade: " + dbError.getMessage());
            throw dbError;
        }
    }



    // this method is used to convert a database result set row into a Grade object
    private Grade convertResultSetToGrade(ResultSet gradeData) throws SQLException {
        Grade newGrade = new Grade();
        newGrade.setGradeId(gradeData.getInt("grade_id"));
        newGrade.setEnrollmentId(gradeData.getInt("enrollment_id"));
        newGrade.setAssessmentId(gradeData.getInt("assessment_id"));
        newGrade.setScore(gradeData.getBigDecimal("score"));
        return newGrade;
    }
}