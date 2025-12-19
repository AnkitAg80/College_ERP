package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class handles all database operations for assessments, this is accessed in the gradebook panel.
public class AssessmentDAO {

    private DatabaseConnection dbConnection;

    public AssessmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // this method will create a new assessment in the database with the name, max_score, and its weightage and return the generated assessment ID
    public int create(Assessment assessment) throws SQLException {
        String insertQuery = "INSERT INTO assessments (section_id, name, max_score, weight) VALUES (?, ?, ?, ?)";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, assessment.getSectionId());
            statement.setString(2, assessment.getName());
            statement.setBigDecimal(3, assessment.getMaxScore());
            statement.setBigDecimal(4, assessment.getWeight());
            // execute the insert into the table
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    assessment.setAssessmentId(newId);
                    return newId;
                } else {
                    throw new SQLException("Failed to create assessment, no ID obtained.");
                }
            }
        }
    }



    // this method is there for getting the assessments from the database for a particular section, it will give all the
    // values in the given assessment id.
    public List<Assessment> getAssessmentsForSection(int sectionId) throws SQLException {
        List<Assessment> assessmentList = new ArrayList<>();
        String selectQuery = "SELECT * FROM assessments WHERE section_id = ? ORDER BY created_at";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, sectionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assessmentList.add(convertToAssessment(resultSet));
                }
            }
        }
        return assessmentList;
    }



    // this method will delete an assessment from the database based on the assessment id
    public void delete(int assessmentId) throws SQLException {
        String deleteQuery = "DELETE FROM assessments WHERE assessment_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, assessmentId);
            statement.executeUpdate();
        }
    }



    // even after the assesment is created and the values are added in that but still through this method we can update
    // the values in that particular assessment
    public void update(Assessment assessment) throws SQLException {
        String updateQuery = "UPDATE assessments SET name = ?, max_score = ?, weight = ? WHERE assessment_id = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, assessment.getName());
            statement.setBigDecimal(2, assessment.getMaxScore());
            statement.setBigDecimal(3, assessment.getWeight());
            statement.setInt(4, assessment.getAssessmentId());
            statement.executeUpdate();
        }
    }



    // this method will convert the result set obtained from the database into an assessment object
    private Assessment convertToAssessment(ResultSet rs) throws SQLException {
        Assessment assessment = new Assessment();
        assessment.setAssessmentId(rs.getInt("assessment_id"));
        assessment.setSectionId(rs.getInt("section_id"));
        assessment.setName(rs.getString("name"));
        assessment.setMaxScore(rs.getBigDecimal("max_score"));
        assessment.setWeight(rs.getBigDecimal("weight"));
        assessment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return assessment;
    }
}