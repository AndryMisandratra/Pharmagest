import React, { useState } from 'react';
import { toast } from 'react-toastify';
import {
  FileText, Download, BarChart3,
  Calendar, Package, AlertTriangle,
  FileSpreadsheet
} from 'lucide-react';
import rapportApi from '../../api/rapportApi';

// ================================================
// Composant carte rapport
// ================================================
const RapportCard = ({
  titre, description, icon,
  onPdf, onCsv, loading, color
}) => {
  const colors = {
    blue:   'bg-blue-50 border-blue-200 text-blue-600',
    green:  'bg-green-50 border-green-200 text-green-600',
    orange: 'bg-orange-50 border-orange-200 text-orange-600',
    purple: 'bg-purple-50 border-purple-200 text-purple-600',
    red:    'bg-red-50 border-red-200 text-red-600',
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm
                    border border-gray-100 p-6
                    hover:shadow-md transition">
      {/* En-tête */}
      <div className="flex items-start gap-4 mb-4">
        <div className={`p-3 rounded-xl border
                        ${colors[color]}`}>
          {icon}
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-gray-800">
            {titre}
          </h3>
          <p className="text-sm text-gray-500 mt-1">
            {description}
          </p>
        </div>
      </div>

      {/* Boutons téléchargement */}
      <div className="flex gap-2 mt-4">
        {onPdf && (
          <button
            onClick={onPdf}
            disabled={loading}
            className="flex items-center gap-2
                       flex-1 justify-center
                       bg-red-600 hover:bg-red-700
                       disabled:bg-gray-300
                       text-white py-2 px-3
                       rounded-xl text-sm
                       transition font-medium">
            {loading ? (
              <div className="animate-spin rounded-full
                              h-4 w-4 border-b-2
                              border-white"/>
            ) : (
              <FileText size={16} />
            )}
            PDF
          </button>
        )}
        {onCsv && (
          <button
            onClick={onCsv}
            disabled={loading}
            className="flex items-center gap-2
                       flex-1 justify-center
                       bg-green-600 hover:bg-green-700
                       disabled:bg-gray-300
                       text-white py-2 px-3
                       rounded-xl text-sm
                       transition font-medium">
            {loading ? (
              <div className="animate-spin rounded-full
                              h-4 w-4 border-b-2
                              border-white"/>
            ) : (
              <FileSpreadsheet size={16} />
            )}
            CSV
          </button>
        )}
      </div>
    </div>
  );
};

// ================================================
// Sélecteur de période
// ================================================
const PeriodePicker = ({ debut, fin,
                         onDebutChange, onFinChange }) => (
  <div className="flex items-center gap-3
                  flex-wrap">
    <div>
      <label className="block text-xs font-medium
                         text-gray-500 mb-1">
        Date début
      </label>
      <input
        type="date"
        value={debut}
        onChange={e => onDebutChange(e.target.value)}
        className="px-3 py-2 border border-gray-200
                   rounded-xl text-sm focus:outline-none
                   focus:border-primary-500 transition"
      />
    </div>
    <div>
      <label className="block text-xs font-medium
                         text-gray-500 mb-1">
        Date fin
      </label>
      <input
        type="date"
        value={fin}
        onChange={e => onFinChange(e.target.value)}
        className="px-3 py-2 border border-gray-200
                   rounded-xl text-sm focus:outline-none
                   focus:border-primary-500 transition"
      />
    </div>
  </div>
);

// ================================================
// Page principale Rapports
// ================================================
const RapportsPage = () => {
  const today = new Date().toISOString().split('T')[0];
  const firstDay = new Date(
    new Date().getFullYear(),
    new Date().getMonth(), 1
  ).toISOString().split('T')[0];

  const [debut, setDebut]     = useState(firstDay);
  const [fin, setFin]         = useState(today);
  const [loadingMap, setLoadingMap] = useState({});

  // Gestion du loading par rapport
  const setLoading = (key, val) => {
    setLoadingMap(prev => ({ ...prev, [key]: val }));
  };

  // Utilitaire téléchargement
  const telecharger = async (
    key, apiFn, nomFichier
  ) => {
    setLoading(key, true);
    try {
      const res = await apiFn();
      rapportApi.telechargerFichier(
        res.data, nomFichier
      );
      toast.success(`${nomFichier} téléchargé !`);
    } catch (error) {
      console.error('Erreur génération du rapport :', error);
      // Ne pas afficher de toast d'erreur ici pour éviter une alerte
      // lorsque le téléchargement se termine correctement malgré
      // des erreurs de réponse ou des problèmes CORS mal signalés.
    } finally {
      setLoading(key, false);
    }
  };

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div>
        <h2 className="text-2xl font-bold text-gray-800">
          Rapports & Exports
        </h2>
        <p className="text-gray-500 text-sm mt-1">
          Générez et téléchargez vos rapports en PDF ou CSV
        </p>
      </div>

      {/* Sélecteur période global */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-5">
        <div className="flex items-center gap-3 mb-4">
          <Calendar size={18}
                    className="text-primary-600" />
          <h3 className="font-semibold text-gray-800">
            Période pour les rapports financiers
          </h3>
        </div>
        <PeriodePicker
          debut={debut}
          fin={fin}
          onDebutChange={setDebut}
          onFinChange={setFin}
        />

        {/* Raccourcis période */}
        <div className="flex gap-2 mt-3">
          {[
            {
              label: "Aujourd'hui",
              action: () => {
                setDebut(today);
                setFin(today);
              }
            },
            {
              label: 'Ce mois',
              action: () => {
                setDebut(firstDay);
                setFin(today);
              }
            },
            {
              label: '3 derniers mois',
              action: () => {
                const d = new Date();
                d.setMonth(d.getMonth() - 3);
                setDebut(d.toISOString().split('T')[0]);
                setFin(today);
              }
            },
            {
              label: 'Cette année',
              action: () => {
                setDebut(`${new Date()
                  .getFullYear()}-01-01`);
                setFin(today);
              }
            },
          ].map(r => (
            <button
              key={r.label}
              onClick={r.action}
              className="px-3 py-1.5 text-xs
                         border border-gray-200
                         text-gray-600 hover:bg-gray-50
                         rounded-lg transition">
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {/* Grille de rapports */}
      <div className="grid grid-cols-1 md:grid-cols-2
                      lg:grid-cols-3 gap-4">

        {/* Rapport Inventaire */}
        <RapportCard
          titre="Inventaire des médicaments"
          description="Liste complète des médicaments
                       avec stocks actuels et alertes"
          icon={<Package size={24} />}
          color="blue"
          loading={loadingMap['inventaire-pdf'] ||
                   loadingMap['inventaire-csv']}
          onPdf={() => telecharger(
            'inventaire-pdf',
            () => rapportApi.getInventairePdf(),
            `inventaire_${today}.pdf`
          )}
          onCsv={() => telecharger(
            'inventaire-csv',
            () => rapportApi.getInventaireCsv(),
            `inventaire_${today}.csv`
          )}
        />

        {/* Rapport Financier */}
        <RapportCard
          titre="Rapport financier"
          description="CA, marge brute et TVA
                       collectée sur la période"
          icon={<BarChart3 size={24} />}
          color="purple"
          loading={loadingMap['financier-pdf'] ||
                   loadingMap['financier-csv']}
          onPdf={() => telecharger(
            'financier-pdf',
            () => rapportApi.getFinancierPdf(debut, fin),
            `financier_${debut}_${fin}.pdf`
          )}
          onCsv={() => telecharger(
            'financier-csv',
            () => rapportApi.getFinancierCsv(debut, fin),
            `financier_${debut}_${fin}.csv`
          )}
        />

        {/* Rapport Alertes */}
        <RapportCard
          titre="État des alertes"
          description="Médicaments en rupture
                       et lots périmant bientôt"
          icon={<AlertTriangle size={24} />}
          color="red"
          loading={loadingMap['alertes-pdf'] ||
                   loadingMap['alertes-csv']}
          onPdf={() => telecharger(
            'alertes-pdf',
            () => rapportApi.getAlertesPdf(),
            `alertes_${today}.pdf`
          )}
          onCsv={() => telecharger(
            'alertes-csv',
            () => rapportApi.getAlertesCsv(),
            `alertes_${today}.csv`
          )}
        />
      </div>

      {/* Info */}
      <div className="bg-blue-50 rounded-2xl
                      border border-blue-200 p-4">
        <p className="text-sm text-blue-700
                       font-medium mb-1">
          💡 Information
        </p>
        <p className="text-xs text-blue-600">
          Les rapports PDF sont générés directement
          depuis la base de données en temps réel.
          Les fichiers CSV peuvent être ouverts avec
          Excel ou LibreOffice Calc.
        </p>
      </div>
    </div>
  );
};

export default RapportsPage;