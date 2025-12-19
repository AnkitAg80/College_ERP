package edu.univ.erp.domain;

//corresponds to the section_enrollment_counts table in erpdb
public class SectionEnrollmentCount {
    private int sectionId;
    private Integer capacity;
    private long enrolledCount;

    public SectionEnrollmentCount() {
    }

    public SectionEnrollmentCount(int sectionId, Integer capacity, long enrolledCount) {
        this.sectionId = sectionId;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
    }

    public SectionEnrollmentCount(int sectionId, long enrolledCount) {
        this.sectionId = sectionId;
        this.enrolledCount = enrolledCount;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    @Override
    public String toString() {
        return "SectionEnrollmentCount{" +
                "sectionId=" + sectionId +
                ", capacity=" + capacity +
                ", enrolledCount=" + enrolledCount +
                '}';
    }
}
