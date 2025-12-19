package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class manages all db operations for our course entities, it handles creating, reading, updating, and deleting courses from the system.
public class CourseDAO {

    // this method is used to add a new course to our erpdb
    public int create(Course course) throws SQLException {
        String addCourseQuery = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        // make the connection with erpdb
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement addCourseStatement = dbConnection.prepareStatement(addCourseQuery, Statement.RETURN_GENERATED_KEYS)) {

            addCourseStatement.setString(1, course.getCode());
            addCourseStatement.setString(2, course.getTitle());
            addCourseStatement.setObject(3, course.getCredits());
            // execute the query
            int rowsAdded = addCourseStatement.executeUpdate();

            if (rowsAdded > 0) {
                try (ResultSet generatedKeys = addCourseStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newCourseId = generatedKeys.getInt(1);
                        course.setCourseId(newCourseId);
                        return newCourseId;
                    } else {
                        throw new SQLException("Something went wrong while creating the course - no ID was returned.");
                    }
                }
            }
            return -1; // an error occurred
        }
    }



    // this method is used to retrieve a specific course from the database using its id
    public Course read(int courseId) throws SQLException {
        String getCourseQuery = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement getCourseStatement = dbConnection.prepareStatement(getCourseQuery)) {

            getCourseStatement.setInt(1, courseId);

            try (ResultSet courseResults = getCourseStatement.executeQuery()) {
                if (courseResults.next()) {
                    return convertResultSetToCourse(courseResults);
                }
            }
        }
        return null; // no course found with this ID
    }



    // this method is used to get all courses offered in a specific semester
    public List<Course> getCoursesBySemester(int semester) throws SQLException {
        String semesterCoursesQuery = "SELECT DISTINCT c.course_id, c.code, c.title, c.credits, " +
                "MAX(co.is_mandatory) as is_mandatory, " +
                "GROUP_CONCAT(DISTINCT b.branch_code ORDER BY b.branch_code) AS branches, " +
                "GROUP_CONCAT(DISTINCT co.semester ORDER BY co.semester) AS semesters " +
                "FROM courses c " +
                "JOIN course_offerings co ON c.course_id = co.course_id AND co.semester = ? " +
                "LEFT JOIN branches b ON co.branch_id = b.branch_id " +
                "GROUP BY c.course_id " +
                "ORDER BY c.code";

        List<Course> semesterCourses = new ArrayList<>();
        // connecting with erpdb
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement semesterStatement = dbConnection.prepareStatement(semesterCoursesQuery)) {

            semesterStatement.setInt(1, semester);
            // execute the query
            try (ResultSet courseResults = semesterStatement.executeQuery()) {
                while (courseResults.next()) {
                    semesterCourses.add(convertResultSetToFullCourse(courseResults));
                }
            }
        }
        return semesterCourses;
    }



    // this method is used to retrieve all courses from our database
    public List<Course> getAllCourses() throws SQLException {
        // we joined with branches to get the branch codes for each course
        String allCoursesQuery = "SELECT DISTINCT c.course_id, c.code, c.title, c.credits, " +
                "GROUP_CONCAT(DISTINCT b.branch_code ORDER BY b.branch_code) AS branches, " +
                "GROUP_CONCAT(DISTINCT co.semester ORDER BY co.semester) AS semesters " +
                "FROM courses c " +
                "LEFT JOIN course_offerings co ON c.course_id = co.course_id " +
                "LEFT JOIN branches b ON co.branch_id = b.branch_id " +
                "GROUP BY c.course_id " +
                "ORDER BY c.code";

        List<Course> allCourses = new ArrayList<>();
        // make the connection with erpdb
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             Statement allCoursesStatement = dbConnection.createStatement();
             // execute the query
             ResultSet courseResults = allCoursesStatement.executeQuery(allCoursesQuery)) {

            while (courseResults.next()) {
                //adding all the courses to the arraylist
                allCourses.add(convertResultSetToFullCourse(courseResults));
            }
        }
        return allCourses;
    }



    // this method is used to completely remove a course and all its related data from the database
    // this is a cascade delete - it will remove sections, enrollments, grades, etc.
    public void delete(int courseId) throws SQLException {
        Connection dbConnection = null;
        try {
            // make the connection with erpdb
            dbConnection = DatabaseConnection.getInstance().getErpConnection();
            dbConnection.setAutoCommit(false);

            List<Integer> sectionIds = new ArrayList<>();
            // getting all the sections of this course.
            try (PreparedStatement getSectionsStatement = dbConnection.prepareStatement("SELECT section_id FROM sections WHERE course_id = ?")) {
                getSectionsStatement.setInt(1, courseId);
                try (ResultSet sectionResults = getSectionsStatement.executeQuery()) {
                    while (sectionResults.next()) {
                        sectionIds.add(sectionResults.getInt("section_id"));
                    }
                }
            }

            // this part clears the grades, assessments, and enrollments for each section of the course.
            for (int sectionId : sectionIds) {
                List<Integer> enrollmentIds = new ArrayList<>();
                try (PreparedStatement getEnrollmentsStatement = dbConnection.prepareStatement("SELECT enrollment_id FROM enrollments WHERE section_id = ?")) {
                    getEnrollmentsStatement.setInt(1, sectionId);
                    try (ResultSet enrollmentResults = getEnrollmentsStatement.executeQuery()) {
                        while (enrollmentResults.next()) {
                            enrollmentIds.add(enrollmentResults.getInt("enrollment_id"));
                        }
                    }
                }
                for (int enrollmentId : enrollmentIds) {
                    try (PreparedStatement deleteGradesStatement = dbConnection.prepareStatement("DELETE FROM grades WHERE enrollment_id = ?")) {
                        deleteGradesStatement.setInt(1, enrollmentId);
                        deleteGradesStatement.executeUpdate();
                    }
                }
                try (PreparedStatement deleteEnrollmentsStatement = dbConnection.prepareStatement("DELETE FROM enrollments WHERE section_id = ?")) {
                    deleteEnrollmentsStatement.setInt(1, sectionId);
                    deleteEnrollmentsStatement.executeUpdate();
                }

                try (PreparedStatement deleteAssessmentsStatement = dbConnection.prepareStatement("DELETE FROM assessments WHERE section_id = ?")) {
                    deleteAssessmentsStatement.setInt(1, sectionId);
                    deleteAssessmentsStatement.executeUpdate();
                }
            }
            try (PreparedStatement deleteSectionsStatement = dbConnection.prepareStatement("DELETE FROM sections WHERE course_id = ?")) {
                deleteSectionsStatement.setInt(1, courseId);
                deleteSectionsStatement.executeUpdate();
            }

            try (PreparedStatement deleteEligibilityStatement = dbConnection.prepareStatement("DELETE FROM course_eligibility WHERE course_id = ?")) {
                deleteEligibilityStatement.setInt(1, courseId);
                deleteEligibilityStatement.executeUpdate();
            } catch (SQLException ignored) {
            }

            try (PreparedStatement deleteOfferingsStatement = dbConnection.prepareStatement("DELETE FROM course_offerings WHERE course_id = ?")) {
                deleteOfferingsStatement.setInt(1, courseId);
                deleteOfferingsStatement.executeUpdate();
            }

            try (PreparedStatement deleteAnnouncementsStatement = dbConnection.prepareStatement("DELETE FROM announcements WHERE title LIKE ?")) {
                deleteAnnouncementsStatement.setString(1, "%" + courseId + "%");
                deleteAnnouncementsStatement.executeUpdate();
            } catch (SQLException ignored) {
            }

            // deleting the course
            try (PreparedStatement deleteCourseStatement = dbConnection.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
                deleteCourseStatement.setInt(1, courseId);
                deleteCourseStatement.executeUpdate();
            }

            dbConnection.commit();
        }
        catch (SQLException e) {
            if (dbConnection != null) {
                try {
                    dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        }
        finally {
            // this is basically the cleanup part where the connection is closed and auto-commit is reset
            if (dbConnection != null) {
                try {
                    dbConnection.setAutoCommit(true);
                    dbConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    // this method is used to remove a specific course offering for a particular semester
    public void deleteOffering(int courseId, int semester) throws SQLException {
        String removeOfferingQuery = "DELETE FROM course_offerings WHERE course_id = ? AND semester = ?";
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement removeOfferingStatement = dbConnection.prepareStatement(removeOfferingQuery)) {

            removeOfferingStatement.setInt(1, courseId);
            removeOfferingStatement.setInt(2, semester);
            removeOfferingStatement.executeUpdate();
        }
    }



    // this method is used to add a new course offering for a specific semester and branch
    public void addOffering(int courseId, int semester, int branchId, boolean isMandatory) throws SQLException {
        String addOfferingQuery = "INSERT INTO course_offerings (course_id, semester, branch_id, is_mandatory) VALUES (?, ?, ?, ?)";
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement addOfferingStatement = dbConnection.prepareStatement(addOfferingQuery)) {

            addOfferingStatement.setInt(1, courseId);
            addOfferingStatement.setInt(2, semester);
            addOfferingStatement.setInt(3, branchId);
            addOfferingStatement.setBoolean(4, isMandatory);

            try {
                addOfferingStatement.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                // it's okay if this is a duplicate - we'll just ignore it
            }
        }
    }



    // this method is used to convert a basic ResultSet row to a Course object
    private Course convertResultSetToCourse(ResultSet courseData) throws SQLException {
        Course newCourse = new Course();
        newCourse.setCourseId(courseData.getInt("course_id"));
        newCourse.setCode(courseData.getString("code"));
        newCourse.setTitle(courseData.getString("title"));
        newCourse.setCredits((Integer) courseData.getObject("credits"));
        return newCourse;
    }



    // this method is used to convert a full ResultSet row to a Course object with all details
    private Course convertResultSetToFullCourse(ResultSet courseData) throws SQLException {
        Course newCourse = new Course();
        newCourse.setCourseId(courseData.getInt("course_id"));
        newCourse.setCode(courseData.getString("code"));
        newCourse.setTitle(courseData.getString("title"));
        newCourse.setCredits((Integer) courseData.getObject("credits"));

        try {
            newCourse.setMandatory(courseData.getBoolean("is_mandatory"));
        } catch (SQLException e) {
            // by defualt if the mandatory field is not selected, then it is set as false
            newCourse.setMandatory(false);
        }

        // select the branches that the course is available in
        String branchesString = courseData.getString("branches");
        if (branchesString != null && !branchesString.isEmpty()) {
            for (String branch : branchesString.split(",")) {
                newCourse.addEligibleBranch(branch.trim());
            }
        }

        // select the semesters that the course is available in
        String semestersString = courseData.getString("semesters");
        if (semestersString != null && !semestersString.isEmpty()) {
            for (String sem : semestersString.split(",")) {
                newCourse.addEligibleSemester(Integer.parseInt(sem.trim()));
            }
        }
        return newCourse;
    }
}