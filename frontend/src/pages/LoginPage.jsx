import React, { useState } from 'react';
import { authService } from '../services/api';

function LoginPage({ onLogin }) {
  const [formData, setFormData] = useState({ login: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await authService.login(formData);
      onLogin(data);
    } catch (err) {
      setError('Неверный логин/email или пароль');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header"><h1>Вход</h1></div>
      {error && <div className="error-message">{error}</div>}
      <form className="form-container" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Username или Email"
          value={formData.login}
          onChange={(e) => setFormData({ ...formData, login: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Пароль"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          required
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Входим...' : 'Войти'}
        </button>
      </form>
    </div>
  );
}

export default LoginPage;
