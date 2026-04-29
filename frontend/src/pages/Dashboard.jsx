import React, { useState, useEffect } from 'react';
import { projectService } from '../services/api';

function Dashboard() {
  const [stats, setStats] = useState({ projects: 0, tasks: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const projects = await projectService.getAll();
      setStats({
        projects: projects.length,
        tasks: projects.reduce((sum, p) => sum + (p.tasks?.length || 0), 0),
      });
    } catch (err) {
      setError('Failed to load data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page dashboard">
      <div className="page-header">
        <h1>Главная</h1>
      </div>

      {error && <div className="error-message">{error}</div>}
      
      {loading ? (
        <p>Загрузка...</p>
      ) : (
        <div className="stats-grid">
          <div className="stat-card">
            <h3>Проекты</h3>
            <p className="stat-number">{stats.projects}</p>
          </div>
          <div className="stat-card">
            <h3>Задачи</h3>
            <p className="stat-number">{stats.tasks}</p>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
