import axiosInstance from './axios';

const authApi = {

  // POST /api/auth/login
  login: (email, password) =>
    axiosInstance.post('/api/auth/login', { email, password }),

  // GET /api/auth/me
  getMe: () =>
    axiosInstance.get('/api/auth/me'),
};

export default authApi;