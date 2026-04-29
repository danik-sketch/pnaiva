import './App.css';
import React, { useMemo, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import CalendarPage from './pages/CalendarPage';
import ProjectsPage from './pages/ProjectsPage';
import ProjectDetailsPage from './pages/ProjectDetailsPage';
import ProjectCreatePage from './pages/ProjectCreatePage';

function App() {
  const [auth, setAuth] = useState(() => ({
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
  }));

  const isAuthenticated = useMemo(() => Boolean(auth.token), [auth.token]);

  const handleLogin = (payload) => {
    localStorage.setItem('token', payload.token);
    localStorage.setItem('username', payload.username);
    localStorage.setItem('userId', String(payload.userId));
    setAuth({ token: payload.token, username: payload.username });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    setAuth({ token: null, username: null });
  };

  return (
    <Router>
      <div className="app">
        {isAuthenticated && (
          <nav className="navbar minimal-navbar">
            <div className="nav-container">
              <Link to="/" className="nav-logo">Notebook</Link>
              <ul className="nav-menu">
                <li><Link to="/calendar" className="nav-link">Календарь</Link></li>
                <li><Link to="/projects" className="nav-link">Проекты</Link></li>
                <li><button className="btn btn-logout" onClick={handleLogout}>Выйти</button></li>
              </ul>
            </div>
          </nav>
        )}

        <div className="main-content">
          <Routes>
            <Route path="/login" element={isAuthenticated ? <Navigate to="/projects" replace /> : <LoginPage onLogin={handleLogin} />} />
            <Route path="/" element={<Navigate to="/projects" replace />} />
            <Route path="/calendar" element={isAuthenticated ? <CalendarPage /> : <Navigate to="/login" replace />} />
            <Route path="/projects" element={isAuthenticated ? <ProjectsPage /> : <Navigate to="/login" replace />} />
            <Route path="/projects/new" element={isAuthenticated ? <ProjectCreatePage /> : <Navigate to="/login" replace />} />
            <Route path="/projects/:id" element={isAuthenticated ? <ProjectDetailsPage /> : <Navigate to="/login" replace />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
