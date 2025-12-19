package edu.univ.erp.util;

import edu.univ.erp.domain.*;
import edu.univ.erp.dao.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class TranscriptGenerator {
    // using opencsv library for CSV generation of the student transcript.
    public static File generateCSVTranscript(Student student) throws SQLException, IOException {
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        SectionDAO sectionDAO = new SectionDAO();
        CourseDAO courseDAO = new CourseDAO();

        // Get all enrollments
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(student.getUserId());

        // Create file
        File file = new File("Transcript_" + student.getRollNo() + ".csv");
        PrintWriter writer = new PrintWriter(new FileWriter(file));

        // Write header
        writer.println("OFFICIAL TRANSCRIPT");
        writer.println("Student Name:," + student.getFullName());
        writer.println("Roll Number:," + student.getRollNo());
        writer.println("Program:," + student.getProgram());
        writer.println("Branch:," + student.getBranch());
        writer.println("CGPA:," + (student.getCgpa() != null ? student.getCgpa() : "N/A"));
        writer.println();
        writer.println("Course Code,Course Title,Credits,Grade,Grade Points");
        writer.println("=============================================================");

        double totalGradePoints = 0.0;
        int totalCredits = 0;

        // Write course data
        for (Enrollment e : enrollments) {
            if (e.getFinalGrade() == null || e.getFinalGrade().isEmpty()) {
                continue; // Skip in-progress courses
            }

            Section section = sectionDAO.read(e.getSectionId());
            if (section == null) continue;

            Course course = courseDAO.read(section.getCourseId());
            if (course == null) continue;

            double gradePoint = e.getCgpa() != null ? e.getCgpa() : 0.0;
            int credits = course.getCredits();

            writer.println(String.format("%s,%s,%d,%s,%.1f",
                    course.getCode(),
                    course.getTitle(),
                    credits,
                    e.getFinalGrade(),
                    gradePoint));

            totalGradePoints += (gradePoint * credits);
            totalCredits += credits;
        }

        writer.println();
        writer.println("Total Credits Completed:," + totalCredits);
        writer.println("Cumulative GPA:," + String.format("%.2f",
                totalCredits > 0 ? totalGradePoints / totalCredits : 0.0));

        writer.close();
        return file;
    }
}