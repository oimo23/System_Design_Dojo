package com.example.interview.service;

import com.example.interview.model.*;
import com.example.interview.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class InterviewService {
    
    @Autowired
    private InterviewSessionRepository interviewSessionRepository;
    
    @Autowired
    private InterviewMessageRepository interviewMessageRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private InterviewFeedbackRepository interviewFeedbackRepository;
    
    @Autowired
    private BedrockService bedrockService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public InterviewSession startInterview(String candidateName, String topic) {
        InterviewSession session = new InterviewSession();
        session.setCandidateName(candidateName);
        session.setTopic(topic);
        session.setStatus(InterviewSession.InterviewStatus.ACTIVE);
        
        InterviewSession savedSession = interviewSessionRepository.save(session);
        
        // Generate initial question
        String initialQuestion = generateInitialQuestion(topic);
        
        InterviewMessage initialMessage = new InterviewMessage();
        initialMessage.setInterviewSession(savedSession);
        initialMessage.setRole(InterviewMessage.MessageRole.INTERVIEWER);
        initialMessage.setContent(initialQuestion);
        
        interviewMessageRepository.save(initialMessage);
        
        return savedSession;
    }
    
    public InterviewMessage respondToCandidate(Long sessionId, String candidateResponse) {
        Optional<InterviewSession> sessionOpt = interviewSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Interview session not found");
        }
        
        InterviewSession session = sessionOpt.get();
        if (session.getStatus() != InterviewSession.InterviewStatus.ACTIVE) {
            throw new RuntimeException("Interview session is not active");
        }
        
        // Save candidate response
        InterviewMessage candidateMessage = new InterviewMessage();
        candidateMessage.setInterviewSession(session);
        candidateMessage.setRole(InterviewMessage.MessageRole.CANDIDATE);
        candidateMessage.setContent(candidateResponse);
        interviewMessageRepository.save(candidateMessage);
        
        // Get conversation history
        List<InterviewMessage> messages = interviewMessageRepository.findByInterviewSessionIdOrderByCreatedAtAsc(sessionId);
        String conversationHistory = buildConversationHistory(messages);
        
        // Check if interview should end (e.g., after 10 exchanges)
        long candidateMessageCount = messages.stream()
                .filter(m -> m.getRole() == InterviewMessage.MessageRole.CANDIDATE)
                .count();
        
        if (candidateMessageCount >= 5) {
            // End interview and generate feedback
            return finishInterview(sessionId);
        }
        
        // Generate interviewer response with diagram evolution
        String questionContext = getQuestionContext(session.getTopic());
        String currentDiagramState = getCurrentDiagramState(messages);
        String interviewerResponse = bedrockService.generateInterviewerResponseWithDiagram(
                conversationHistory, candidateResponse, questionContext, currentDiagramState);
        
        InterviewMessage interviewerMessage = new InterviewMessage();
        interviewerMessage.setInterviewSession(session);
        interviewerMessage.setRole(InterviewMessage.MessageRole.INTERVIEWER);
        interviewerMessage.setContent(interviewerResponse);
        
        return interviewMessageRepository.save(interviewerMessage);
    }
    
    public InterviewMessage finishInterview(Long sessionId) {
        Optional<InterviewSession> sessionOpt = interviewSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Interview session not found");
        }
        
        InterviewSession session = sessionOpt.get();
        session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        interviewSessionRepository.save(session);
        
        // Generate feedback
        generateFeedback(sessionId);
        
        InterviewMessage finalMessage = new InterviewMessage();
        finalMessage.setInterviewSession(session);
        finalMessage.setRole(InterviewMessage.MessageRole.INTERVIEWER);
        finalMessage.setContent("面接を終了します。フィードバックをご確認ください。お疲れ様でした。");
        
        return interviewMessageRepository.save(finalMessage);
    }
    
    public InterviewFeedback getFeedback(Long sessionId) {
        return interviewFeedbackRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Feedback not found for session"));
    }
    
    public InterviewSession getInterviewSession(Long sessionId) {
        return interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));
    }
    
    public List<InterviewMessage> getInterviewMessages(Long sessionId) {
        return interviewMessageRepository.findByInterviewSessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    private String generateInitialQuestion(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            topic = "大規模なWebアプリケーション";
        }
        
        // Generate specific requirements based on topic
        String baseRequirements = getBaseRequirements(topic);
        String detailedRequirements = bedrockService.generateDetailedRequirements(topic, baseRequirements);
        
        // Initial simple AWS architecture diagram
        String initialDiagram = """
            ```mermaid
            graph TD
                A[Frontend<br/>React/Vue.js] --> B[Backend<br/>ECS/EC2]
                
                style A fill:#e1f5fe
                style B fill:#f3e5f5
            ```
            """;
        
        return String.format("""
            こんにちは。「%s」のシステム設計について議論していきます。
            
            %s
            
            まずは、最もシンプルなAWS構成から始めましょう：
            
            %s
            
            この基本的な構成から、どのように拡張・改善していけばよいでしょうか？まず、システム全体のアーキテクチャについて教えてください。主要コンポーネントとその責務はどのように設計しますか？
            """, topic, detailedRequirements, initialDiagram);
    }
    
    private String getBaseRequirements(String topic) {
        return switch (topic.toLowerCase()) {
            case "チャットアプリケーション", "チャットアプリ", "chat" -> """
                基本要件:
                - ユーザー認証
                - リアルタイムメッセージング
                - メッセージ履歴保存
                
                スケール要件:
                - 同時接続ユーザー数: 10万人
                - 1日のメッセージ数: 1億件
                - レスポンス時間: 100ms以下
                - 可用性: 99.9%""";
            
            case "eコマース", "ecサイト", "オンラインショップ", "ecommerce" -> """
                基本要件:
                - 商品検索・表示
                - ショッピングカート
                - 決済処理
                
                スケール要件:
                - 月間アクティブユーザー: 500万人
                - 商品数: 100万点
                - ピーク時同時接続: 5万人
                - 1日の注文数: 10万件
                - 可用性: 99.95%""";
            
            case "動画配信", "動画サイト", "video streaming", "youtube" -> """
                基本要件:
                - 動画アップロード・再生
                - 動画検索
                - ユーザー管理
                
                スケール要件:
                - 月間アクティブユーザー: 1億人
                - 1日の動画アップロード: 50万本
                - 同時視聴者数: 100万人
                - ストレージ: 10PB
                - 可用性: 99.99%""";
            
            case "sns", "ソーシャルネットワーク", "twitter", "facebook" -> """
                基本要件:
                - 投稿・タイムライン
                - フォロー機能
                - 通知システム
                
                スケール要件:
                - 月間アクティブユーザー: 3億人
                - 1日の投稿数: 5億件
                - 1日のタイムライン表示: 100億回
                - レスポンス時間: 200ms以下
                - 可用性: 99.9%""";
            
            case "配車サービス", "uber", "タクシーアプリ", "ride sharing" -> """
                基本要件:
                - ユーザー・ドライバーマッチング
                - リアルタイム位置追跡
                - 決済処理
                
                スケール要件:
                - 登録ユーザー数: 1億人
                - アクティブドライバー: 500万人
                - 1日のライド数: 1500万件
                - ピーク時同時リクエスト: 10万件
                - 可用性: 99.95%""";
            
            default -> String.format("""
                基本要件:
                - ユーザー認証
                - コア機能
                - データ管理
                
                スケール要件:
                - 月間アクティブユーザー: 100万人
                - 1日のリクエスト数: 1000万件
                - レスポンス時間: 300ms以下
                - 可用性: 99.9%%""");
        };
    }
    
    private String buildConversationHistory(List<InterviewMessage> messages) {
        StringBuilder history = new StringBuilder();
        for (InterviewMessage message : messages) {
            String roleLabel = message.getRole() == InterviewMessage.MessageRole.INTERVIEWER ? "面接官" : "候補者";
            history.append(String.format("%s: %s\n\n", roleLabel, message.getContent()));
        }
        return history.toString();
    }
    
    private String getQuestionContext(String topic) {
        return String.format("""
            現在の面接トピック: %s (AWS環境前提)
            
            重要な評価ポイント:
            - AWSサービスの適切な選択と組み合わせ
            - スケーラビリティ戦略（Auto Scaling、ELB、CloudFront）
            - データベース設計（RDS、DynamoDB、ElastiCache）
            - 可用性とfault tolerance（Multi-AZ、Region構成）
            - 一貫性モデル（CAP定理の理解）
            - パフォーマンス最適化（CloudFront、ElastiCache）
            - セキュリティ考慮事項（VPC、IAM、Security Groups）
            - 監視とモニタリング戦略（CloudWatch、X-Ray）
            - コスト最適化の考慮
            
            面接官は候補者の回答に応じてMermaid図を使って具体的なAWS構成を示すことがある。
            """, topic);
    }
    
    private void generateFeedback(Long sessionId) {
        List<InterviewMessage> messages = interviewMessageRepository.findByInterviewSessionIdOrderByCreatedAtAsc(sessionId);
        String conversationHistory = buildConversationHistory(messages);
        
        // Analyze candidate performance
        String candidatePerformance = analyzeCandidatePerformance(messages);
        
        // Generate feedback using Bedrock
        String feedbackJson = bedrockService.generateFeedback(conversationHistory, candidatePerformance);
        
        try {
            JsonNode feedbackNode = objectMapper.readTree(feedbackJson);
            
            InterviewFeedback feedback = new InterviewFeedback();
            feedback.setInterviewSession(interviewSessionRepository.findById(sessionId).orElse(null));
            feedback.setOverallScore(feedbackNode.get("overallScore").asInt());
            feedback.setTechnicalScore(feedbackNode.get("technicalScore").asInt());
            feedback.setCommunicationScore(feedbackNode.get("communicationScore").asInt());
            feedback.setProblemSolvingScore(feedbackNode.get("problemSolvingScore").asInt());
            feedback.setStrengths(feedbackNode.get("strengths").asText());
            feedback.setAreasForImprovement(feedbackNode.get("areasForImprovement").asText());
            feedback.setDetailedFeedback(feedbackNode.get("detailedFeedback").asText());
            feedback.setRecommendations(feedbackNode.get("recommendations").asText());
            
            interviewFeedbackRepository.save(feedback);
            
        } catch (Exception e) {
            // Fallback feedback if JSON parsing fails
            InterviewFeedback feedback = new InterviewFeedback();
            feedback.setInterviewSession(interviewSessionRepository.findById(sessionId).orElse(null));
            feedback.setOverallScore(5);
            feedback.setTechnicalScore(5);
            feedback.setCommunicationScore(5);
            feedback.setProblemSolvingScore(5);
            feedback.setDetailedFeedback("フィードバックの生成中にエラーが発生しました。");
            
            interviewFeedbackRepository.save(feedback);
        }
    }
    
    private String analyzeCandidatePerformance(List<InterviewMessage> messages) {
        StringBuilder analysis = new StringBuilder();
        
        List<InterviewMessage> candidateMessages = messages.stream()
                .filter(m -> m.getRole() == InterviewMessage.MessageRole.CANDIDATE)
                .toList();
        
        analysis.append("候補者の回答数: ").append(candidateMessages.size()).append("\n");
        
        int totalLength = candidateMessages.stream()
                .mapToInt(m -> m.getContent().length())
                .sum();
        analysis.append("平均回答長: ").append(totalLength / Math.max(candidateMessages.size(), 1)).append("文字\n");
        
        // Simple keyword analysis
        String allResponses = candidateMessages.stream()
                .map(InterviewMessage::getContent)
                .reduce("", (a, b) -> a + " " + b);
        
        String[] technicalKeywords = {"スケール", "データベース", "キャッシュ", "負荷分散", "レプリケーション", 
                                    "パーティション", "シャーディング", "可用性", "パフォーマンス"};
        
        int keywordCount = 0;
        for (String keyword : technicalKeywords) {
            if (allResponses.contains(keyword)) {
                keywordCount++;
            }
        }
        
        analysis.append("技術的キーワードの使用: ").append(keywordCount).append("/").append(technicalKeywords.length);
        
        return analysis.toString();
    }
    
    private String getCurrentDiagramState(List<InterviewMessage> messages) {
        // Extract current diagram state from message history
        String currentDiagram = """
            graph TD
              A[Frontend<br/>React/Vue.js] --> B[Backend<br/>ECS/EC2]
              
              style A fill:#e1f5fe
              style B fill:#f3e5f5
            """;
        
        // Find the most recent diagram update in messages
        for (int i = messages.size() - 1; i >= 0; i--) {
            InterviewMessage message = messages.get(i);
            if (message.getRole() == InterviewMessage.MessageRole.INTERVIEWER && 
                message.getContent().contains("DIAGRAM_UPDATE:")) {
                String[] lines = message.getContent().split("\n");
                for (String line : lines) {
                    if (line.startsWith("DIAGRAM_UPDATE:")) {
                        return line.substring("DIAGRAM_UPDATE:".length()).trim();
                    }
                }
            }
        }
        
        return currentDiagram;
    }
}