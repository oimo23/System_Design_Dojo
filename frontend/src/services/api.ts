import axios from 'axios';
import { 
  InterviewSession, 
  InterviewMessage, 
  InterviewFeedback, 
  StartInterviewRequest, 
  CandidateResponseRequest,
  InterviewSessionResponse
} from '../types';

const API_BASE_URL = 'http://localhost:8003';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const interviewApi = {
  // 面接を開始
  startInterview: async (request: StartInterviewRequest): Promise<InterviewSession> => {
    const response = await api.post('/api/interviews/start', request);
    return response.data;
  },

  // 候補者の回答を送信
  respondToCandidate: async (sessionId: number, request: CandidateResponseRequest): Promise<InterviewMessage> => {
    const response = await api.post(`/api/interviews/${sessionId}/respond`, request);
    return response.data;
  },

  // 面接セッション情報を取得
  getInterviewSession: async (sessionId: number): Promise<InterviewSessionResponse> => {
    const response = await api.get(`/api/interviews/${sessionId}`);
    return response.data;
  },

  // 面接を終了
  finishInterview: async (sessionId: number): Promise<InterviewMessage> => {
    const response = await api.post(`/api/interviews/${sessionId}/finish`);
    return response.data;
  },

  // フィードバックを取得
  getFeedback: async (sessionId: number): Promise<InterviewFeedback> => {
    const response = await api.get(`/api/interviews/${sessionId}/feedback`);
    return response.data;
  },

  // メッセージ一覧を取得
  getMessages: async (sessionId: number): Promise<InterviewMessage[]> => {
    const response = await api.get(`/api/interviews/${sessionId}/messages`);
    return response.data;
  },
};

export default api;