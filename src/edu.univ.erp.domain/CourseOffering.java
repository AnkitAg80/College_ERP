package edu.univ.erp.domain;

//corresponds to the course_offerings table in erpdb
public class CourseOffering {
    private int offeringId;
    private int branchId;
    private int semester;
    private int courseId;

    public CourseOffering() {
    }

    public CourseOffering(int offeringId, int branchId, int semester, int courseId) {
        this.offeringId = offeringId;
        this.branchId = branchId;
        this.semester = semester;
        this.courseId = courseId;
    }

    public CourseOffering(int branchId, int semester, int courseId) {
        this.branchId = branchId;
        this.semester = semester;
        this.courseId = courseId;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "CourseOffering{" +
                "offeringId=" + offeringId +
                ", branchId=" + branchId +
                ", semester=" + semester +
                ", courseId=" + courseId +
                '}';
    }
}
