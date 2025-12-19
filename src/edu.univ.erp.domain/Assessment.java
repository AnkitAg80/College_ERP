package edu.univ.erp.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

 //Corresponds to the 'assessments' table in erpdb
public class Assessment {
    private int assessmentId;
    private int sectionId;
    private String name;
    private BigDecimal maxScore;
    private BigDecimal weight;
    private LocalDateTime createdAt;

    // default constructor
    public Assessment() {
    }

    // constructor with all fields
    public Assessment(int assessmentId, int sectionId, String name, BigDecimal maxScore,
                      BigDecimal weight, LocalDateTime createdAt) {
        this.assessmentId = assessmentId;
        this.sectionId = sectionId;
        this.name = name;
        this.maxScore = maxScore;
        this.weight = weight;
        this.createdAt = createdAt;
    }

    // constructor that is used for creating new assessment before id is known
    public Assessment(int sectionId, String name, BigDecimal maxScore, BigDecimal weight) {
        this.sectionId = sectionId;
        this.name = name;
        this.maxScore = maxScore;
        this.weight = weight;
        this.createdAt = LocalDateTime.now();
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessmentId=" + assessmentId +
                ", sectionId=" + sectionId +
                ", name='" + name + '\'' +
                ", maxScore=" + maxScore +
                ", weight=" + weight +
                ", createdAt=" + createdAt +
                '}';
    }
}
