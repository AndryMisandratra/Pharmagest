import axiosInstance from './axios';

const utilisateurApi = {

  // GET /api/utilisateurs — PHARMACIEN
  getAll: () =>
    axiosInstance.get('/api/utilisateurs'),

  // GET /api/utilisateurs/{id} — PHARMACIEN
  getById: (id) =>
    axiosInstance.get(`/api/utilisateurs/${id}`),

  // POST /api/utilisateurs — PHARMACIEN
  create: (data) =>
    axiosInstance.post('/api/utilisateurs', data),

  // PUT /api/utilisateurs/{id} — PHARMACIEN
  update: (id, data) =>
    axiosInstance.put(`/api/utilisateurs/${id}`, data),

  // PATCH /api/utilisateurs/{id}/actif — PHARMACIEN
  changerActif: (id, estActif) =>
    axiosInstance.patch(
      `/api/utilisateurs/${id}/actif`,
      null,
      { params: { estActif } }
    ),
};

export default utilisateurApi;
