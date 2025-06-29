import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { interviewApi } from '../services/api';
import { InterviewFeedback as FeedbackType, InterviewSession } from '../types';

interface InterviewFeedbackProps {}

const InterviewFeedback: React.FC<InterviewFeedbackProps> = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const [session, setSession] = useState<InterviewSession | null>(null);
  const [feedback, setFeedback] = useState<FeedbackType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (sessionId) {
      loadSessionAndFeedback();
    }
  }, [sessionId]);

  const loadSessionAndFeedback = async () => {
    if (!sessionId) return;
    
    try {
      const [sessionData, feedbackData] = await Promise.all([
        interviewApi.getInterviewSession(parseInt(sessionId)),
        interviewApi.getFeedback(parseInt(sessionId))
      ]);
      setSession(sessionData.session);
      setFeedback(feedbackData);
    } catch (err) {
      setError('セッションまたはフィードバックの取得に失敗しました');
      console.error('Failed to load session and feedback:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRestart = () => {
    navigate('/');
  };

  const getScoreColor = (score: number) => {
    if (score >= 8) return '#4CAF50'; // Green
    if (score >= 6) return '#FF9800'; // Orange
    return '#F44336'; // Red
  };

  const getScoreText = (score: number) => {
    if (score >= 8) return '優秀';
    if (score >= 6) return '良好';
    if (score >= 4) return '普通';
    return '要改善';
  };

  if (loading) {
    return (
      <div className="feedback-loading">
        <h2>フィードバックを生成中...</h2>
        <p>AI面接官があなたの回答を分析しています。しばらくお待ちください。</p>
      </div>
    );
  }

  if (error || !feedback || !session) {
    return (
      <div className="feedback-error">
        <h2>エラー</h2>
        <p>{error}</p>
        <button onClick={handleRestart}>最初に戻る</button>
      </div>
    );
  }

  return (
    <div className="interview-feedback">
      <div className="container">
        <h1>面接フィードバック</h1>
        
        <div className="session-summary">
          <h3>面接概要</h3>
          <p><strong>候補者:</strong> {session.candidateName}</p>
          <p><strong>トピック:</strong> {session.topic}</p>
          <p><strong>実施日時:</strong> {new Date(session.createdAt).toLocaleString('ja-JP')}</p>
        </div>

        <div className="scores-section">
          <h3>評価スコア</h3>
          <div className="scores-grid">
            <div className="score-item">
              <div className="score-label">総合評価</div>
              <div 
                className="score-value"
                style={{ color: getScoreColor(feedback.overallScore) }}
              >
                {feedback.overallScore}/10
              </div>
              <div className="score-text">{getScoreText(feedback.overallScore)}</div>
            </div>
            
            <div className="score-item">
              <div className="score-label">技術的理解</div>
              <div 
                className="score-value"
                style={{ color: getScoreColor(feedback.technicalScore) }}
              >
                {feedback.technicalScore}/10
              </div>
              <div className="score-text">{getScoreText(feedback.technicalScore)}</div>
            </div>
            
            <div className="score-item">
              <div className="score-label">コミュニケーション</div>
              <div 
                className="score-value"
                style={{ color: getScoreColor(feedback.communicationScore) }}
              >
                {feedback.communicationScore}/10
              </div>
              <div className="score-text">{getScoreText(feedback.communicationScore)}</div>
            </div>
            
            <div className="score-item">
              <div className="score-label">問題解決能力</div>
              <div 
                className="score-value"
                style={{ color: getScoreColor(feedback.problemSolvingScore) }}
              >
                {feedback.problemSolvingScore}/10
              </div>
              <div className="score-text">{getScoreText(feedback.problemSolvingScore)}</div>
            </div>
          </div>
        </div>

        <div className="feedback-details">
          <div className="feedback-section">
            <h3>強み</h3>
            <p>{feedback.strengths}</p>
          </div>

          <div className="feedback-section">
            <h3>改善すべき点</h3>
            <p>{feedback.areasForImprovement}</p>
          </div>

          <div className="feedback-section">
            <h3>詳細フィードバック</h3>
            <p>{feedback.detailedFeedback}</p>
          </div>

          <div className="feedback-section">
            <h3>学習推奨事項</h3>
            <p>{feedback.recommendations}</p>
          </div>
        </div>

        <div className="actions">
          <button onClick={handleRestart} className="restart-button">
            新しい面接を開始
          </button>
        </div>
      </div>
    </div>
  );
};

export default InterviewFeedback;