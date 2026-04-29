import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { projectService } from '../services/api';

const ProjectsPage = () => {
  const [allProjects, setAllProjects] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    projectName: '',
    taskTitle: '',
    completed: '',
    dueFrom: '',
    dueTo: '',
  });

  useEffect(() => {
    loadProjects();
  }, []);

  useEffect(() => {
    const debounceId = setTimeout(() => {
      setProjects(applyFilters(allProjects, filters));
    }, 250);

    return () => clearTimeout(debounceId);
  }, [filters, allProjects]);

  const applyFilters = (source, activeFilters) => {
    const projectName = activeFilters.projectName.trim().toLowerCase();
    const taskTitle = activeFilters.taskTitle.trim().toLowerCase();
    const completed = activeFilters.completed;
    const dueFrom = activeFilters.dueFrom ? new Date(`${activeFilters.dueFrom}T00:00:00`) : null;
    const dueTo = activeFilters.dueTo ? new Date(`${activeFilters.dueTo}T23:59:59`) : null;

    return source.filter((project) => {
      const nameMatch = !projectName || (project.name || '').toLowerCase().includes(projectName);
      if (!nameMatch) {
        return false;
      }

      const tasks = project.tasks || [];

      if (!taskTitle && !completed && !dueFrom && !dueTo) {
        return true;
      }

      return tasks.some((task) => {
        const taskNameMatch = !taskTitle || (task.title || '').toLowerCase().includes(taskTitle);
        const statusMatch = !completed
          || (completed === 'true' && Boolean(task.completed))
          || (completed === 'false' && !task.completed);
        const dueDate = task.dueDate ? new Date(task.dueDate) : null;
        const fromMatch = !dueFrom || (dueDate && dueDate >= dueFrom);
        const toMatch = !dueTo || (dueDate && dueDate <= dueTo);
        return taskNameMatch && statusMatch && fromMatch && toMatch;
      });
    });
  };

  const loadProjects = async () => {
    try {
      setLoading(true);
      const data = await projectService.getAll();
      setAllProjects(data);
      setProjects(applyFilters(data, filters));
    } catch (err) {
      console.error('Load error', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Удалить проект?')) {
      try {
        await projectService.delete(id);
        await loadProjects();
      } catch (err) {
        console.error('Delete error', err);
      }
    }
  };

  const handleSearchSubmit = async (event) => {
    event.preventDefault();
    setProjects(applyFilters(allProjects, filters));
  };

  const resetSearch = async () => {
    setFilters({
      projectName: '',
      taskTitle: '',
      completed: '',
      dueFrom: '',
      dueTo: '',
    });
    setProjects(allProjects);
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Проекты</h1>
        <Link className="btn btn-primary" to="/projects/new">+ Новый</Link>
      </div>

      <form className="form-container" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="Поиск по имени проекта"
          value={filters.projectName}
          onChange={(e) => setFilters({ ...filters, projectName: e.target.value })}
        />
        <input
          type="text"
          placeholder="Поиск по названию задачи"
          value={filters.taskTitle}
          onChange={(e) => setFilters({ ...filters, taskTitle: e.target.value })}
        />
        <select
          value={filters.completed}
          onChange={(e) => setFilters({ ...filters, completed: e.target.value })}
        >
          <option value="">Статус задачи: любой</option>
          <option value="false">Только активные</option>
          <option value="true">Только завершенные</option>
        </select>
        <input
          type="date"
          value={filters.dueFrom}
          onChange={(e) => setFilters({ ...filters, dueFrom: e.target.value })}
        />
        <input
          type="date"
          value={filters.dueTo}
          onChange={(e) => setFilters({ ...filters, dueTo: e.target.value })}
        />
        <div className="card-actions">
          <button type="submit" className="btn btn-primary">Искать</button>
          <button type="button" className="btn" onClick={resetSearch}>Сбросить</button>
        </div>
      </form>

      {loading ? (
        <p>Загрузка...</p>
      ) : projects.length === 0 ? (
        <p>Нет проектов</p>
      ) : (
        <div className="cards-grid">
          {projects.map((p) => (
            <div key={p.id} className="card">
              <h3>{p.name}</h3>
              <p className="card-desc">{p.description}</p>
              <div className="card-actions project-card-actions">
                <Link className="btn btn-sm" to={`/projects/${p.id}`}>
                  Подробнее
                </Link>
                <button className="btn btn-sm btn-delete" onClick={() => handleDelete(p.id)}>
                  Удалить
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ProjectsPage;