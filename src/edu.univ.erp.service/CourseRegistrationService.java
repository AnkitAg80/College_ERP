package edu.univ.erp.service;

import edu.univ.erp.domain.*;
import edu.univ.erp.dao.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

// this class handles all course registration logic including validation for credit limits and course availability
public class CourseRegistrationService {

    private static final int baseLimit = 20;
    // using bigdecimal for precise cgpa comparison
    private static final BigDecimal cgpaCutoff = new BigDecimal("8.0");
    private static final int extraCredsMid = 2;
    private static final int extraCredsFinal = 4;

    // DAOs
    private EnrollmentDAO enrollmentDAO;
    private SectionDAO sectionDAO;
    private InstructorDAO instructorDAO;

    public CourseRegistrationService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
        this.instructorDAO = new InstructorDAO();
    }

    // this method calculates the maximum credits a student can register for based on their cgpa and semester
    public int getMaxCreditLimit(Student student) {
        if (student == null) {
            return baseLimit;
        }

        // if cgpa is null or below threshold, we return the base limit
        if (student.getCgpa() == null || student.getCgpa().compareTo(cgpaCutoff) < 0) {
            return baseLimit;
        }

        // calculating current semester from the year field
        int currentSemester = student.getYear();

        if (currentSemester >= 7 && currentSemester <= 8) {
            return baseLimit + extraCredsFinal;
        } else if (currentSemester >= 3 && currentSemester <= 6) {
            return baseLimit + extraCredsMid;
        }

        return baseLimit;
    }

    // this method calculates the total credits for all enrolled courses
    public int calculateTotalCredits(List<Course> enrolledCourses) {
        if (enrolledCourses == null) {
            return 0;
        }
        return enrolledCourses.stream()
                .mapToInt(course -> course.getCredits() != null ? course.getCredits() : 0)
                .sum();
    }

    // checks if student is already enrolled in any section of this course
    public boolean isStudentEnrolledInCourse(int studentId, int courseId) {
        try {
            List<Enrollment> studentEnrollments = enrollmentDAO.getEnrollmentsByStudent(studentId);

            for (Enrollment enrollment : studentEnrollments) {
                // only check active enrollments
                if (!"enrolled".equalsIgnoreCase(enrollment.getStatus())) {
                    continue;
                }

                Section section = sectionDAO.read(enrollment.getSectionId());
                if (section != null && section.getCourseId() == courseId) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // gets information about the students existing enrollment in this course
    public String getEnrolledSectionInfo(int studentId, int courseId) {
        try {
            List<Enrollment> studentEnrollments = enrollmentDAO.getEnrollmentsByStudent(studentId);

            for (Enrollment enrollment : studentEnrollments) {
                if (!"enrolled".equalsIgnoreCase(enrollment.getStatus())) {
                    continue;
                }

                Section section = sectionDAO.read(enrollment.getSectionId());
                if (section != null && section.getCourseId() == courseId) {
                    // get instructor name
                    String instructorName = "Unknown Professor";
                    if (section.getInstructorId() != null) {
                        try {
                            Instructor instructor = instructorDAO.getInstructorByUserId(section.getInstructorId());
                            if (instructor != null) {
                                instructorName = "Prof. " + instructor.getFullName();
                            }
                        } catch (Exception e) {
                            // ignore instructor lookup errors
                        }
                    }

                    return "Section taught by " + instructorName;
                }
            }
            return "Unknown section";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Unknown section";
        }
    }
}
