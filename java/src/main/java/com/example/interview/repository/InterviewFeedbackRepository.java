package com.example.interview.repository;

import com.example.interview.model.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {
    
    Optional<InterviewFeedback> findByInterviewSessionId(Long interviewSessionId);
    
    List<InterviewFeedback> findByOverallScoreGreaterThanEqual(Integer minScore);
    
    List<InterviewFeedback> findByOverallScoreLessThanEqual(Integer maxScore);
    
    @Query("SELECT AVG(f.overallScore) FROM InterviewFeedback f")
    Double getAverageOverallScore();
    
    @Query("SELECT AVG(f.technicalScore) FROM InterviewFeedback f")
    Double getAverageTechnicalScore();
    
    @Query("SELECT AVG(f.communicationScore) FROM InterviewFeedback f")
    Double getAverageCommunicationScore();
    
    @Query("SELECT AVG(f.problemSolvingScore) FROM InterviewFeedback f")
    Double getAverageProblemSolvingScore();
    
    @Query("SELECT f FROM InterviewFeedback f WHERE f.interviewSession.candidateName LIKE %:candidateName%")
    List<InterviewFeedback> findByCandidateName(@Param("candidateName") String candidateName);
}