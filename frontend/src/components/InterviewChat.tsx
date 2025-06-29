import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { interviewApi } from '../services/api';
import { InterviewSession, InterviewMessage } from '../types';
import mermaid from 'mermaid';

interface InterviewChatProps {}

const InterviewChat: React.FC<InterviewChatProps> = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const [session, setSession] = useState<InterviewSession | null>(null);
  const [messages, setMessages] = useState<InterviewMessage[]>([]);
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentDiagram, setCurrentDiagram] = useState<string>('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const diagramRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (sessionId) {
      loadSession();
    }
  }, [sessionId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    mermaid.initialize({ startOnLoad: true, theme: 'default' });
  }, []);

  useEffect(() => {
    // Re-render Mermaid diagrams when messages update
    setTimeout(() => {
      mermaid.init(undefined, '.mermaid');
    }, 100);
  }, [messages]);

  useEffect(() => {
    // Re-render fixed diagram when currentDiagram changes
    if (currentDiagram && diagramRef.current) {
      setTimeout(() => {
        mermaid.init(undefined, diagramRef.current!);
      }, 100);
    }
  }, [currentDiagram]);

  const loadSession = async () => {
    if (!sessionId) return;
    
    try {
      const sessionData = await interviewApi.getInterviewSession(parseInt(sessionId));
      setSession(sessionData.session);
      setMessages(sessionData.messages);
      
      // Set initial diagram
      const initialDiagram = `
        graph TD
          A[Frontend<br/>React/Vue.js] --> B[Backend<br/>ECS/EC2]
          
          style A fill:#e1f5fe
          style B fill:#f3e5f5
      `;
      setCurrentDiagram(initialDiagram);
    } catch (err) {
      setError('セッションの取得に失敗しました');
      console.error('Failed to load session:', err);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!response.trim() || loading) return;

    setLoading(true);
    setError(null);

    try {
      // 候補者の回答を送信
      const candidateMessage: InterviewMessage = {
        id: Date.now(), // 一時的なID
        role: 'CANDIDATE',
        content: response.trim(),
        createdAt: new Date().toISOString(),
      };
      
      setMessages(prev => [...prev, candidateMessage]);
      setResponse('');

      // 面接官の返答を取得
      const interviewerMessage = await interviewApi.respondToCandidate(parseInt(sessionId!), {
        response: response.trim(),
      });

      setMessages(prev => [...prev, interviewerMessage]);

      // Update diagram if interviewer message contains new diagram
      if (interviewerMessage.content.includes('DIAGRAM_UPDATE:')) {
        const diagramMatch = interviewerMessage.content.match(/DIAGRAM_UPDATE:\s*(.+?)(?:\n|$)/);
        if (diagramMatch) {
          setCurrentDiagram(diagramMatch[1]);
        }
      }

      // 面接が終了したかチェック
      if (interviewerMessage.content.includes('面接を終了します')) {
        setTimeout(() => {
          navigate(`/feedback/${sessionId}`);
        }, 2000);
      }
    } catch (err) {
      setError('回答の送信に失敗しました。もう一度お試しください。');
      console.error('Failed to send response:', err);
      // エラー時は候補者のメッセージを削除
      setMessages(prev => prev.slice(0, -1));
    } finally {
      setLoading(false);
    }
  };

  const handleFinishInterview = async () => {
    if (loading || !sessionId) return;

    setLoading(true);
    try {
      await interviewApi.finishInterview(parseInt(sessionId));
      navigate(`/feedback/${sessionId}`);
    } catch (err) {
      setError('面接の終了に失敗しました');
      console.error('Failed to finish interview:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString('ja-JP', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const renderMessageContent = (content: string) => {
    // Check if content contains Mermaid diagram
    if (content.includes('```mermaid')) {
      const parts = content.split('```mermaid');
      return parts.map((part, index) => {
        if (index === 0) {
          return <div key={index}>{part}</div>;
        }
        
        const [diagramCode, ...rest] = part.split('```');
        return (
          <div key={index}>
            <div className="mermaid" style={{ backgroundColor: 'white', padding: '10px', margin: '10px 0', borderRadius: '5px' }}>
              {diagramCode.trim()}
            </div>
            {rest.length > 0 && <div>{rest.join('```')}</div>}
          </div>
        );
      });
    }
    
    return <div>{content}</div>;
  };

  if (!session) {
    return <div className="loading">セッションを読み込んでいます...</div>;
  }

  return (
    <div className="interview-chat">
      <div className="chat-header">
        <h2>システムデザイン面接</h2>
        <div className="session-info">
          <span>候補者: {session.candidateName}</span>
          <span>トピック: {session.topic}</span>
        </div>
        <button 
          onClick={handleFinishInterview}
          disabled={loading}
          className="finish-button"
        >
          面接を終了
        </button>
      </div>

      {/* Fixed Diagram Area */}
      <div className="diagram-area" style={{
        position: 'sticky',
        top: '0',
        backgroundColor: 'white',
        padding: '10px',
        borderBottom: '1px solid #ddd',
        zIndex: 100
      }}>
        <h3>現在のアーキテクチャ</h3>
        {currentDiagram && (
          <div 
            ref={diagramRef}
            className="mermaid fixed-diagram" 
            style={{ 
              backgroundColor: '#f9f9f9', 
              padding: '15px', 
              borderRadius: '8px',
              border: '1px solid #ddd'
            }}
          >
            {currentDiagram}
          </div>
        )}
      </div>

      <div className="messages-container">
        {messages.map((message, index) => (
          <div 
            key={`${message.id}-${index}`} 
            className={`message ${message.role.toLowerCase()}`}
          >
            <div className="message-header">
              <span className="role">
                {message.role === 'INTERVIEWER' ? '面接官' : '候補者'}
              </span>
              <span className="time">{formatTime(message.createdAt)}</span>
            </div>
            <div className="message-content">
              {renderMessageContent(message.content)}
            </div>
          </div>
        ))}
        {loading && (
          <div className="message interviewer">
            <div className="message-header">
              <span className="role">面接官</span>
              <span className="time">入力中...</span>
            </div>
            <div className="message-content typing">
              <span>回答を考えています...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSubmit} className="response-form">
        {error && <div className="error-message">{error}</div>}
        
        <div className="input-container">
          <textarea
            value={response}
            onChange={(e) => setResponse(e.target.value)}
            placeholder="あなたの回答を入力してください..."
            disabled={loading}
            rows={4}
            required
          />
          <button 
            type="submit" 
            disabled={loading || !response.trim()}
            className="send-button"
          >
            {loading ? '送信中...' : '回答を送信'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default InterviewChat;