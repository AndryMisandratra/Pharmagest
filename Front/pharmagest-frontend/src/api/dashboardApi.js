import axiosInstance from './axios';

const dashboardApi = {

  // GET /api/dashboard/resume
  getResume: () =>
    axiosInstance.get('/api/dashboard/resume'),

  // GET /api/dashboard/alertes
  getAlertes: () =>
    axiosInstance.get('/api/dashboard/alertes'),

  // POST /api/dashboard/alertes/tester — PHARMACIEN
  testerAlertes: () =>
    axiosInstance.post('/api/dashboard/alertes/tester'),
};

export default dashboardApi;