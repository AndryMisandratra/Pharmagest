import axiosInstance from './axios';

const rapportApi = {

  // ===== PDF =====

  // GET /api/rapports/inventaire
  getInventairePdf: () =>
    axiosInstance.get('/api/rapports/inventaire', {
      responseType: 'blob'
    }),

  // GET /api/rapports/alertes
  getAlertesPdf: () =>
    axiosInstance.get('/api/rapports/alertes', {
      responseType: 'blob'
    }),

  // ===== CSV =====

  // GET /api/rapports/inventaire/csv
  getInventaireCsv: () =>
    axiosInstance.get('/api/rapports/inventaire/csv', {
      responseType: 'blob'
    }),

  // GET /api/rapports/financier?debut=&fin=
  getFinancierPdf: (debut, fin) =>
    axiosInstance.get('/api/rapports/financier', {
      params: { debut, fin },
      responseType: 'blob'
    }),

  // GET /api/rapports/financier/csv?debut=&fin=
  getFinancierCsv: (debut, fin) =>
    axiosInstance.get('/api/rapports/financier/csv', {
      params: { debut, fin },
      responseType: 'blob'
    }),

  // GET /api/rapports/alertes/csv
  getAlertesCsv: () =>
    axiosInstance.get('/api/rapports/alertes/csv', {
      responseType: 'blob'
    }),

  // Utilitaire pour télécharger un blob PDF/CSV
  telechargerFichier: (blob, nomFichier) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', nomFichier);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};

export default rapportApi;