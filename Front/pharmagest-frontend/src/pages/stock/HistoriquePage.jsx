import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ArrowLeft, History } from 'lucide-react';
import stockApi from '../../api/stockApi';
import medicamentApi from '../../api/medicamentApi';

const typeMouvementStyle = {
  ENTREE_RECEPTION: {
    bg:    'bg-green-100',
    text:  'text-green-700',
    label: '📥 Réception',
  },
  ENTREE_RETOUR: {
    bg:    'bg-blue-100',
    text:  'text-blue-700',
    label: '↩️ Retour client',
  },
  SORTIE_VENTE: {
    bg:    'bg-orange-100',
    text:  'text-orange-700',
    label: '🛒 Vente',
  },
  SORTIE_PERIME: {
    bg:    'bg-red-100',
    text:  'text-red-700',
    label: '⛔ Périmé',
  },
  SORTIE_PERTE: {
    bg:    'bg-red-100',
    text:  'text-red-700',
    label: '💸 Perte',
  },
  INVENTAIRE: {
    bg:    'bg-purple-100',
    text:  'text-purple-700',
    label: '📋 Inventaire',
  },
};

const HistoriquePage = () => {
  const navigate = useNavigate();
  const { medicamentId } = useParams();

  const [mouvements, setMouvements]   = useState([]);
  const [medicament, setMedicament]   = useState(null);
  const [loading, setLoading]         = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [mvtRes, medRes] = await Promise.all([
          stockApi.getHistorique(medicamentId),
          medicamentApi.getById(medicamentId),
        ]);
        setMouvements(mvtRes.data);
        setMedicament(medRes.data);
      } catch {
        toast.error('Erreur chargement historique');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [medicamentId]);

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/stock')}
          className="p-2 hover:bg-gray-100 rounded-xl
                     transition">
          <ArrowLeft size={20} className="text-gray-600" />
        </button>
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Historique des mouvements
          </h2>
          {medicament && (
            <p className="text-gray-500 text-sm mt-1">
              {medicament.denomination} —
              Stock actuel : {medicament.stockQuantiteTotale}
            </p>
          )}
        </div>
      </div>

      {/* Tableau historique */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="flex items-center
                          justify-center h-48">
            <div className="animate-spin rounded-full
                            h-10 w-10 border-b-2
                            border-primary-600"/>
          </div>
        ) : mouvements.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48 text-gray-400">
            <History size={48} className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucun mouvement enregistré
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {['Date', 'Type', 'Quantité',
                    'N° Lot', 'Motif', 'Effectué par'
                  ].map(h => (
                    <th key={h}
                        className="px-4 py-3 text-left
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {mouvements.map(m => {
                  const style =
                    typeMouvementStyle[m.type] ||
                    typeMouvementStyle.INVENTAIRE;
                  const isEntree =
                    m.type.startsWith('ENTREE');
                  return (
                    <tr key={m.id}
                        className="hover:bg-gray-50
                                   transition">
                      <td className="px-4 py-3 text-xs
                                     text-gray-500">
                        {new Date(m.effectueLe)
                          .toLocaleString('fr-FR')}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1
                          rounded-full text-xs font-medium
                          ${style.bg} ${style.text}`}>
                          {style.label}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`font-bold
                          ${isEntree
                            ? 'text-green-600'
                            : 'text-red-600'}`}>
                          {isEntree ? '+' : '-'}
                          {m.quantite}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-mono
                                     text-xs text-gray-500">
                        {m.lotNumero || '—'}
                      </td>
                      <td className="px-4 py-3 text-gray-600
                                     max-w-xs truncate">
                        {m.motif || '—'}
                      </td>
                      <td className="px-4 py-3 text-gray-600">
                        {m.effectueParPrenom} {m.effectueParNom}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default HistoriquePage;