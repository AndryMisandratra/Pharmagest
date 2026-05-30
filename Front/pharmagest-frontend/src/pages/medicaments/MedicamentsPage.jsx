import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Search, Edit, Archive,
  RefreshCw, Pill, AlertTriangle,
  CheckCircle, XCircle, Eye
} from 'lucide-react';
import medicamentApi from '../../api/medicamentApi';
import { useAuth } from '../../context/AuthContext';
import Modal from '../../components/Modal';
import MedicamentFormPage from './MedicamentFormPage';

// Badge niveau alerte
const AlerteBadge = ({ niveau }) => {
  const styles = {
    NORMAL: 'bg-green-100 text-green-700',
    ORANGE: 'bg-orange-100 text-orange-700',
    ROUGE:  'bg-red-100 text-red-700',
  };
  const labels = {
    NORMAL: '✅ Normal',
    ORANGE: '🟠 Stock faible',
    ROUGE:  '🔴 Rupture',
  };
  return (
    <span className={`px-2 py-1 rounded-full text-xs
                     font-medium ${styles[niveau]}`}>
      {labels[niveau]}
    </span>
  );
};

// Badge statut légal
const StatutBadge = ({ statut }) => {
  const styles = {
    LIBRE_ACCES:         'bg-blue-100 text-blue-700',
    CONSEIL:             'bg-yellow-100 text-yellow-700',
    ORDONNANCE:          'bg-orange-100 text-orange-700',
    ORDONNANCE_SECURISEE:'bg-red-100 text-red-700',
  };
  const labels = {
    LIBRE_ACCES:          'Libre accès',
    CONSEIL:              'Conseil',
    ORDONNANCE:           'Ordonnance',
    ORDONNANCE_SECURISEE: 'Ordo. sécurisée',
  };
  return (
    <span className={`px-2 py-1 rounded-full text-xs
                     font-medium ${styles[statut]}`}>
      {labels[statut]}
    </span>
  );
};

const MedicamentsPage = () => {
  const navigate = useNavigate();
  const { isPharmacien } = useAuth();

  const [medicaments, setMedicaments] = useState([]);
  const [loading, setLoading]         = useState(true);
  const [search, setSearch]           = useState('');
  const [filtreAlerte, setFiltreAlerte] = useState('TOUS');
  const [filtreStatut, setFiltreStatut] = useState('TOUS');

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const fetchMedicaments = async () => {
    setLoading(true);
    try {
      const response = await medicamentApi.getAll(
        search || null
      );
      setMedicaments(response.data);
    } catch (error) {
      toast.error('Erreur chargement médicaments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMedicaments();
  }, []);

  // Recherche avec délai (debounce)
  useEffect(() => {
    const timer = setTimeout(() => {
      fetchMedicaments();
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  const handleArchiver = async (id, estDisponible) => {
    const action = estDisponible ? 'archiver' : 'réactiver';
    if (!window.confirm(
      `Voulez-vous ${action} ce médicament ?`)) return;

    try {
      await medicamentApi.changerDisponibilite(
        id, !estDisponible
      );
      toast.success(
        `Médicament ${action === 'archiver'
          ? 'archivé' : 'réactivé'} avec succès`
      );
      fetchMedicaments();
    } catch (error) {
      toast.error(`Erreur lors de l'action`);
    }
  };

  // Filtrage côté client
  const medicamentsFiltres = medicaments.filter(m => {
    if (filtreAlerte !== 'TOUS' &&
        m.niveauAlerte !== filtreAlerte) return false;
    if (filtreStatut !== 'TOUS' &&
        m.statutLegal !== filtreStatut) return false;
    return true;
  });

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center
                      justify-between">
        <div>
          <h2 className="text-2xl font-bold
                         text-gray-800">
            Catalogue Médicaments
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {medicaments.length} médicament(s) au total
          </p>
        </div>
        {isPharmacien() && (
          <button
            onClick={() => {
              setEditingId(null);
              setShowModal(true);
            }}
            className="flex items-center gap-2
                       bg-primary-600 hover:bg-primary-700
                       text-white px-4 py-2 rounded-xl
                       text-sm transition shadow-sm">
            <Plus size={18} />
            Nouveau médicament
          </button>
        )}
      </div>

      {/* Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingId(null);
          fetchMedicaments();
        }}
        title={editingId
          ? 'Modifier le médicament'
          : 'Nouveau médicament'}
        size="xl"
      >
        <MedicamentFormPage
          medicamentId={editingId}
          onSuccess={() => {
            setShowModal(false);
            setEditingId(null);
            fetchMedicaments();
          }}
          onCancel={() => {
            setShowModal(false);
            setEditingId(null);
          }}
        />
      </Modal>

      {/* Barre de recherche + filtres */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-4">
        <div className="flex flex-col md:flex-row
                        gap-4">

          {/* Recherche */}
          <div className="flex-1 relative">
            <Search size={18}
                    className="absolute left-3 top-1/2
                               -translate-y-1/2
                               text-gray-400" />
            <input
              type="text"
              placeholder="Rechercher par nom, DCI ou code CIP..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5
                         border border-gray-200 rounded-xl
                         text-sm focus:outline-none
                         focus:border-primary-500
                         transition"
            />
          </div>

          {/* Filtre alerte */}
          <select
            value={filtreAlerte}
            onChange={e => setFiltreAlerte(e.target.value)}
            className="px-4 py-2.5 border border-gray-200
                       rounded-xl text-sm focus:outline-none
                       focus:border-primary-500 transition
                       bg-white">
            <option value="TOUS">Toutes les alertes</option>
            <option value="ROUGE">🔴 Rupture</option>
            <option value="ORANGE">🟠 Stock faible</option>
            <option value="NORMAL">✅ Normal</option>
          </select>

          {/* Filtre statut légal */}
          <select
            value={filtreStatut}
            onChange={e => setFiltreStatut(e.target.value)}
            className="px-4 py-2.5 border border-gray-200
                       rounded-xl text-sm focus:outline-none
                       focus:border-primary-500 transition
                       bg-white">
            <option value="TOUS">Tous les statuts</option>
            <option value="LIBRE_ACCES">Libre accès</option>
            <option value="CONSEIL">Conseil</option>
            <option value="ORDONNANCE">Ordonnance</option>
            <option value="ORDONNANCE_SECURISEE">
              Ordo. sécurisée
            </option>
          </select>

          {/* Bouton rafraîchir */}
          <button
            onClick={fetchMedicaments}
            className="flex items-center gap-2
                       border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-4 py-2.5 rounded-xl text-sm
                       transition">
            <RefreshCw size={16} />
            Actualiser
          </button>
        </div>
      </div>

      {/* Tableau */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center
                          h-48">
            <div className="animate-spin rounded-full
                            h-10 w-10 border-b-2
                            border-primary-600"/>
          </div>
        ) : medicamentsFiltres.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48 text-gray-400">
            <Pill size={48} className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucun médicament trouvé
            </p>
            <p className="text-sm mt-1">
              Modifiez vos critères de recherche
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {[
                    'Code CIP', 'Dénomination', 'DCI',
                    'Forme', 'Stock', 'Statut légal',
                    'Prix TTC', 'Alerte', 'Disponible',
                    'Actions'
                  ].map(h => (
                    <th key={h}
                        className="px-4 py-3 text-left
                                   text-xs font-semibold
                                   text-gray-500
                                   uppercase tracking-wider">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {medicamentsFiltres.map(m => (
                  <tr key={m.id}
                      className={`hover:bg-gray-50
                                 transition
                                 ${!m.estDisponible
                                   ? 'opacity-50' : ''}`}>
                    <td className="px-4 py-3 font-mono
                                   text-xs text-gray-500">
                      {m.codeCip}
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-medium
                                      text-gray-800">
                        {m.denomination}
                      </div>
                      <div className="text-xs text-gray-400">
                        {m.fabricant}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {m.dci}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {m.formeGalenique}
                      {m.dosage && (
                        <span className="text-xs
                                         text-gray-400 ml-1">
                          {m.dosage}
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`font-bold
                                       ${m.stockQuantiteTotale
                                         <= m.stockSeuilAlerte
                                         ? 'text-red-600'
                                         : 'text-gray-800'}`}>
                        {m.stockQuantiteTotale}
                      </span>
                      <span className="text-xs text-gray-400
                                       ml-1">
                        / {m.stockSeuilAlerte}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <StatutBadge statut={m.statutLegal} />
                    </td>
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {Number(m.prixVenteTtc)
                        .toLocaleString('fr-FR')} Ar
                    </td>
                    <td className="px-4 py-3">
                      <AlerteBadge niveau={m.niveauAlerte} />
                    </td>
                    <td className="px-4 py-3">
                      {m.estDisponible ? (
                        <span className="flex items-center
                                         gap-1 text-green-600
                                         text-xs">
                          <CheckCircle size={14} />
                          Actif
                        </span>
                      ) : (
                        <span className="flex items-center
                                         gap-1 text-gray-400
                                         text-xs">
                          <XCircle size={14} />
                          Archivé
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        {/* Modifier */}
                        {isPharmacien() && (
                          <button
                            onClick={() => {
                              setEditingId(m.id);
                              setShowModal(true);
                            }}
                            className="p-1.5 text-orange-600
                                       hover:bg-orange-50
                                       rounded-lg transition"
                            title="Modifier">
                            <Edit size={16} />
                          </button>
                        )}
                        {/* Archiver/Réactiver */}
                        {isPharmacien() && (
                          <button
                            onClick={() => handleArchiver(
                              m.id, m.estDisponible
                            )}
                            className={`p-1.5 rounded-lg
                                       transition
                                       ${m.estDisponible
                                         ? 'text-red-600 hover:bg-red-50'
                                         : 'text-green-600 hover:bg-green-50'
                                       }`}
                            title={m.estDisponible
                              ? 'Archiver'
                              : 'Réactiver'}>
                            <Archive size={16} />
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

      {/* Résumé bas de page */}
      <div className="flex items-center justify-between
                      text-sm text-gray-500">
        <span>
          {medicamentsFiltres.length} résultat(s) affiché(s)
        </span>
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full
                             bg-red-400 inline-block"/>
            Rupture : {medicaments.filter(
              m => m.niveauAlerte === 'ROUGE').length}
          </span>
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full
                             bg-orange-400 inline-block"/>
            Faible : {medicaments.filter(
              m => m.niveauAlerte === 'ORANGE').length}
          </span>
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full
                             bg-green-400 inline-block"/>
            Normal : {medicaments.filter(
              m => m.niveauAlerte === 'NORMAL').length}
          </span>
        </div>
      </div>
    </div>
  );
};

export default MedicamentsPage;