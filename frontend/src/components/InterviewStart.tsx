import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { interviewApi } from '../services/api';
import { InterviewSession } from '../types';

interface InterviewStartProps {}

const InterviewStart: React.FC<InterviewStartProps> = () => {
  const navigate = useNavigate();
  const [candidateName, setCandidateName] = useState('');
  const [topic, setTopic] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const predefinedTopics = [
    '大規模SNSサービスの設計',
    '動画ストリーミングサービスの設計',
    'チャットアプリケーションの設計',
    'eコマースサイトの設計',
    'URL短縮サービスの設計',
    '検索エンジンの設計',
    '配車サービスの設計',
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!candidateName.trim() || !topic.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const session = await interviewApi.startInterview({
        candidateName: candidateName.trim(),
        topic: topic.trim(),
      });
      navigate(`/interview/${session.id}`);
    } catch (err) {
      setError('面接の開始に失敗しました。しばらく待ってから再試行してください。');
      console.error('Failed to start interview:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="interview-start">
      <div className="container">
        <h1>システムデザイン面接システム</h1>
        <p className="subtitle">
          システム設計能力を評価するAI面接官との対話型面接を開始します。
        </p>

        <form onSubmit={handleSubmit} className="start-form">
          <div className="form-group">
            <label htmlFor="candidateName">候補者名</label>
            <input
              id="candidateName"
              type="text"
              value={candidateName}
              onChange={(e) => setCandidateName(e.target.value)}
              placeholder="お名前を入力してください"
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="topic">面接トピック</label>
            <select
              id="topic"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              disabled={loading}
              required
            >
              <option value="">トピックを選択してください</option>
              {predefinedTopics.map((predefinedTopic) => (
                <option key={predefinedTopic} value={predefinedTopic}>
                  {predefinedTopic}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="customTopic">カスタムトピック（任意）</label>
            <input
              id="customTopic"
              type="text"
              value={topic.includes('カスタム:') ? topic.replace('カスタム:', '') : ''}
              onChange={(e) => setTopic(`カスタム:${e.target.value}`)}
              placeholder="独自のトピックを入力"
              disabled={loading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading || !candidateName.trim() || !topic.trim()}>
            {loading ? '面接を開始中...' : '面接を開始'}
          </button>
        </form>

        <div className="info-section">
          <h3>面接について</h3>
          <ul>
            <li>AI面接官がシステム設計に関する質問を行います</li>
            <li>非機能要件（スケーラビリティ、可用性など）に重点を置きます</li>
            <li>約5回の質疑応答で面接が終了します</li>
            <li>最後に詳細なフィードバックを受け取れます</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default InterviewStart;