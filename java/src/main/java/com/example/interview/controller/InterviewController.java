package com.example.interview.controller;

import com.example.interview.model.InterviewFeedback;
import com.example.interview.model.InterviewMessage;
import com.example.interview.model.InterviewSession;
import com.example.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InterviewController {
    
    @Autowired
    private InterviewService interviewService;
    
    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        try {
            InterviewSession session = interviewService.startInterview(
                    request.getCandidateName(), 
                    request.getTopic()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{sessionId}/respond")
    public ResponseEntity<InterviewMessage> respondToCandidate(
            @PathVariable Long sessionId,
            @Valid @RequestBody CandidateResponseRequest request) {
        try {
            InterviewMessage response = interviewService.respondToCandidate(sessionId, request.getResponse());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<InterviewSessionResponse> getInterviewSession(@PathVariable Long sessionId) {
        try {
            InterviewSession session = interviewService.getInterviewSession(sessionId);
            List<InterviewMessage> messages = interviewService.getInterviewMessages(sessionId);
            
            InterviewSessionResponse response = new InterviewSessionResponse();
            response.setSession(session);
            response.setMessages(messages);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<InterviewMessage> finishInterview(@PathVariable Long sessionId) {
        try {
            InterviewMessage finalMessage = interviewService.finishInterview(sessionId);
            return ResponseEntity.ok(finalMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{sessionId}/feedback")
    public ResponseEntity<InterviewFeedback> getFeedback(@PathVariable Long sessionId) {
        try {
            InterviewFeedback feedback = interviewService.getFeedback(sessionId);
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<InterviewMessage>> getInterviewMessages(@PathVariable Long sessionId) {
        try {
            List<InterviewMessage> messages = interviewService.getInterviewMessages(sessionId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DTO classes
    public static class StartInterviewRequest {
        private String candidateName;
        private String topic;
        
        public String getCandidateName() {
            return candidateName;
        }
        
        public void setCandidateName(String candidateName) {
            this.candidateName = candidateName;
        }
        
        public String getTopic() {
            return topic;
        }
        
        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
    
    public static class CandidateResponseRequest {
        private String response;
        
        public String getResponse() {
            return response;
        }
        
        public void setResponse(String response) {
            this.response = response;
        }
    }
    
    public static class InterviewSessionResponse {
        private InterviewSession session;
        private List<InterviewMessage> messages;
        
        public InterviewSession getSession() {
            return session;
        }
        
        public void setSession(InterviewSession session) {
            this.session = session;
        }
        
        public List<InterviewMessage> getMessages() {
            return messages;
        }
        
        public void setMessages(List<InterviewMessage> messages) {
            this.messages = messages;
        }
    }
}