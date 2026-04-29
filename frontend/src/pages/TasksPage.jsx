import React, { useEffect, useMemo, useState } from 'react';
import { projectService, taskService } from '../services/api';
import '../styles/TasksPage.css';

const initialForm = {
  title: '',
  description: '',
  dueDate: '',
  projectId: '',
  done: false,
};

const toDateTimeLocal = (isoDateTime) => {
  if (!isoDateTime) {
    return '';
  }

  const parsed = new Date(isoDateTime);
  if (Number.isNaN(parsed.getTime())) {
    return '';
  }

  const timezoneOffsetMs = parsed.getTimezoneOffset() * 60 * 1000;
  return new Date(parsed.getTime() - timezoneOffsetMs).toISOString().slice(0, 16);
};

function TasksPage() {
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState(initialForm);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [page, setPage] = useState(1);

  const pageSize = 6;

  const preparedTasks = useMemo(
    () =>
      [...tasks].sort((a, b) => {
        const dateA = a.dueDate ? new Date(a.dueDate).getTime() : Number.MAX_SAFE_INTEGER;
        const dateB = b.dueDate ? new Date(b.dueDate).getTime() : Number.MAX_SAFE_INTEGER;
        return dateA - dateB;
      }),
    [tasks],
  );

  const filteredTasks = useMemo(() => {
    return preparedTasks.filter((task) => {
      const inSearch =
        query.trim().length === 0 ||
        task.title?.toLowerCase().includes(query.toLowerCase()) ||
        task.description?.toLowerCase().includes(query.toLowerCase()) ||
        task.projectName?.toLowerCase().includes(query.toLowerCase());

      if (!inSearch) {
        return false;
      }

      if (statusFilter === 'done') {
        return Boolean(task.completed);
      }
      if (statusFilter === 'todo') {
        return !task.completed;
      }
      return true;
    });
  }, [preparedTasks, query, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredTasks.length / pageSize));
  const pagedTasks = useMemo(() => {
    const start = (page - 1) * pageSize;
    return filteredTasks.slice(start, start + pageSize);
  }, [filteredTasks, page]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [tasksData, projectsData] = await Promise.all([
        taskService.getAll(),
        projectService.getAll(),
      ]);
      setTasks(tasksData);
      setProjects(projectsData);
    } catch (err) {
      setError('Не удалось загрузить задачи');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    setPage(1);
  }, [query, statusFilter]);

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const resetForm = () => {
    setFormData(initialForm);
    setEditingId(null);
    setShowForm(false);
  };

  const handleEdit = (task) => {
    setEditingId(task.id);
    setFormData({
      title: task.title || '',
      description: task.description || '',
      dueDate: toDateTimeLocal(task.dueDate),
      projectId: projects.find((project) => project.name === task.projectName)?.id || '',
      done: Boolean(task.completed),
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Удалить задачу?')) {
      return;
    }

    try {
      await taskService.delete(id);
      await loadData();
    } catch (err) {
      setError('Не удалось удалить задачу');
      console.error(err);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const dueDateIso = new Date(formData.dueDate).toISOString();
    const completedIso = formData.done ? new Date().toISOString() : null;

    const payload = {
      title: formData.title.trim(),
      description: formData.description.trim(),
      dueDate: dueDateIso,
      completed: completedIso,
      projectId: Number(formData.projectId),
      categoryIds: [],
      reminders: [],
    };

    try {
      setError('');
      if (editingId) {
        await taskService.update(editingId, payload);
      } else {
        await taskService.create(payload);
      }
      await loadData();
      resetForm();
    } catch (err) {
      setError('Не удалось сохранить задачу');
      console.error(err);
    }
  };

  return (
    <div className="page tasks-layout">
      <div className="tasks-header">
        <div>
          <h1>Список задач</h1>
          <p className="tasks-subtitle">Минималистичный трекер с быстрым управлением</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm((value) => !value)}>
          {showForm ? 'Закрыть форму' : '+ Добавить задачу'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="tasks-toolbar">
        <input
          className="tasks-search"
          type="text"
          placeholder="Поиск по задаче, описанию или проекту"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
          <option value="all">Все</option>
          <option value="todo">В работе</option>
          <option value="done">Выполненные</option>
        </select>
      </div>

      {showForm && (
        <form className="form-container tasks-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Название задачи"
            value={formData.title}
            onChange={(event) => setFormData({ ...formData, title: event.target.value })}
            required
          />
          <textarea
            placeholder="Описание"
            value={formData.description}
            onChange={(event) => setFormData({ ...formData, description: event.target.value })}
          />
          <input
            type="datetime-local"
            value={formData.dueDate}
            onChange={(event) => setFormData({ ...formData, dueDate: event.target.value })}
            required
          />
          <select
            value={formData.projectId}
            onChange={(event) => setFormData({ ...formData, projectId: event.target.value })}
            required
          >
            <option value="">Выбери проект</option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {project.name}
              </option>
            ))}
          </select>
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={formData.done}
              onChange={(event) => setFormData({ ...formData, done: event.target.checked })}
            />
            Отметить как выполненную
          </label>
          <button type="submit" className="btn btn-success">
            {editingId ? 'Обновить' : 'Создать'}
          </button>
        </form>
      )}

      {loading ? (
        <p>Загрузка...</p>
      ) : filteredTasks.length === 0 ? (
        <p>Пока нет задач</p>
      ) : (
        <>
          <div className="task-list">
            {pagedTasks.map((task) => (
              <div key={task.id} className="task-row">
                <div className="task-main">
                  <h3>{task.title}</h3>
                  <p>{task.description || 'Без описания'}</p>
                </div>
                <div className="task-meta">
                  <span className="chip">{task.projectName || 'Без проекта'}</span>
                  <span className="task-date">
                    {task.dueDate ? new Date(task.dueDate).toLocaleString('ru-RU') : 'Без срока'}
                  </span>
                  <span className={task.completed ? 'status-done' : 'status-pending'}>
                    {task.completed ? 'done' : 'todo'}
                  </span>
                </div>
                <div className="task-actions">
                  <button className="btn btn-sm btn-edit" onClick={() => handleEdit(task)}>
                    Ред.
                  </button>
                  <button className="btn btn-sm btn-delete" onClick={() => handleDelete(task.id)}>
                    Удалить
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="pagination">
            <button className="btn btn-sm" disabled={page <= 1} onClick={() => setPage((value) => value - 1)}>
              Назад
            </button>
            <span>
              Страница {page} из {totalPages}
            </span>
            <button
              className="btn btn-sm"
              disabled={page >= totalPages}
              onClick={() => setPage((value) => value + 1)}
            >
              Вперёд
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default TasksPage;
