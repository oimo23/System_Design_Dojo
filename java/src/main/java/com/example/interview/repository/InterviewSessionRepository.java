package com.example.interview.repository;

import com.example.interview.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    
    List<InterviewSession> findByStatus(InterviewSession.InterviewStatus status);
    
    List<InterviewSession> findByCandidateNameContainingIgnoreCase(String candidateName);
    
    List<InterviewSession> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT i FROM InterviewSession i WHERE i.status = :status AND i.createdAt >= :since")
    List<InterviewSession> findActiveSessionsSince(@Param("status") InterviewSession.InterviewStatus status, 
                                                  @Param("since") LocalDateTime since);
}