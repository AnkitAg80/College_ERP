package edu.univ.erp.service;

import edu.univ.erp.dao.*;
import edu.univ.erp.domain.*;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.univ.erp.data.DatabaseConnection;

import edu.univ.erp.ui.admin.AdminPanel;
import org.mindrot.jbcrypt.BCrypt;

// this class handles all administrative operations, basically all the operations that admin can perform from the admin panel
public class AdminService {

    private AdminDAO adminDAO;
    private StudentDAO studentDAO;
    private BranchDAO branchDAO;
    private CourseDAO courseDAO;
    private InstructorDAO instructorDAO;
    private SectionDAO sectionDAO;
    private CourseOfferingDAO courseOfferingDAO;
    private AnnouncementDAO announcementDAO;

    public AdminService() {
        this.adminDAO = new AdminDAO();
        this.studentDAO = new StudentDAO();
        this.branchDAO = new BranchDAO();
        this.courseDAO = new CourseDAO();
        this.instructorDAO = new InstructorDAO();
        this.sectionDAO = new SectionDAO();
        this.courseOfferingDAO = new CourseOfferingDAO();
        this.announcementDAO = new AnnouncementDAO();
    }

    // checking if the system is in maintenance mode so we can block certain operations
    private boolean isMaintenanceModeActive() {
        return adminDAO.isMaintenanceModeOn();
    }

    // this method fetches all the dashboard statistics like student count, instructor count etc for the admin panel
    public AdminPanel.DashboardStats getDashboardStats() {
        try {
            return adminDAO.getDashboardStats();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new AdminPanel.DashboardStats(0, 0, 0, 0);
        }
    }

    // this method creates a new student in both authdb and erpdb
    public String createNewStudent(String username, String password, String fullName,
                                   String rollNo, String program, String branch, int semester) {

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || rollNo.isEmpty()) {
            return "Error: Username, Password, Full Name, and Roll Number cannot be empty.";
        }
        // hashing the password before storing it in database for security
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        int newUserId = adminDAO.insertAuthUser(username, hashedPassword, "Student");

        if (newUserId == -1) {
            return "Error: Database error creating auth user. Is username unique?";
        }

        try {
            // creating the student profile in erpdb
            Student student = new Student();
            student.setUserId(newUserId);
            student.setFullName(fullName);
            student.setRollNo(rollNo);
            student.setProgram(program);
            student.setBranch(branch);
            student.setYear(semester);
            studentDAO.create(student);

            logAuditAction(null, "ADD_STUDENT", "Student", newUserId, "Added student: " + fullName);
            return "Success! Created student " + fullName + ".";
        } catch (Exception e) {
            // checking if the roll number already exists in the database
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("for key 'students.roll_no'")) {
                // deleting the orphaned auth user that we just created
                try {
                    adminDAO.deleteAuthUser(newUserId);
                }
                catch (SQLException ex) {
                    System.err.println("CRITICAL: Failed to roll back auth user after duplicate roll_no: " + ex.getMessage());
                }
                return "Error: A student with this Roll Number already exists.";
            }

            System.err.println("CRITICAL ERROR: Failed to create student profile in erpdb: " + e.getMessage());
            return "Error: Failed to create student profile.";
        }
    }

    // this method gets all sections for a specific course from the database
    public List<Section> getSectionsForCourse(int courseId) {
        try {
            return sectionDAO.getSectionsByCourse(courseId);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // this method deletes a student from both authdb and erpdb
    public String deleteStudent(int userId) {
        try {
            // we only need to delete from authdb, the ON DELETE CASCADE rule will automatically delete from erpdb.students
            if (adminDAO.deleteAuthUser(userId)) {
                logAuditAction(null, "DELETE_STUDENT", "Student", userId, "Deleted student");
                return "Success: Student deleted.";
            }
            else {
                return "Error: Student not found.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: A database error occurred.";
        }
    }

    // this method retrieves all students from the database for the admin panel to display
    public List<Student> getAllStudents() {
        try {
            return studentDAO.getAllStudents();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // this method creates a new instructor in both authdb and erpdb with validation
    public String createNewInstructor(String username, String password, String fullName, String department, String email) {
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            return "Error: Username, Password, and Full Name cannot be empty.";
        }
        // hash the password for security before storing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        int newUserId = adminDAO.insertAuthUser(username, hashedPassword, "Instructor");

        if (newUserId == -1) {
            return "Error: Database error creating auth user. Is username unique?";
        }

        try {
            // create the instructor profile in erpdb
            Instructor instructor = new Instructor();
            instructor.setUserId(newUserId);
            instructor.setFullName(fullName);
            instructor.setDepartment(department);
            instructor.setEmail(email);
            instructorDAO.create(instructor);

            logAuditAction(null, "ADD_INSTRUCTOR", "Instructor", newUserId, "Added instructor: " + fullName);
            return "Success! Created instructor " + fullName + ".";
        }
        catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to create instructor profile in erpdb: " + e.getMessage());
            return "Error: Failed to create instructor profile.";
        }
    }

    // this method deletes an instructor from both databases using cascade delete
    public String deleteInstructor(int userId) {
        try {
            if (adminDAO.deleteAuthUser(userId)) {
                logAuditAction(null, "DELETE_INSTRUCTOR", "Instructor", userId, "Deleted instructor");
                return "Success: Instructor deleted.";
            } else {
                return "Error: Instructor not found.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: A database error occurred.";
        }
    }

    // getting all instructors from the database to display in the admin panel
    public List<Instructor> getAllInstructors() {
        try {
            return instructorDAO.getAllInstructors();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // this method retrieves all branches from the database
    public List<Branch> getAllBranches() {
        try {
            return branchDAO.getAllBranches();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // creating a new branch in the database with validation for unique code and name
    public String createNewBranch(String branchCode, String branchName) {
        if (branchCode.isEmpty() || branchName.isEmpty()) {
            return "Error: Branch Code and Name are required.";
        }
        try {
            if (branchDAO.createBranch(branchCode, branchName)) {
                return "Success: Created branch " + branchCode;
            } else {
                return "Error: Failed to create branch.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            // checking if the branch name already exists
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("for key 'branches.name'")) {
                return "Error: A branch with this name already exists.";
            }
            // checking if the branch code already exists
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("for key 'branches.branch_code'")) {
                return "Error: A branch with this code already exists.";
            }
            return "Error: Database error. Is code/name unique?";
        }
    }

    // deleting a branch from the database with foreign key constraint check
    public String deleteBranch(int branchId) {
        try {
            if (branchDAO.deleteBranch(branchId)) {
                return "Success: Branch deleted.";
            } else {
                return "Error: Branch not found.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("foreign key constraint fails")) {
                return "Error: Cannot delete branch. It is likely in use by a course.";
            }
            return "Error: Database error.";
        }
    }

    // this method removes a course offering from a specific semester without affecting other semesters
    public String removeCourseFromSemester(int courseId, int semester) {
        try {
            courseDAO.deleteOffering(courseId, semester);
            logAuditAction(null, "REMOVE_OFFERING", "Course", courseId, "Removed from Semester " + semester);
            return "Success: Removed from Semester " + semester;
        }
        catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    // this method creates a new course and links it to specific semesters and branches with section capacities for instructors
    public String createNewCourse(String code, String title, int credits,
                                  List<String> targetBranches,
                                  Map<Integer, Boolean> semesterEligibility,
                                  Map<Instructor, Integer> selectedInstructors) {

        if (targetBranches.isEmpty()) return "Error: No branches selected.";
        if (semesterEligibility.isEmpty()) return "Error: No semesters selected.";

        try {
            // this creates course, offerings and sections in a single operation
            Course course = new Course();
            course.setCode(code);
            course.setTitle(title);
            course.setCredits(credits);
            int courseId = courseDAO.create(course);
            List<Branch> allBranches = branchDAO.getAllBranches();

            for (String branchCode : targetBranches) {
                int branchId = -1;
                for (Branch b : allBranches) {
                    if (b.getBranchCode().equalsIgnoreCase(branchCode)) {
                        branchId = b.getBranchId();
                        break;
                    }
                }

                if (branchId != -1) {
                    for (Map.Entry<Integer, Boolean> entry : semesterEligibility.entrySet()) {
                        int semester = entry.getKey();
                        boolean isMandatory = entry.getValue();

                        courseDAO.addOffering(courseId, semester, branchId, isMandatory);
                    }
                }
            }
            int currentYear = java.time.Year.now().getValue();
            int defaultSemester = semesterEligibility.keySet().iterator().next();

            for (Map.Entry<Instructor, Integer> entry : selectedInstructors.entrySet()) {
                Section section = new Section();
                section.setCourseId(courseId);
                section.setInstructorId(entry.getKey().getUserId());
                section.setYear(currentYear);
                section.setCapacity(entry.getValue());
                section.setSemester(defaultSemester);
                sectionDAO.create(section);
            }
            return "Success: Course created.";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public List<Course> getCoursesBySemester(int semester) {
        try {
            return courseDAO.getCoursesBySemester(semester);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // this method deletes a course completely with all its sections, enrollments, grades and assessments using cascade delete
    public String deleteCourse(int courseId) {
        try {
            Course course = courseDAO.read(courseId);
            if (course == null) {
                return "Error: Course not found.";
            }

            // getting statistics before deletion for logging purposes
            int enrollmentCount = getEnrollmentCountForCourse(courseId);
            int sectionCount = getSectionCountForCourse(courseId);

            // performing cascade delete operation
            courseDAO.delete(courseId);
            logAuditAction(null, "DELETE_COURSE", "Course", courseId,
                "Deleted course: " + course.getCode() + " (affected " + enrollmentCount + " enrollments, " + sectionCount + " sections)");
            return "Success: Deleted course " + course.getCode() +
                   "\nRemoved " + sectionCount + " section(s) and " + enrollmentCount + " enrollment(s).";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: Could not delete course. " + e.getMessage();
        }
    }

    // getting all courses from the database for deletion dialog
    public List<Course> getAllCourses() {
        try {
            return courseDAO.getAllCourses();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // this helper method counts all enrollments across all sections of a specific course
    private int getEnrollmentCountForCourse(int courseId) {
        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM enrollments e " +
                 "JOIN sections s ON e.section_id = s.section_id " +
                 "WHERE s.course_id = ?")) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // this helper method counts all sections for a specific course
    private int getSectionCountForCourse(int courseId) {
        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM sections WHERE course_id = ?")) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // this is a simple audit logging method that prints administrative actions to console
    private void logAuditAction(Integer userId, String action, String objectType,
                                int objectId, String details) {
        System.out.println("AUDIT: " + action + " - " + details);
    }

    // this method creates a new announcement that will be visible to all students
    public String createNewAnnouncement(String title, String message, int createdBy) {
        if (title == null || title.trim().isEmpty()) {
            return "Error: Title cannot be empty.";
        }
        if (message == null || message.trim().isEmpty()) {
            return "Error: Message cannot be empty.";
        }

        try {
            Announcement announcement = new Announcement(title, message, createdBy);
            int announcementId = announcementDAO.create(announcement);

            if (announcementId > 0) {
                logAuditAction(createdBy, "CREATE_ANNOUNCEMENT", "Announcement", announcementId,
                        "Created announcement: " + title);
                return "Success: Announcement created successfully.";
            }
            else {
                return "Error: Failed to create announcement.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error occurred. " + e.getMessage();
        }
    }

    // this method gets all announcements with creator names by joining the users_auth table
    public List<Announcement> getAllAnnouncements() throws SQLException {
        List<Announcement> announcements = new ArrayList<>();

        String sql = "SELECT a.announcement_id, a.title, a.message, a.created_by, a.created_at, " +
                "u.username as creator_name " +
                "FROM announcements a " +
                "JOIN authdb.users_auth u ON a.created_by = u.user_id " +
                "ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Announcement announcement = new Announcement();
                announcement.setAnnouncementId(rs.getInt("announcement_id"));
                announcement.setTitle(rs.getString("title"));
                announcement.setMessage(rs.getString("message"));
                announcement.setCreatedBy(rs.getInt("created_by"));
                announcement.setCreatedByName(rs.getString("creator_name"));
                announcement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                announcements.add(announcement);
            }
        }
        return announcements;
    }

    // this method retrieves a specific announcement by its id
    public Announcement getAnnouncementById(int announcementId) {
        try {
            return announcementDAO.getById(announcementId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // this method updates the title and message of an existing announcement
    public String updateAnnouncement(int announcementId, String title, String message) {
        if (title == null || title.trim().isEmpty()) {
            return "Error: Title cannot be empty.";
        }
        if (message == null || message.trim().isEmpty()) {
            return "Error: Message cannot be empty.";
        }

        try {
            Announcement announcement = announcementDAO.getById(announcementId);
            if (announcement == null) {
                return "Error: Announcement not found.";
            }

            announcement.setTitle(title);
            announcement.setMessage(message);

            if (announcementDAO.update(announcement)) {
                logAuditAction(announcement.getCreatedBy(), "UPDATE_ANNOUNCEMENT", "Announcement",
                        announcementId, "Updated announcement: " + title);
                return "Success: Announcement updated successfully.";
            } else {
                return "Error: Failed to update announcement.";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error occurred. " + e.getMessage();
        }
    }

    // this method deletes an announcement from the database
    public void deleteAnnouncement(int announcementId) {
        try {
            if (announcementDAO.delete(announcementId)) {
                logAuditAction(null, "DELETE_ANNOUNCEMENT", "Announcement", announcementId,
                        "Deleted announcement");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}