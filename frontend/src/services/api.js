import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const unwrapResponseData = (response) => response.data;

export const projectService = {
  getAll: () => api.get('/projects').then(unwrapResponseData),
  search: (filters = {}, page = 0, size = 20) => api.get('/projects/search', {
    params: {
      projectName: filters.projectName || undefined,
      taskTitle: filters.taskTitle || undefined,
      completed: filters.completed === '' ? undefined : filters.completed,
      dueFrom: filters.dueFrom || undefined,
      dueTo: filters.dueTo || undefined,
      page,
      size,
    },
  }).then(unwrapResponseData),
  getById: (id) => api.get(`/projects/${id}`).then(unwrapResponseData),
  create: (payload) => api.post('/projects', payload).then(unwrapResponseData),
  update: (id, payload) => api.put(`/projects/${id}`, payload).then(unwrapResponseData),
  delete: (id) => api.delete(`/projects/${id}`),
};

export const taskService = {
  getAll: () => api.get('/tasks').then(unwrapResponseData),
  getById: (id) => api.get(`/tasks/${id}`).then(unwrapResponseData),
  create: (payload) => api.post('/tasks', payload).then(unwrapResponseData),
  update: (id, payload) => api.put(`/tasks/${id}`, payload).then(unwrapResponseData),
  delete: (id) => api.delete(`/tasks/${id}`),
};

export const reminderService = {
  getAll: () => api.get('/reminders').then(unwrapResponseData),
  getById: (id) => api.get(`/reminders/${id}`).then(unwrapResponseData),
  create: (payload) => api.post('/reminders', payload).then(unwrapResponseData),
  update: (id, payload) => api.put(`/reminders/${id}`, payload).then(unwrapResponseData),
  delete: (id) => api.delete(`/reminders/${id}`),
};

export const authService = {
  login: (payload) => api.post('/auth/login', payload).then(unwrapResponseData),
  register: (payload) => api.post('/auth/register', payload),
};