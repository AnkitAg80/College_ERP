package edu.univ.erp.domain;

import java.math.BigDecimal;

//corresponds to the students table in erpdb
public class Student {
    private int userId;
    private String name;
    private String rollNo;
    private String fullName;
    private String program;
    private String branch;
    private Integer yearOfAdmission;
    private BigDecimal cgpa;
    private Integer year;
    private String email;


    public Student() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getFullName() {
        if (fullName == null && name != null) {
            return name;
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getYearOfAdmission() {
        return yearOfAdmission;
    }

    public void setYearOfAdmission(Integer yearOfAdmission) {
        this.yearOfAdmission = yearOfAdmission;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

     //to calculate current semester based on year of admission and current date
    public int calculateCurrentSemester() {
        if (yearOfAdmission == null) {
            // Fallback if year is not set
            if (this.year != null) {
                return (this.year * 2) - 1;
            }
            return 1;
        }
        int currentYear = java.time.Year.now().getValue();
        int yearsDiff = currentYear - yearOfAdmission;
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int semOffset = (currentMonth >= 1 && currentMonth <= 5) ? 1 : 2;//monsoon sem is odd sem and winter sem is even sem
        return (yearsDiff * 2) + semOffset;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId=" + userId +
                ", fullName='" + getFullName() + '\'' +
                ", rollNo='" + rollNo + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}