import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  register: (fullName, email, password) =>
    api.post('/auth/register', { fullName, email, password }),
  login: (email, password) =>
    api.post('/auth/login', { email, password }),
};

export const slotAPI = {
  getAvailable: () =>
    api.get('/slots/available'),
};

export const appointmentAPI = {
  create: (slotId) =>
    api.post('/appointments', { slotId }),
  cancel: (appointmentId) =>
    api.patch(`/appointments/${appointmentId}/cancel`),
  getUserAppointments: () =>
    api.get('/appointments'),
};

export default api;
