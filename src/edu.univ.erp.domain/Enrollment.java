package edu.univ.erp.domain;

//Corresponds to the 'enrollments' table in erpdb
public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private String status;
    private String finalGrade;  // Letter grade: A+, A, A-, B, B-, C, C-, D, F
    private Double cgpa;        // Course GPA based on grade

    public Enrollment() {
        this.status = "enrolled";
    }

    public Enrollment(int enrollmentId, int studentId, int sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public Enrollment(int studentId, int sectionId, String status) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public Enrollment(int studentId, int sectionId) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = "enrolled";
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", status='" + status + '\'' +
                '}';
    }
}
