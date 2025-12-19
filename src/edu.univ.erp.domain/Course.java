package edu.univ.erp.domain;

import java.util.ArrayList;
import java.util.List;

// this class is for the Course table present in erpdb
public class Course {
    private int courseId;
    private String code;
    private String title;
    private Integer credits;

    // some extra variables that are not in the table
    private boolean mandatory;
    private List<String> eligibleBranches = new ArrayList<>();
    private List<Integer> eligibleSemesters = new ArrayList<>();

    public Course() {}

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<String> getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(List<String> eligibleBranches) {
        this.eligibleBranches = eligibleBranches;
    }

    public List<Integer> getEligibleSemesters() {
        return eligibleSemesters;
    }

    public void setEligibleSemesters(List<Integer> eligibleSemesters) {
        this.eligibleSemesters = eligibleSemesters;
    }

    public void addEligibleBranch(String branchCode) {
        this.eligibleBranches.add(branchCode);
    }

    public void addEligibleSemester(int semester) {
        this.eligibleSemesters.add(semester);
    }

    @Override
    public String toString() {
        String type = mandatory ? " (Mandatory)" : " (Elective)";
        return code + ": " + title + type;
    }
}