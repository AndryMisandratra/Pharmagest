import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Eye, ClipboardList,
  RefreshCw, Send, CheckCircle, Pencil
} from 'lucide-react';
import commandeApi from '../../api/commandeApi';
import { useAuth } from '../../context/AuthContext';
import Modal from '../../components/Modal';
import CommandeFormPage from './CommandeFormPage';

const StatutBadge = ({ statut }) => {
  const config = {
    BROUILLON: {
      style: 'bg-gray-100 text-gray-700',
      label: '📝 Brouillon'
    },
    ENVOYEE: {
      style: 'bg-blue-100 text-blue-700',
      label: '📤 Envoyée'
    },
    EN_ATTENTE: {
      style: 'bg-yellow-100 text-yellow-700',
      label: '⏳ En attente'
    },
    CLOTUREE: {
      style: 'bg-green-100 text-green-700',
      label: '✅ Clôturée'
    },
    ANNULEE: {
      style: 'bg-red-100 text-red-700',
      label: '❌ Annulée'
    },
  };
  const c = config[statut] || config.BROUILLON;
  return (
    <span className={`px-2 py-1 rounded-full
                     text-xs font-medium ${c.style}`}>
      {c.label}
    </span>
  );
};

const CommandesPage = () => {
  const navigate = useNavigate();
  const { isPharmacien, canManageStock } = useAuth();

  const [commandes, setCommandes]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [filtreStatut, setFiltreStatut] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] =
      useState(false);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [initialSuggestions, setInitialSuggestions] = useState([]);

  useEffect(() => {
    fetchCommandes();
  }, [filtreStatut]);

  const fetchCommandes = async () => {
    setLoading(true);
    try {
      const statutFilter = filtreStatut === '' ? null : filtreStatut;
      const res = await commandeApi.getAll(
        null,
        statutFilter
      );
      setCommandes(res.data);
    } catch {
      toast.error('Erreur chargement commandes');
    } finally {
      setLoading(false);
    }
  };

  const fetchSuggestions = async () => {
    try {
      const res = await commandeApi.getSuggestions();
      setSuggestions(res.data);
      setShowSuggestions(true);
    } catch {
      toast.error('Erreur chargement suggestions');
    }
  };

  const handleEnvoyer = async (id) => {
    if (!window.confirm(
      'Confirmer l\'envoi de cette commande ?'
    )) return;
    try {
      await commandeApi.envoyer(id);
      toast.success('Commande envoyée !');
      fetchCommandes();
    } catch {
      toast.error('Erreur envoi commande');
    }
  };

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Commandes Fournisseurs
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {commandes.length} commande(s)
          </p>
        </div>
        <div className="flex items-center gap-3">
          {canManageStock() && (
            <button
              onClick={fetchSuggestions}
              className="flex items-center gap-2
                         border-2 border-primary-300
                         text-primary-600
                         hover:bg-primary-50
                         px-4 py-2 rounded-xl text-sm
                         transition font-medium">
              💡 Suggestions
            </button>
          )}
          {canManageStock() && (
            <button
              onClick={() => {
                setEditingId(null);
                setInitialSuggestions([]);
                setShowModal(true);
              }}
              className="flex items-center gap-2
                         bg-primary-600
                         hover:bg-primary-700
                         text-white px-4 py-2 rounded-xl
                         text-sm transition shadow-sm">
              <Plus size={18} />
              Nouvelle commande
            </button>
          )}
        </div>
      </div>

      {/* Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingId(null);
          setInitialSuggestions([]);
          fetchCommandes();
        }}
        title={editingId
          ? 'Modifier la commande'
          : 'Nouvelle commande'}
        size="xl"
      >
        <CommandeFormPage
          commandeId={editingId}
          initialSuggestions={initialSuggestions}
          onSuccess={() => {
            setShowModal(false);
            setEditingId(null);
            setInitialSuggestions([]);
            fetchCommandes();
          }}
          onCancel={() => {
            setShowModal(false);
            setEditingId(null);
            setInitialSuggestions([]);
          }}
        />
      </Modal>

      {/* Suggestions stock bas */}
      {showSuggestions && suggestions.length > 0 && (
        <div className="bg-yellow-50 rounded-2xl
                        border border-yellow-200 p-4">
          <div className="flex items-center
                          justify-between mb-3">
            <h3 className="font-semibold text-yellow-800
                           flex items-center gap-2">
              💡 Suggestions de réapprovisionnement
            </h3>
            <button
              onClick={() => setShowSuggestions(false)}
              className="text-yellow-600 text-sm
                         hover:underline">
              Fermer
            </button>
          </div>
          <div className="space-y-2">
            {suggestions.map((s, i) => (
              <div key={i}
                   className="flex items-center
                              justify-between p-3
                              bg-white rounded-xl
                              border border-yellow-100">
                <div>
                  <p className="font-medium text-sm
                                text-gray-800">
                    {s.medicamentDenomination}
                  </p>
                  <p className="text-xs text-gray-500">
                    Quantité suggérée : {s.quantiteCommandee}
                  </p>
                </div>
                <span className="text-xs bg-red-100
                                 text-red-700 px-2 py-1
                                 rounded-full">
                  Stock bas
                </span>
              </div>
            ))}
          </div>
          <button
            onClick={() => {
              setEditingId(null);
              setInitialSuggestions(suggestions);
              setShowModal(true);
            }}
            className="mt-3 w-full bg-yellow-600
                       hover:bg-yellow-700 text-white
                       py-2 rounded-xl text-sm
                       font-medium transition">
            Créer une commande avec ces suggestions
          </button>
        </div>
      )}

      {/* Filtres statut */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-4">
        <div className="flex flex-wrap gap-2
                        items-center">
          <span className="text-sm text-gray-500
                           font-medium">
            Filtrer :
          </span>
          {[
            { val: '',            label: 'Toutes' },
            { val: 'BROUILLON',   label: '📝 Brouillon' },
            { val: 'ENVOYEE',     label: '📤 Envoyée' },
            { val: 'EN_ATTENTE',  label: '⏳ En attente' },
            { val: 'CLOTUREE',    label: '✅ Clôturée' },
          ].map(f => (
            <button
              key={f.val}
              onClick={() => setFiltreStatut(f.val)}
              className={`px-3 py-1.5 rounded-xl
                         text-xs font-medium transition
                         border-2
                         ${filtreStatut === f.val
                           ? 'border-primary-500 bg-primary-50 text-primary-700'
                           : 'border-gray-200 text-gray-600 hover:border-gray-300'
                         }`}>
              {f.label}
            </button>
          ))}
          <button
            onClick={fetchCommandes}
            className="ml-auto flex items-center gap-1
                       border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-3 py-1.5 rounded-xl
                       text-xs transition">
            <RefreshCw size={14} />
            Actualiser
          </button>
        </div>
      </div>

      {/* Tableau */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100
                      overflow-hidden">
        {loading ? (
          <div className="flex items-center
                          justify-center h-48">
            <div className="animate-spin rounded-full
                            h-10 w-10 border-b-2
                            border-primary-600"/>
          </div>
        ) : commandes.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48
                          text-gray-400">
            <ClipboardList size={48}
                           className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucune commande
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {['Référence', 'Fournisseur',
                    'Créateur', 'Date commande',
                    'Montant HT', 'Statut', 'Actions'
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
                {commandes.map(c => (
                  <tr key={c.id}
                      className="hover:bg-gray-50
                                 transition">
                    <td className="px-4 py-3 font-mono
                                   text-xs font-medium
                                   text-gray-700">
                      {c.reference}
                    </td>
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {c.fournisseurRaisonSociale}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-600">
                      {c.createurPrenom} {c.createurNom}
                    </td>
                    <td className="px-4 py-3 text-xs
                                   text-gray-500">
                      {new Date(c.dateCommande)
                        .toLocaleDateString('fr-FR')}
                    </td>
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {c.montantTotalHt
                        ? `${Number(c.montantTotalHt)
                            .toLocaleString('fr-FR')} Ar`
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      <StatutBadge statut={c.statut} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center
                                      gap-2">
                        {/* Voir détail - uniquement CLOTUREE */}
                        {c.statut === 'CLOTUREE' && (
                          <button
                            onClick={() => navigate(
                              `/commandes/${c.id}`
                            )}
                            className="p-1.5 text-blue-600
                                       hover:bg-blue-50
                                       rounded-lg transition"
                            title="Voir détail">
                            <Eye size={16} />
                          </button>
                        )}
                        {/* Modifier */}
                        {c.statut === 'BROUILLON' && (
                          <button
                            onClick={() => navigate(
                              `/commandes/${c.id}/modifier`
                            )}
                            className="p-1.5 text-orange-600
                                       hover:bg-orange-50
                                       rounded-lg transition"
                            title="Modifier la commande">
                            <Pencil size={16} />
                          </button>
                        )}
                        {/* Envoyer */}
                        {isPharmacien() &&
                         c.statut === 'BROUILLON' && (
                          <button
                            onClick={() =>
                              handleEnvoyer(c.id)}
                            className="p-1.5 text-green-600
                                       hover:bg-green-50
                                       rounded-lg transition"
                            title="Envoyer la commande">
                            <Send size={16} />
                          </button>
                        )}
                        {/* Réceptionner */}
                        {canManageStock() &&
                         (c.statut === 'ENVOYEE' ||
                          c.statut === 'EN_ATTENTE') && (
                          <button
                            onClick={() => navigate(
                              `/commandes/${c.id}/reception`
                            )}
                            className="p-1.5 text-purple-600
                                       hover:bg-purple-50
                                       rounded-lg transition"
                            title="Réceptionner">
                            <CheckCircle size={16} />
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
  );
};

export default CommandesPage;