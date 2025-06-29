export interface InterviewSession {
  id: number;
  candidateName: string;
  topic: string;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
  endedAt?: string;
}

export interface InterviewMessage {
  id: number;
  role: 'INTERVIEWER' | 'CANDIDATE';
  content: string;
  createdAt: string;
}

export interface InterviewFeedback {
  id: number;
  overallScore: number;
  technicalScore: number;
  communicationScore: number;
  problemSolvingScore: number;
  strengths: string;
  areasForImprovement: string;
  detailedFeedback: string;
  recommendations: string;
  createdAt: string;
}

export interface StartInterviewRequest {
  candidateName: string;
  topic: string;
}

export interface CandidateResponseRequest {
  response: string;
}

export interface InterviewSessionResponse {
  session: InterviewSession;
  messages: InterviewMessage[];
}