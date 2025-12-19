package edu.univ.erp.domain;

import java.math.BigDecimal;

//Corresponds to the grades table in erpdb
public class Grade {
    private int gradeId;
    private int enrollmentId;
    private int assessmentId;
    private BigDecimal score;

    public Grade() {
    }

    public Grade(int enrollmentId, int assessmentId, BigDecimal score) {
        this.enrollmentId = enrollmentId;
        this.assessmentId = assessmentId;
        this.score = score;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", assessmentId=" + assessmentId +
                ", score=" + score +
                '}';
    }
}