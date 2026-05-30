import axiosInstance from './axios';

const venteApi = {

  // POST /api/ventes
  creerVente: (data) =>
    axiosInstance.post('/api/ventes', data),

  // GET /api/ventes/{id}
  getById: (id) =>
    axiosInstance.get(`/api/ventes/${id}`),

  // GET /api/ventes/{id}/ticket-pdf
  getTicketPdf: (id) =>
    axiosInstance.get(
      `/api/ventes/${id}/ticket-pdf`,
      {
        responseType: 'blob',
        headers: {
          // Token JWT explicitement ajouté
          Authorization: `Bearer ${
            localStorage.getItem('token')
          }`
        }
      }
    ),


  telechargerFichier: (blob, nomFichier) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', nomFichier);
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();

    // Délai avant suppression pour éviter ERR_FAILED
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }, 1000);
  },
};

export default venteApi;