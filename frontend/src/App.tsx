import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './App.css';
import InterviewStart from './components/InterviewStart';
import InterviewChat from './components/InterviewChat';
import InterviewFeedback from './components/InterviewFeedback';

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<InterviewStart />} />
        <Route path="/start" element={<InterviewStart />} />
        <Route path="/interview/:sessionId" element={<InterviewChat />} />
        <Route path="/feedback/:sessionId" element={<InterviewFeedback />} />
      </Routes>
    </div>
  );
}

export default App;