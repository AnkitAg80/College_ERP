package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Branch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class manages all database operations for our branch related queries.
public class BranchDAO {

    private DatabaseConnection dbConnection;

    public BranchDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // this method is to create a new branch in the database,and return true or false if the branch is created or not.
    public boolean createBranch(String code, String name) throws SQLException {
        String insertQuery = "INSERT INTO branches (branch_code, name) VALUES (?, ?)";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setString(1, code);
            statement.setString(2, name);

            int result = statement.executeUpdate();
            return result > 0;
        }
    }



    // this method retrieves all branches from the database and sorts them by name
    public List<Branch> getAllBranches() throws SQLException {
        String selectQuery = "SELECT * FROM branches ORDER BY name";
        List<Branch> branchList = new ArrayList<>();

        try (Connection connection = dbConnection.getErpConnection();
             Statement statement = connection.createStatement();
             // executing the query
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            while (resultSet.next()) {
                branchList.add(extractBranchFromResultSet(resultSet));
            }
        }
        return branchList;
    }

    // we are saving the branch with its unique id, this method gets the branch from the database through that id.
    public Branch getBranchByCode(String code) throws SQLException {
        String selectQuery = "SELECT * FROM branches WHERE branch_code = ?";

        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractBranchFromResultSet(resultSet);
                }
            }
        }
        return null;
    }



    // this method is created to remove a branch by its id from the database
    public boolean deleteBranch(int id) throws SQLException {
        String deleteQuery = "DELETE FROM branches WHERE branch_id = ?";
        // connect with erpdb and execute the delete query
        try (Connection connection = dbConnection.getErpConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }



    // this method is for converting the resultset to Branch object
    private Branch extractBranchFromResultSet(ResultSet rs) throws SQLException {
        return new Branch(
                rs.getInt("branch_id"),
                rs.getString("branch_code"),
                rs.getString("name")
        );
    }
}