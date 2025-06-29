package com.example.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class BedrockService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.bedrock.region:ap-northeast-1}")
    private String region;
    
    @Value("${aws.bedrock.model-id:anthropic.claude-3-sonnet-20240229-v1:0}")
    private String modelId;
    
    public BedrockService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(System.getProperty("aws.bedrock.region", "us-east-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateInterviewerResponse(String conversationHistory, String candidateResponse, String questionContext) {
        try {
            String prompt = buildInterviewerPrompt(conversationHistory, candidateResponse, questionContext);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", new Object[]{message});
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.get("content").get(0).get("text").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate interviewer response", e);
        }
    }
    
    public String generateInterviewerResponseWithDiagram(String conversationHistory, String candidateResponse, String questionContext, String currentDiagram) {
        try {
            String prompt = buildInterviewerPromptWithDiagram(conversationHistory, candidateResponse, questionContext, currentDiagram);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 1200);
            requestBody.put("temperature", 0.7);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", new Object[]{message});
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.get("content").get(0).get("text").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate interviewer response with diagram", e);
        }
    }
    
    public String generateFeedback(String conversationHistory, String candidatePerformance) {
        try {
            String prompt = buildFeedbackPrompt(conversationHistory, candidatePerformance);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.3);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", new Object[]{message});
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.get("content").get(0).get("text").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate feedback", e);
        }
    }
    
    public String generateDetailedRequirements(String topic, String baseRequirements) {
        try {
            String prompt = buildRequirementsPrompt(topic, baseRequirements);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.5);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", new Object[]{message});
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.get("content").get(0).get("text").asText();
            
        } catch (Exception e) {
            // Fallback to base requirements if LLM generation fails
            return baseRequirements;
        }
    }
    
    private String buildRequirementsPrompt(String topic, String baseRequirements) {
        return String.format("""
            あなたはシステムデザイン面接官です。以下の基本要件をもとに、より具体的で面接に適した要件を生成してください。
            
            トピック: %s
            
            基本要件:
            %s
            
            以下の点を考慮して要件を詳細化してください：
            - 面接時間に適した適切な複雑さ
            - 技術的な判断が必要な要素を含める
            - スケーラビリティの課題が明確になるような規模設定
            - 現実的でありながら面接に適した制約
            
            出力は日本語で、面接官が候補者に提示する形式で記述してください。
            簡潔にまとめ、500文字以内で回答してください。
            """, topic, baseRequirements);
    }
    
    private String buildInterviewerPrompt(String conversationHistory, String candidateResponse, String questionContext) {
        return String.format("""
            あなたは経験豊富なシステムデザイン面接官です。候補者のシステムデザイン能力を評価するために面接を行っています。
            
            面接の方針：
            - 非機能要件（スケーラビリティ、可用性、一貫性、パフォーマンス）に重点を置く
            - 候補者の回答を適度に深掘りする
            - 具体的な技術的詳細について質問する
            - トレードオフについて考えさせる
            - 現実的な制約を考慮させる
            
            質問のコンテキスト：
            %s
            
            これまでの会話履歴：
            %s
            
            候補者の最新の回答：
            %s
            
            上記を踏まえて、適切な面接官としての次の質問や深掘りを行ってください。
            回答は日本語で、面接官らしい口調で行ってください。
            """, questionContext, conversationHistory, candidateResponse);
    }
    
    private String buildFeedbackPrompt(String conversationHistory, String candidatePerformance) {
        return String.format("""
            あなたは経験豊富なシステムデザイン面接官です。以下の面接内容を評価し、フィードバックを提供してください。
            
            面接の会話履歴：
            %s
            
            候補者のパフォーマンス概要：
            %s
            
            以下の観点で評価し、JSON形式で回答してください：
            {
                "overallScore": 1-10の総合評価,
                "technicalScore": 1-10の技術的理解度,
                "communicationScore": 1-10のコミュニケーション能力,
                "problemSolvingScore": 1-10の問題解決能力,
                "strengths": "候補者の強み",
                "areasForImprovement": "改善すべき点",
                "detailedFeedback": "詳細なフィードバック",
                "recommendations": "今後の学習に関する推奨事項"
            }
            
            評価は客観的かつ建設的に行ってください。
            """, conversationHistory, candidatePerformance);
    }
    
    private String buildInterviewerPromptWithDiagram(String conversationHistory, String candidateResponse, String questionContext, String currentDiagram) {
        return String.format("""
            あなたは経験豊富なシステムデザイン面接官です。候補者のシステムデザイン能力を評価するために面接を行っています。
            
            面接の方針：
            - 非機能要件（スケーラビリティ、可用性、一貫性、パフォーマンス）に重点を置く
            - 候補者の回答を適度に深掘りする
            - 具体的な技術的詳細について質問する
            - トレードオフについて考えさせる
            - 現実的な制約を考慮させる
            - 候補者の回答に応じて図を進化させる
            
            現在のアーキテクチャ図：
            %s
            
            質問のコンテキスト：
            %s
            
            これまでの会話履歴：
            %s
            
            候補者の最新の回答：
            %s
            
            指示：
            1. 候補者の回答を評価し、適切な面接官としての次の質問や深掘りを行ってください
            2. 候補者が新しいコンポーネントやアーキテクチャを提案した場合、図を更新してください
            3. 図を更新する場合は、回答の最後に「DIAGRAM_UPDATE: [新しいMermaid図のコード]」を含めてください
            4. 図の更新例：
               - ロードバランサーの追加
               - データベースの分離
               - キャッシュレイヤーの追加
               - CDNの追加
               - マイクロサービス化
               - Auto Scalingグループの追加
            
            回答は日本語で、面接官らしい口調で行ってください。
            """, currentDiagram, questionContext, conversationHistory, candidateResponse);
    }
}