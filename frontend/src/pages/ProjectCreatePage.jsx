import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { projectService } from '../services/api';

function ProjectCreatePage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      setSaving(true);
      setError('');
      await projectService.create({
        name: formData.name,
        description: formData.description,
        userId: Number(localStorage.getItem('userId')),
        tasks: [],
      });
      navigate('/projects');
    } catch (err) {
      console.error('Create project error', err);
      setError('Не удалось создать проект');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Создание проекта</h1>
        <Link className="btn" to="/projects">← К проектам</Link>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form className="form-container" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Название"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
        />
        <textarea
          placeholder="Описание"
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
        />
        <div className="card-actions">
          <button className="btn btn-success" type="submit" disabled={saving}>
            {saving ? 'Сохранение...' : 'Создать'}
          </button>
          <Link className="btn" to="/projects">Отмена</Link>
        </div>
      </form>
    </div>
  );
}

export default ProjectCreatePage;
