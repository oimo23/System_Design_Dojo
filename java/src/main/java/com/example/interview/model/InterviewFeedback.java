package com.example.interview.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_feedback")
public class InterviewFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_session_id", nullable = false)
    @JsonBackReference
    private InterviewSession interviewSession;
    
    @Column(name = "overall_score")
    @Min(value = 1, message = "Overall score must be at least 1")
    @Max(value = 10, message = "Overall score must be at most 10")
    private Integer overallScore;
    
    @Column(name = "technical_score")
    @Min(value = 1, message = "Technical score must be at least 1")
    @Max(value = 10, message = "Technical score must be at most 10")
    private Integer technicalScore;
    
    @Column(name = "communication_score")
    @Min(value = 1, message = "Communication score must be at least 1")
    @Max(value = 10, message = "Communication score must be at most 10")
    private Integer communicationScore;
    
    @Column(name = "problem_solving_score")
    @Min(value = 1, message = "Problem solving score must be at least 1")
    @Max(value = 10, message = "Problem solving score must be at most 10")
    private Integer problemSolvingScore;
    
    @Lob
    @Column(name = "strengths")
    private String strengths;
    
    @Lob
    @Column(name = "areas_for_improvement")
    private String areasForImprovement;
    
    @Lob
    @Column(name = "detailed_feedback")
    private String detailedFeedback;
    
    @Lob
    @Column(name = "recommendations")
    private String recommendations;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public InterviewSession getInterviewSession() {
        return interviewSession;
    }
    
    public void setInterviewSession(InterviewSession interviewSession) {
        this.interviewSession = interviewSession;
    }
    
    public Integer getOverallScore() {
        return overallScore;
    }
    
    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }
    
    public Integer getTechnicalScore() {
        return technicalScore;
    }
    
    public void setTechnicalScore(Integer technicalScore) {
        this.technicalScore = technicalScore;
    }
    
    public Integer getCommunicationScore() {
        return communicationScore;
    }
    
    public void setCommunicationScore(Integer communicationScore) {
        this.communicationScore = communicationScore;
    }
    
    public Integer getProblemSolvingScore() {
        return problemSolvingScore;
    }
    
    public void setProblemSolvingScore(Integer problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }
    
    public String getStrengths() {
        return strengths;
    }
    
    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }
    
    public String getAreasForImprovement() {
        return areasForImprovement;
    }
    
    public void setAreasForImprovement(String areasForImprovement) {
        this.areasForImprovement = areasForImprovement;
    }
    
    public String getDetailedFeedback() {
        return detailedFeedback;
    }
    
    public void setDetailedFeedback(String detailedFeedback) {
        this.detailedFeedback = detailedFeedback;
    }
    
    public String getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}