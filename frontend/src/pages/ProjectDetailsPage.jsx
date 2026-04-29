import React, { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { projectService, reminderService, taskService } from '../services/api';

const formatDateTime = (value) => {
  if (!value) {
    return 'Не указано';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('ru-RU');
};

function ProjectDetailsPage() {
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [taskForm, setTaskForm] = useState({
    title: '',
    description: '',
    dueDate: '',
  });
  const [reminderForm, setReminderForm] = useState({
    taskId: '',
    reminderTime: '',
    message: '',
  });
  const [createType, setCreateType] = useState('task');

  const tasks = useMemo(() => project?.tasks ?? [], [project]);

  const loadProject = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await projectService.getById(id);
      setProject(data);
      setFormData({ name: data.name || '', description: data.description || '' });
    } catch (err) {
      console.error('Project details load error', err);
      setError('Не удалось загрузить проект');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProject();
  }, [id]);

  const handleUpdateProject = async (event) => {
    event.preventDefault();
    try {
      await projectService.update(id, {
        name: formData.name,
        description: formData.description,
        userId: project.userId,
        tasks: [],
      });
      setIsEditing(false);
      await loadProject();
    } catch (err) {
      console.error('Project update error', err);
      setError('Не удалось обновить проект');
    }
  };

  const handleCreateTask = async (event) => {
    event.preventDefault();
    try {
      await taskService.create({
        title: taskForm.title,
        description: taskForm.description,
        dueDate: taskForm.dueDate,
        completed: null,
        projectId: Number(id),
        categoryIds: [],
        reminders: [],
      });
      setTaskForm({ title: '', description: '', dueDate: '' });
      await loadProject();
    } catch (err) {
      console.error('Task create error', err);
      setError('Не удалось создать задачу');
    }
  };

  const handleCreateReminder = async (event) => {
    event.preventDefault();
    try {
      await reminderService.create({
        reminderTime: reminderForm.reminderTime,
        message: reminderForm.message,
        taskId: Number(reminderForm.taskId),
      });
      setReminderForm({ taskId: '', reminderTime: '', message: '' });
      await loadProject();
    } catch (err) {
      console.error('Reminder create error', err);
      setError('Не удалось создать напоминание');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div className="project-top-menu">
          <Link className="btn" to="/projects">← К проектам</Link>
          {!isEditing && project && (
            <button className="btn btn-primary" onClick={() => setIsEditing(true)}>
              Редактировать проект
            </button>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <p>Загрузка...</p>
      ) : !project ? (
        <p>Проект не найден</p>
      ) : (
        <>
          {isEditing ? (
            <form className="form-container" onSubmit={handleUpdateProject}>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
              <div className="card-actions">
                <button type="submit" className="btn btn-success">Сохранить</button>
                <button type="button" className="btn" onClick={() => setIsEditing(false)}>
                  Отмена
                </button>
              </div>
            </form>
          ) : (
            <div className="project-overview">
              <h1>{project.name}</h1>
              <p className="card-desc">{project.description || 'Описание отсутствует'}</p>
            </div>
          )}

          <form className="form-container">
            <h3>Создание</h3>
            <select value={createType} onChange={(e) => setCreateType(e.target.value)}>
              <option value="task">Создать задачу</option>
              <option value="reminder">Создать напоминание</option>
            </select>
          </form>

          {createType === 'task' ? (
            <form className="form-container" onSubmit={handleCreateTask}>
              <h3>Новая задача</h3>
              <input
                type="text"
                placeholder="Название задачи"
                value={taskForm.title}
                onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
                required
              />
              <textarea
                placeholder="Описание задачи"
                value={taskForm.description}
                onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })}
              />
              <input
                type="datetime-local"
                value={taskForm.dueDate}
                onChange={(e) => setTaskForm({ ...taskForm, dueDate: e.target.value })}
                required
              />
              <button type="submit" className="btn btn-success">Добавить задачу</button>
            </form>
          ) : (
            <form className="form-container" onSubmit={handleCreateReminder}>
              <h3>Новое напоминание</h3>
              <select
                value={reminderForm.taskId}
                onChange={(e) => setReminderForm({ ...reminderForm, taskId: e.target.value })}
                required
              >
                <option value="">Выбери задачу</option>
                {tasks.map((task) => (
                  <option key={task.id} value={task.id}>{task.title}</option>
                ))}
              </select>
              <input
                type="datetime-local"
                value={reminderForm.reminderTime}
                onChange={(e) => setReminderForm({ ...reminderForm, reminderTime: e.target.value })}
                required
              />
              <input
                type="text"
                placeholder="Текст напоминания"
                value={reminderForm.message}
                onChange={(e) => setReminderForm({ ...reminderForm, message: e.target.value })}
                required
              />
              <button type="submit" className="btn btn-success">Добавить напоминание</button>
            </form>
          )}

          <div className="page-header">
            <h2>Задачи проекта</h2>
          </div>

          {tasks.length === 0 ? (
            <p>У проекта пока нет задач.</p>
          ) : (
            <div className="cards-grid">
              {tasks.map((task) => (
                <article key={task.id} className="card">
                  <h3>{task.title}</h3>
                  <p className="card-desc">{task.description || 'Описание отсутствует'}</p>
                  <p><strong>Дедлайн:</strong> {formatDateTime(task.dueDate)}</p>
                  <p><strong>Статус:</strong> {task.completed ? 'Выполнена' : 'Активна'}</p>
                  <p><strong>Категории:</strong> {task.categories?.length ? task.categories.join(', ') : 'Нет'}</p>
                  <div className="project-reminders">
                    <strong>Напоминания:</strong>
                    {task.reminders?.length ? (
                      <ul>
                        {task.reminders.map((reminder) => (
                          <li key={reminder.id}>
                            {formatDateTime(reminder.reminderTime)} - {reminder.message}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p>Нет напоминаний</p>
                    )}
                  </div>
                </article>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default ProjectDetailsPage;
