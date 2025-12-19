package edu.univ.erp.dao;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.domain.*;
import java.sql.*;
import java.util.*;

// this class manages all database operations for course sections, it handles creating, reading, updating, and deleting sections from our system.
public class SectionDAO {

    // this method is used to add a new section in erpdb
    public void create(Section section) throws SQLException {
        String addSectionQuery = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        // making the connection
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement addSectionStatement = dbConnection.prepareStatement(addSectionQuery, Statement.RETURN_GENERATED_KEYS)) {

            addSectionStatement.setInt(1, section.getCourseId());
            addSectionStatement.setObject(2, section.getInstructorId());
            addSectionStatement.setString(3, section.getDayTime());
            addSectionStatement.setString(4, section.getRoom());
            addSectionStatement.setObject(5, section.getCapacity());
            addSectionStatement.setObject(6, section.getSemester());
            addSectionStatement.setObject(7, section.getYear());
            // executing the insert query
            addSectionStatement.executeUpdate();

            try (ResultSet generatedKeys = addSectionStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    section.setSectionId(generatedKeys.getInt(1));
                }
            }
        }
    }



    // this method is used to retrieve a specific section from the database using its id
    public Section read(int sectionId) throws SQLException {
        String getSectionQuery = "SELECT * FROM sections WHERE section_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement getSectionStatement = dbConnection.prepareStatement(getSectionQuery)) {

            getSectionStatement.setInt(1, sectionId);

            try (ResultSet sectionResults = getSectionStatement.executeQuery()) {
                if (sectionResults.next()) {
                    return convertResultSetToSection(sectionResults);
                }
            }
        }
        // no section found with this ID
        return null;
    }



    // this method is used to update an existing section in the database
    public void update(Section section) throws SQLException {
        String updateSectionQuery = "UPDATE sections SET course_id=?, instructor_id=?, day_time=?, room=?, " +
                "capacity=?, semester=?, year=? WHERE section_id=?";

        // making the connection
        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement updateStatement = dbConnection.prepareStatement(updateSectionQuery)) {

            updateStatement.setInt(1, section.getCourseId());
            updateStatement.setObject(2, section.getInstructorId());
            updateStatement.setString(3, section.getDayTime());
            updateStatement.setString(4, section.getRoom());
            updateStatement.setObject(5, section.getCapacity());
            updateStatement.setObject(6, section.getSemester());
            updateStatement.setObject(7, section.getYear());
            updateStatement.setInt(8, section.getSectionId());
            //execute the query
            updateStatement.executeUpdate();
        }
    }



    // this method is used to remove a section from the erpdb
    public void delete(int sectionId) throws SQLException {
        String deleteSectionQuery = "DELETE FROM sections WHERE section_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteSectionQuery)) {

            deleteStatement.setInt(1, sectionId);
            deleteStatement.executeUpdate();
        }
    }



    // this method is used to retrieve all sections from our erpdb
    public List<Section> getAllSections() throws SQLException {
        String getAllSectionsQuery = "SELECT * FROM sections";
        List<Section> sectionList = new ArrayList<>();

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             Statement getAllStatement = dbConnection.createStatement();
             ResultSet sectionResults = getAllStatement.executeQuery(getAllSectionsQuery)) {

            while (sectionResults.next()) {
                sectionList.add(convertResultSetToSection(sectionResults));
            }
        }
        return sectionList;
    }



    // this method is used to get all sections that students can currently register for, it includes details about the course, instructor, and available spots
    public List<Section> getAvailableSections() {
        List<Section> availableSections = new ArrayList<>();

        String getAvailableQuery = "SELECT s.section_id, s.day_time, s.room, s.capacity, " +
                "c.code, c.title, c.credits, " +
                "i.name AS instructor_name, " +
                "(s.capacity - (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id)) AS spots_available " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.user_id " +
                "WHERE s.year = ? AND s.semester = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement getAvailableStatement = dbConnection.prepareStatement(getAvailableQuery)) {

            getAvailableStatement.setInt(1, 2024);
            getAvailableStatement.setString(2, "Fall");

            try (ResultSet sectionResults = getAvailableStatement.executeQuery()) {
                while (sectionResults.next()) {
                    Section section = new Section();
                    section.setSectionId(sectionResults.getInt("section_id"));
                    section.setDayTime(sectionResults.getString("day_time"));
                    section.setRoom(sectionResults.getString("room"));
                    section.setCapacity(sectionResults.getInt("capacity"));

                    // add the course details
                    section.setCourseCode(sectionResults.getString("code"));
                    section.setCourseTitle(sectionResults.getString("title"));
                    section.setCredits(sectionResults.getInt("credits"));
                    section.setInstructorName(sectionResults.getString("instructor_name"));
                    section.setSpotsAvailable(sectionResults.getInt("spots_available"));

                    availableSections.add(section);
                }
            }
        } catch (SQLException dbError) {
            System.err.println("Oops! Something went wrong while getting available sections: " + dbError.getMessage());
            dbError.printStackTrace();
        }
        return availableSections;
    }



    // this method is used to retrieve all sections for a specific course
    public List<Section> getSectionsByCourse(int courseId) throws SQLException {
        List<Section> courseSections = new ArrayList<>();
        String getCourseSectionsQuery = "SELECT * FROM sections WHERE course_id = ?";

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement getCourseSectionsStatement = dbConnection.prepareStatement(getCourseSectionsQuery)) {

            getCourseSectionsStatement.setInt(1, courseId);
            ResultSet sectionResults = getCourseSectionsStatement.executeQuery();

            while (sectionResults.next()) {
                Section section = new Section();
                section.setSectionId(sectionResults.getInt("section_id"));
                section.setCourseId(sectionResults.getInt("course_id"));
                section.setInstructorId(sectionResults.getInt("instructor_id"));
                section.setDayTime(sectionResults.getString("day_time"));
                section.setRoom(sectionResults.getString("room"));
                section.setCapacity(sectionResults.getInt("capacity"));
                section.setSemester(sectionResults.getInt("semester"));
                section.setYear(sectionResults.getInt("year"));
                courseSections.add(section);
            }
        }

        return courseSections;
    }



    // this method is used to get all sections assigned to a specific instructor, this is called in the instructor panel
    // this helps the instructor see all the courses they are teaching.
    // using GROUP_CONCAT to combine all eligible semesters into a single field
    public List<Section> getSectionsByInstructor(int instructorId) throws SQLException {
        String getInstructorSectionsQuery = "SELECT s.section_id, s.course_id, s.instructor_id, s.day_time, s.room, s.capacity, s.year, " +
                "c.code, c.title, c.credits, " +
                "GROUP_CONCAT(DISTINCT co.semester ORDER BY co.semester) AS eligible_semesters, " +
                "COUNT(e.enrollment_id) AS enrolled_count " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN course_offerings co ON c.course_id = co.course_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE s.instructor_id = ? " +
                "GROUP BY s.section_id, c.course_id, c.code, c.title, c.credits";

        List<Section> instructorSections = new ArrayList<>();

        try (Connection dbConnection = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement getInstructorSectionsStatement = dbConnection.prepareStatement(getInstructorSectionsQuery)) {

            getInstructorSectionsStatement.setInt(1, instructorId);

            try (ResultSet sectionResults = getInstructorSectionsStatement.executeQuery()) {
                while (sectionResults.next()) {
                    Section section = new Section();
                    section.setSectionId(sectionResults.getInt("section_id"));
                    section.setCourseId(sectionResults.getInt("course_id"));
                    section.setInstructorId(sectionResults.getInt("instructor_id"));
                    section.setDayTime(sectionResults.getString("day_time"));
                    section.setRoom(sectionResults.getString("room"));
                    section.setCapacity(sectionResults.getInt("capacity"));
                    section.setYear(sectionResults.getInt("year"));
                    section.setCourseCode(sectionResults.getString("code"));
                    section.setCourseTitle(sectionResults.getString("title"));
                    section.setCredits(sectionResults.getInt("credits"));

                    instructorSections.add(section);
                }
            }
        }
        return instructorSections;
    }



    // this method is used to convert a database result set row into a Section object
    private Section convertResultSetToSection(ResultSet sectionData) throws SQLException {
        Section section = new Section();
        section.setSectionId(sectionData.getInt("section_id"));
        section.setCourseId(sectionData.getInt("course_id"));
        section.setInstructorId((Integer) sectionData.getObject("instructor_id"));
        section.setDayTime(sectionData.getString("day_time"));
        section.setRoom(sectionData.getString("room"));
        section.setCapacity((Integer) sectionData.getObject("capacity"));
        section.setSemester((Integer) sectionData.getObject("semester"));
        section.setYear((Integer) sectionData.getObject("year"));
        return section;
    }
}