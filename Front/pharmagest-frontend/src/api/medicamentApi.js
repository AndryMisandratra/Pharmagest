import axiosInstance from './axios';

const medicamentApi = {

  // GET /api/medicaments
  getAll: (search) =>
    axiosInstance.get('/api/medicaments', {
      params: search ? { search } : {}
    }),

  // GET /api/medicaments/{id}
  getById: (id) =>
    axiosInstance.get(`/api/medicaments/${id}`),

  // GET /api/medicaments/code-cip/{cip}
  getByCip: (cip) =>
    axiosInstance.get(`/api/medicaments/code-cip/${cip}`),

  // POST /api/medicaments — PHARMACIEN
  create: (data) =>
    axiosInstance.post('/api/medicaments', data),

  // PUT /api/medicaments/{id} — PHARMACIEN
  update: (id, data) =>
    axiosInstance.put(`/api/medicaments/${id}`, data),

  // PATCH /api/medicaments/{id}/disponibilite — PHARMACIEN
  changerDisponibilite: (id, disponible) =>
    axiosInstance.patch(
      `/api/medicaments/${id}/disponibilite`,
      null,
      { params: { disponible } }
    ),

  // GET /api/medicaments/alertes/stock-bas
  getStockBas: () =>
    axiosInstance.get('/api/medicaments/alertes/stock-bas'),

  // GET /api/medicaments/alertes/stock-faible
  getStockFaible: () =>
    axiosInstance.get('/api/medicaments/alertes/stock-faible'),

  // GET /api/medicaments/alertes/peremption?jours=90
  getLotsPeremption: (jours = 90) =>
    axiosInstance.get(
      '/api/medicaments/alertes/peremption',
      { params: { jours } }
    ),

  // GET /api/categories
  getCategories: () =>
    axiosInstance.get('/api/categories'),
};

export default medicamentApi;