import axiosInstance from './axios';

const fournisseurApi = {

  // GET /api/fournisseurs
  getAll: () =>
    axiosInstance.get('/api/fournisseurs'),

  // GET /api/fournisseurs/{id}
  getById: (id) =>
    axiosInstance.get(`/api/fournisseurs/${id}`),

  // POST /api/fournisseurs — PHARMACIEN
  create: (data) =>
    axiosInstance.post('/api/fournisseurs', data),

  // PUT /api/fournisseurs/{id} — PHARMACIEN
  update: (id, data) =>
    axiosInstance.put(`/api/fournisseurs/${id}`, data),
};

export default fournisseurApi;