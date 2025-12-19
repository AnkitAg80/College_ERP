package edu.univ.erp.domain;

// this class corresponds to course_eligibility table in erpdb
public class CourseEligibility {
    private int eligibilityId;
    private int courseId;
    private String targetBranch;
    private Integer targetYear;
    private boolean isMandatory;

    public CourseEligibility() {
    }

    public CourseEligibility(int eligibilityId, int courseId, String targetBranch, Integer targetYear,
                            boolean isMandatory) {
        this.eligibilityId = eligibilityId;
        this.courseId = courseId;
        this.targetBranch = targetBranch;
        this.targetYear = targetYear;
        this.isMandatory = isMandatory;
    }
    // constructor without eligibilityId for new records
    public CourseEligibility(int courseId, String targetBranch, Integer targetYear,
                            boolean isMandatory) {
        this.courseId = courseId;
        this.targetBranch = targetBranch;
        this.targetYear = targetYear;
        this.isMandatory = isMandatory;
    }

    public int getEligibilityId() {
        return eligibilityId;
    }

    public void setEligibilityId(int eligibilityId) {
        this.eligibilityId = eligibilityId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public Integer getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(Integer targetYear) {
        this.targetYear = targetYear;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    @Override
    public String toString() {
        return "CourseEligibility{" +
                "eligibilityId=" + eligibilityId +
                ", courseId=" + courseId +
                ", targetBranch='" + targetBranch + '\'' +
                ", targetYear=" + targetYear +
                ", isMandatory=" + isMandatory +
                '}';
    }
}
