import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Edit, UserCog,
  RefreshCw, CheckCircle, XCircle,
  Shield
} from 'lucide-react';
import utilisateurApi from '../../api/utilisateurApi';
import { useAuth } from '../../context/AuthContext';
import Modal from '../../components/Modal';
import UtilisateurFormPage from './UtilisateurFormPage';

const RoleBadge = ({ role }) => {
  const config = {
    ROLE_PHARMACIEN: {
      style: 'bg-purple-100 text-purple-700',
      label: '👨‍⚕️ Pharmacien'
    },
    ROLE_PREPARATEUR: {
      style: 'bg-blue-100 text-blue-700',
      label: '🧪 Préparateur'
    },
    ROLE_CAISSIER: {
      style: 'bg-green-100 text-green-700',
      label: '💰 Caissier'
    },
    ROLE_ADMIN: {
      style: 'bg-red-100 text-red-700',
      label: '⚙️ Admin'
    },
  };
  const c = config[role] || config.ROLE_CAISSIER;
  return (
    <span className={`px-2 py-1 rounded-full
                     text-xs font-medium ${c.style}`}>
      {c.label}
    </span>
  );
};

const UtilisateursPage = () => {
  const navigate = useNavigate();
  const { isPharmacien } = useAuth();

  const [utilisateurs, setUtilisateurs] = useState([]);
  const [loading, setLoading]           = useState(true);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    fetchUtilisateurs();
  }, []);

  const fetchUtilisateurs = async () => {
    setLoading(true);
    try {
      const res = await utilisateurApi.getAll();
      setUtilisateurs(res.data);
    } catch {
      toast.error('Erreur chargement utilisateurs');
    } finally {
      setLoading(false);
    }
  };

  const handleChangerActif = async (id, estActif) => {
    const action = estActif ? 'désactiver' : 'activer';
    if (!window.confirm(
      `Voulez-vous ${action} ce compte ?`
    )) return;

    try {
      await utilisateurApi.changerActif(id, !estActif);
      toast.success(
        `Compte ${estActif ? 'désactivé' : 'activé'} !`
      );
      fetchUtilisateurs();
    } catch {
      toast.error('Erreur modification compte');
    }
  };

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Gestion des utilisateurs
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {utilisateurs.length} compte(s) dans le système
          </p>
        </div>
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
          Nouveau compte
        </button>
      </div>

      {/* Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingId(null);
          fetchUtilisateurs();
        }}
        title={editingId
          ? 'Modifier le compte'
          : 'Nouveau compte utilisateur'}
        size="lg"
      >
        <UtilisateurFormPage
          utilisateurId={editingId}
          onSuccess={() => {
            setShowModal(false);
            setEditingId(null);
            fetchUtilisateurs();
          }}
          onCancel={() => {
            setShowModal(false);
            setEditingId(null);
          }}
        />
      </Modal>

      {/* Info sécurité */}
      <div className="bg-purple-50 rounded-2xl
                      border border-purple-200 p-4
                      flex items-start gap-3">
        <Shield size={20}
                className="text-purple-600 mt-0.5
                           flex-shrink-0" />
        <div>
          <p className="text-sm font-medium
                        text-purple-800">
            Accès réservé au Pharmacien
          </p>
          <p className="text-xs text-purple-600 mt-1">
            Seul le pharmacien peut créer, modifier
            ou désactiver des comptes utilisateurs.
            Les mots de passe sont hashés avec BCrypt.
          </p>
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
        ) : utilisateurs.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48
                          text-gray-400">
            <UserCog size={48}
                     className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucun utilisateur
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {['Nom', 'Prénom', 'Email',
                    'Rôle', 'Statut', 'Créé le',
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
                {utilisateurs.map(u => (
                  <tr key={u.id}
                      className={`hover:bg-gray-50
                                 transition
                                 ${!u.estActif
                                   ? 'opacity-60' : ''}`}>
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {u.nom}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-600">
                      {u.prenom}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-500 text-xs">
                      {u.email}
                    </td>
                    <td className="px-4 py-3">
                      <RoleBadge role={u.role} />
                    </td>
                    <td className="px-4 py-3">
                      {u.estActif ? (
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
                          Désactivé
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-xs
                                   text-gray-400">
                      {u.creeLe
                        ? new Date(u.creeLe)
                            .toLocaleDateString('fr-FR')
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center
                                      gap-2">
                        {/* Modifier */}
                        <button
                          onClick={() => {
                            setEditingId(u.id);
                            setShowModal(true);
                          }}
                          className="p-1.5 text-orange-600
                                     hover:bg-orange-50
                                     rounded-lg transition"
                          title="Modifier">
                          <Edit size={16} />
                        </button>
                        {/* Activer/Désactiver */}
                        <button
                          onClick={() =>
                            handleChangerActif(
                              u.id, u.estActif
                            )}
                          className={`p-1.5 rounded-lg
                                     transition
                                     ${u.estActif
                                       ? 'text-red-600 hover:bg-red-50'
                                       : 'text-green-600 hover:bg-green-50'
                                     }`}
                          title={u.estActif
                            ? 'Désactiver'
                            : 'Activer'}>
                          {u.estActif
                            ? <XCircle size={16} />
                            : <CheckCircle size={16} />}
                        </button>
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

export default UtilisateursPage;