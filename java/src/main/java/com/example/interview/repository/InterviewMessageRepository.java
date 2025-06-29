package com.example.interview.repository;

import com.example.interview.model.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {
    
    List<InterviewMessage> findByInterviewSessionIdOrderByCreatedAtAsc(Long interviewSessionId);
    
    List<InterviewMessage> findByInterviewSessionIdAndRole(Long interviewSessionId, InterviewMessage.MessageRole role);
    
    @Query("SELECT COUNT(m) FROM InterviewMessage m WHERE m.interviewSession.id = :sessionId AND m.role = :role")
    Long countByInterviewSessionIdAndRole(@Param("sessionId") Long sessionId, @Param("role") InterviewMessage.MessageRole role);
    
    @Query("SELECT m FROM InterviewMessage m WHERE m.interviewSession.id = :sessionId ORDER BY m.createdAt DESC")
    List<InterviewMessage> findLatestMessagesBySessionId(@Param("sessionId") Long sessionId);
}