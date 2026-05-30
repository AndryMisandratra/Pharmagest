import axiosInstance from './axios';

const commandeApi = {

  // GET /api/commandes?statut=&fournisseurId=
  getAll: (fournisseurId, statut) =>
    axiosInstance.get('/api/commandes', {
      params: {
        ...(fournisseurId && { fournisseurId }),
        ...(statut && { statut })
      }
    }),

  // POST /api/commandes
  create: (data) =>
    axiosInstance.post('/api/commandes', data),

  // PUT /api/commandes/{id}
  modifier: (id, data) =>
    axiosInstance.put(`/api/commandes/${id}`, data),

  // PATCH /api/commandes/{id}/envoyer — PHARMACIEN
  envoyer: (id) =>
    axiosInstance.patch(`/api/commandes/${id}/envoyer`),

  // GET /api/commandes/{id}
  getById: (id) =>
    axiosInstance.get(`/api/commandes/${id}`),

  // POST /api/commandes/{id}/reception
  receptionner: (id, data) =>
    axiosInstance.post(
      `/api/commandes/${id}/reception`, data),

  // GET /api/commandes/suggestions
  getSuggestions: () =>
    axiosInstance.get('/api/commandes/suggestions'),
};

export default commandeApi;