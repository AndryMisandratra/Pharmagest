import axiosInstance from './axios';

const patientApi = {

  // GET /api/patients?search=
  getAll: (search) =>
    axiosInstance.get('/api/patients', {
      params: search ? { search } : {}
    }),

  // GET /api/patients/{id}
  getById: (id) =>
    axiosInstance.get(`/api/patients/${id}`),

  // POST /api/patients
  create: (data) =>
    axiosInstance.post('/api/patients', data),

  // PUT /api/patients/{id}
  update: (id, data) =>
    axiosInstance.put(`/api/patients/${id}`, data),
};

export default patientApi;