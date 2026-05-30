import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Search, Package, AlertTriangle,
  RefreshCw, History, Trash2, Calendar
} from 'lucide-react';
import stockApi from '../../api/stockApi';
import medicamentApi from '../../api/medicamentApi';
import { useAuth } from '../../context/AuthContext';

// Badge alerte péremption
const AlertePeremptionBadge = ({ alerte }) => {
  const styles = {
    NORMAL: 'bg-green-100 text-green-700',
    ORANGE: 'bg-orange-100 text-orange-700',
    ROUGE:  'bg-red-100 text-red-700',
    PERIME: 'bg-gray-100 text-gray-700',
  };
  const labels = {
    NORMAL: '✅ Normal',
    ORANGE: '🟠 < 90 jours',
    ROUGE:  '🔴 < 30 jours',
    PERIME: '⚫ Périmé',
  };
  return (
    <span className={`px-2 py-1 rounded-full
                     text-xs font-medium
                     ${styles[alerte]}`}>
      {labels[alerte]}
    </span>
  );
};

const StockPage = () => {
  const navigate = useNavigate();
  const { isPharmacien, canManageStock } = useAuth();

  const [lots, setLots]               = useState([]);
  const [medicaments, setMedicaments] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [search, setSearch]           = useState('');
  const [selectedMed, setSelectedMed] = useState('');
  const [filtreAlerte, setFiltreAlerte] = useState('TOUS');
  const [onglet, setOnglet]           = useState('lots');

  // Alertes
  const [alertesPeremption, setAlertesPeremption] =
      useState([]);

  useEffect(() => {
    fetchMedicaments();
    fetchAlertesPeremption();
  }, []);

  useEffect(() => {
    if (selectedMed) fetchLots(selectedMed);
  }, [selectedMed]);

  const fetchMedicaments = async () => {
    try {
      const res = await medicamentApi.getAll();
      setMedicaments(res.data);
    } catch {
      toast.error('Erreur chargement médicaments');
    }
  };

  const fetchLots = async (medicamentId) => {
    setLoading(true);
    try {
      const res = await stockApi
          .getLotsParMedicament(medicamentId);
      setLots(res.data);
    } catch {
      toast.error('Erreur chargement des lots');
    } finally {
      setLoading(false);
    }
  };

  const fetchAlertesPeremption = async () => {
    try {
      const res = await medicamentApi
          .getLotsPeremption(90);
      setAlertesPeremption(res.data);
    } catch {
      console.error('Erreur alertes péremption');
    }
  };

  const handleRetirerLot = async (lotId) => {
    const motif = window.prompt(
      'Motif du retrait (ex: périmé, cassé, perdu) :'
    );
    if (!motif) return;

    try {
      await stockApi.retirerLot(lotId, motif);
      toast.success('Lot retiré avec succès');
      if (selectedMed) fetchLots(selectedMed);
      fetchAlertesPeremption();
    } catch {
      toast.error('Erreur lors du retrait du lot');
    }
  };

  // Filtrage des médicaments pour la recherche
  const medicamentsFiltres = medicaments.filter(m =>
    m.denomination.toLowerCase()
      .includes(search.toLowerCase()) ||
    m.codeCip.includes(search) ||
    m.dci.toLowerCase().includes(search.toLowerCase())
  );

  // Filtrage des lots
  const lotsFiltres = lots.filter(lot => {
    if (filtreAlerte === 'TOUS') return true;
    return lot.alertePeremption === filtreAlerte;
  });

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Gestion du Stock
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            Gestion des lots et mouvements de stock
          </p>
        </div>
      </div>

      {/* Onglets */}
      <div className="flex gap-2 border-b border-gray-200">
        {[
          { id: 'lots',     label: '📦 Lots par médicament' },
          { id: 'alertes',  label: `🔴 Alertes péremption (${alertesPeremption.length})` },
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setOnglet(tab.id)}
            className={`px-4 py-2 text-sm font-medium
                       border-b-2 transition
                       ${onglet === tab.id
                         ? 'border-primary-600 text-primary-600'
                         : 'border-transparent text-gray-500 hover:text-gray-700'
                       }`}>
            {tab.label}
          </button>
        ))}
      </div>

      {/* Onglet LOTS */}
      {onglet === 'lots' && (
        <div className="space-y-4">

          {/* Sélection médicament */}
          <div className="bg-white rounded-2xl shadow-sm
                          border border-gray-100 p-4">
            <div className="flex flex-col md:flex-row
                            gap-4">
              <div className="flex-1 relative">
                <Search size={18}
                        className="absolute left-3
                                   top-1/2 -translate-y-1/2
                                   text-gray-400" />
                <input
                  type="text"
                  placeholder="Rechercher un médicament..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5
                             border border-gray-200
                             rounded-xl text-sm
                             focus:outline-none
                             focus:border-primary-500
                             transition"
                />
              </div>
              <select
                value={selectedMed}
                onChange={e => setSelectedMed(e.target.value)}
                className="flex-1 px-4 py-2.5 border
                           border-gray-200 rounded-xl
                           text-sm focus:outline-none
                           focus:border-primary-500
                           transition bg-white">
                <option value="">
                  Sélectionner un médicament...
                </option>
                {medicamentsFiltres.map(m => (
                  <option key={m.id} value={m.id}>
                    {m.denomination} — Stock: {m.stockQuantiteTotale}
                  </option>
                ))}
              </select>
              <select
                value={filtreAlerte}
                onChange={e => setFiltreAlerte(e.target.value)}
                className="px-4 py-2.5 border border-gray-200
                           rounded-xl text-sm
                           focus:outline-none
                           focus:border-primary-500
                           transition bg-white">
                <option value="TOUS">Tous les lots</option>
                <option value="ROUGE">🔴 &lt; 30 jours</option>
                <option value="ORANGE">🟠 &lt; 90 jours</option>
                <option value="NORMAL">✅ Normal</option>
              </select>
            </div>
          </div>

          {/* Tableau des lots */}
          <div className="bg-white rounded-2xl shadow-sm
                          border border-gray-100
                          overflow-hidden">
            {!selectedMed ? (
              <div className="flex flex-col items-center
                              justify-center h-48
                              text-gray-400">
                <Package size={48}
                         className="mb-3 opacity-30" />
                <p className="font-medium">
                  Sélectionnez un médicament
                </p>
                <p className="text-sm mt-1">
                  pour voir ses lots disponibles
                </p>
              </div>
            ) : loading ? (
              <div className="flex items-center
                              justify-center h-48">
                <div className="animate-spin rounded-full
                                h-10 w-10 border-b-2
                                border-primary-600"/>
              </div>
            ) : lotsFiltres.length === 0 ? (
              <div className="flex flex-col items-center
                              justify-center h-48
                              text-gray-400">
                <Package size={48}
                         className="mb-3 opacity-30" />
                <p className="font-medium">
                  Aucun lot disponible
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 border-b
                                    border-gray-100">
                    <tr>
                      {['N° Lot', 'Qté reçue',
                        'Qté restante', 'Fabrication',
                        'Péremption', 'Jours restants',
                        'Prix achat HT', 'Alerte',
                        'Statut', 'Actions'
                      ].map(h => (
                        <th key={h}
                            className="px-4 py-3 text-left
                                       text-xs font-semibold
                                       text-gray-500
                                       uppercase">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {lotsFiltres.map(lot => (
                      <tr key={lot.id}
                          className="hover:bg-gray-50
                                     transition">
                        <td className="px-4 py-3 font-mono
                                       text-xs font-medium
                                       text-gray-700">
                          {lot.numero}
                        </td>
                        <td className="px-4 py-3 text-gray-600">
                          {lot.quantiteRecue}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`font-bold
                            ${lot.quantiteRestante === 0
                              ? 'text-gray-400'
                              : lot.quantiteRestante <= 5
                              ? 'text-red-600'
                              : 'text-gray-800'}`}>
                            {lot.quantiteRestante}
                          </span>
                        </td>
                        <td className="px-4 py-3
                                       text-gray-500 text-xs">
                          {lot.dateFabrication
                            ? new Date(lot.dateFabrication)
                                .toLocaleDateString('fr-FR')
                            : '—'}
                        </td>
                        <td className="px-4 py-3
                                       text-gray-700 text-xs
                                       font-medium">
                          {new Date(lot.datePeremption)
                            .toLocaleDateString('fr-FR')}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`font-bold
                            ${lot.joursAvantPeremption <= 0
                              ? 'text-gray-500'
                              : lot.joursAvantPeremption <= 30
                              ? 'text-red-600'
                              : lot.joursAvantPeremption <= 90
                              ? 'text-orange-600'
                              : 'text-green-600'}`}>
                            {lot.joursAvantPeremption <= 0
                              ? 'Périmé'
                              : `${lot.joursAvantPeremption}j`}
                          </span>
                        </td>
                        <td className="px-4 py-3
                                       text-gray-600">
                          {Number(lot.prixAchatHt)
                            .toLocaleString('fr-FR')} Ar
                        </td>
                        <td className="px-4 py-3">
                          <AlertePeremptionBadge
                            alerte={lot.alertePeremption} />
                        </td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-1
                            rounded-full text-xs font-medium
                            ${lot.estActif
                              ? 'bg-green-100 text-green-700'
                              : 'bg-gray-100 text-gray-500'}`}>
                            {lot.estActif ? 'Actif' : 'Retiré'}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center
                                          gap-2">
                            {/* Historique */}
                            <button
                              onClick={() => navigate(
                                `/stock/historique/${
                                  lot.medicamentId}`
                              )}
                              className="p-1.5 text-blue-600
                                         hover:bg-blue-50
                                         rounded-lg
                                         transition"
                              title="Historique">
                              <History size={16} />
                            </button>
                            {/* Retirer */}
                            {isPharmacien() &&
                             lot.estActif && (
                              <button
                                onClick={() =>
                                  handleRetirerLot(lot.id)}
                                className="p-1.5 text-red-600
                                           hover:bg-red-50
                                           rounded-lg
                                           transition"
                                title="Retirer le lot">
                                <Trash2 size={16} />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Onglet ALERTES PÉREMPTION */}
      {onglet === 'alertes' && (
        <div className="space-y-4">
          <div className="bg-white rounded-2xl shadow-sm
                          border border-gray-100
                          overflow-hidden">
            {alertesPeremption.length === 0 ? (
              <div className="flex flex-col items-center
                              justify-center h-48
                              text-gray-400">
                <span className="text-5xl mb-3">✅</span>
                <p className="font-medium">
                  Aucun lot en péremption prochaine
                </p>
                <p className="text-sm mt-1">
                  Tous les lots expirent dans plus de 90 jours
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 border-b
                                    border-gray-100">
                    <tr>
                      {['Médicament', 'N° Lot',
                        'Qté restante', 'Date péremption',
                        'Jours restants', 'Alerte',
                        'Actions'
                      ].map(h => (
                        <th key={h}
                            className="px-4 py-3 text-left
                                       text-xs font-semibold
                                       text-gray-500
                                       uppercase">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {alertesPeremption.map(lot => (
                      <tr key={lot.id}
                          className={`hover:bg-gray-50
                            transition
                            ${lot.alertePeremption === 'ROUGE'
                              ? 'bg-red-50/30'
                              : 'bg-orange-50/30'}`}>
                        <td className="px-4 py-3">
                          <div className="font-medium
                                          text-gray-800">
                            {lot.medicamentDenomination}
                          </div>
                        </td>
                        <td className="px-4 py-3 font-mono
                                       text-xs">
                          {lot.numero}
                        </td>
                        <td className="px-4 py-3 font-bold
                                       text-gray-700">
                          {lot.quantiteRestante}
                        </td>
                        <td className="px-4 py-3 font-medium
                                       text-gray-700">
                          {new Date(lot.datePeremption)
                            .toLocaleDateString('fr-FR')}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`font-bold
                            ${lot.joursAvantPeremption <= 30
                              ? 'text-red-600'
                              : 'text-orange-600'}`}>
                            {lot.joursAvantPeremption} jours
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <AlertePeremptionBadge
                            alerte={lot.alertePeremption} />
                        </td>
                        <td className="px-4 py-3">
                          {isPharmacien() && (
                            <button
                              onClick={() =>
                                handleRetirerLot(lot.id)}
                              className="flex items-center
                                         gap-1 text-red-600
                                         hover:bg-red-50
                                         px-3 py-1.5
                                         rounded-lg
                                         transition text-xs">
                              <Trash2 size={14} />
                              Retirer
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default StockPage;