import axiosInstance from './axios';

const stockApi = {

  // GET /api/lots?medicamentId=
  getLotsParMedicament: (medicamentId) =>
    axiosInstance.get('/api/lots', {
      params: { medicamentId }
    }),

  // POST /api/lots
  creerLot: (data) =>
    axiosInstance.post('/api/lots', data),

  // PATCH /api/lots/{id}/retrait
  retirerLot: (id, motif) =>
    axiosInstance.patch(
      `/api/lots/${id}/retrait`,
      null,
      { params: { motif } }
    ),

  // POST /api/mouvements
  enregistrerMouvement: (data) =>
    axiosInstance.post('/api/mouvements', data),

  // GET /api/mouvements?medicamentId=
  getHistorique: (medicamentId) =>
    axiosInstance.get('/api/mouvements', {
      params: { medicamentId }
    }),
};

export default stockApi;