import axiosInstance from './axios';

const ordonnanceApi = {

  // GET /api/ordonnances?patientId=&statut=
  getAll: (patientId, statut) =>
    axiosInstance.get('/api/ordonnances', {
      params: {
        ...(patientId && { patientId }),
        ...(statut && { statut })
      }
    }),

  // GET /api/ordonnances/{id}
  getById: (id) =>
    axiosInstance.get(`/api/ordonnances/${id}`),

  // GET /api/ordonnances/numero/{numero}
  // Vérification anti-doublon
  getByNumero: (numero) =>
    axiosInstance.get(`/api/ordonnances/numero/${numero}`),

  // POST /api/ordonnances
  create: (data) =>
    axiosInstance.post('/api/ordonnances', data),

  // PATCH /api/ordonnances/{id}/statut
  changerStatut: (id, statut) =>
    axiosInstance.patch(
      `/api/ordonnances/${id}/statut`,
      null,
      { params: { statut } }
    ),
};

export default ordonnanceApi;