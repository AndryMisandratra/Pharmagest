import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Search, Edit, Truck,
  RefreshCw, Phone, Mail,
  CheckCircle, XCircle
} from 'lucide-react';
import fournisseurApi from '../../api/fournisseurApi';
import { useAuth } from '../../context/AuthContext';
import Modal from '../../components/Modal';
import FournisseurFormPage from './FournisseurFormPage';

const FournisseursPage = () => {
  const navigate = useNavigate();
  const { isPharmacien } = useAuth();

  const [fournisseurs, setFournisseurs] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [search, setSearch]             = useState('');

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    fetchFournisseurs();
  }, []);

  const fetchFournisseurs = async () => {
    setLoading(true);
    try {
      const res = await fournisseurApi.getAll();
      setFournisseurs(res.data);
    } catch {
      toast.error('Erreur chargement fournisseurs');
    } finally {
      setLoading(false);
    }
  };

  // Filtrage local
  const fournisseursFiltres = fournisseurs.filter(f =>
    !search ||
    f.raisonSociale.toLowerCase()
      .includes(search.toLowerCase()) ||
    (f.email && f.email.toLowerCase()
      .includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Fournisseurs
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {fournisseursFiltres.length} fournisseur(s)
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
            Nouveau fournisseur
          </button>
        )}
      </div>

      {/* Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingId(null);
          fetchFournisseurs();
        }}
        title={editingId
          ? 'Modifier le fournisseur'
          : 'Nouveau fournisseur'}
        size="lg"
      >
        <FournisseurFormPage
          fournisseurId={editingId}
          onSuccess={() => {
            setShowModal(false);
            setEditingId(null);
            fetchFournisseurs();
          }}
          onCancel={() => {
            setShowModal(false);
            setEditingId(null);
          }}
        />
      </Modal>

      {/* Recherche */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-4">
        <div className="flex gap-4">
          <div className="flex-1 relative">
            <Search size={18}
                    className="absolute left-3 top-1/2
                               -translate-y-1/2
                               text-gray-400" />
            <input
              type="text"
              placeholder="Rechercher un fournisseur..."
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
          <button
            onClick={fetchFournisseurs}
            className="flex items-center gap-2
                       border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-4 py-2.5 rounded-xl
                       text-sm transition">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>

      {/* Grille de cartes */}
      {loading ? (
        <div className="flex items-center
                        justify-center h-48">
          <div className="animate-spin rounded-full
                          h-10 w-10 border-b-2
                          border-primary-600"/>
        </div>
      ) : fournisseursFiltres.length === 0 ? (
        <div className="flex flex-col items-center
                        justify-center h-48 text-gray-400">
          <Truck size={48} className="mb-3 opacity-30" />
          <p className="font-medium">
            Aucun fournisseur trouvé
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2
                        lg:grid-cols-3 gap-4">
          {fournisseursFiltres.map(f => (
            <div key={f.id}
                 className="bg-white rounded-2xl shadow-sm
                            border border-gray-100 p-5
                            hover:shadow-md transition">

              {/* En-tête carte */}
              <div className="flex items-start
                              justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-primary-100
                                  rounded-xl flex items-center
                                  justify-center">
                    <Truck size={20}
                           className="text-primary-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold
                                   text-gray-800 text-sm">
                      {f.raisonSociale}
                    </h3>
                    <span className={`text-xs
                      ${f.estActif
                        ? 'text-green-600'
                        : 'text-gray-400'}`}>
                      {f.estActif ? '● Actif' : '● Inactif'}
                    </span>
                  </div>
                </div>
                {isPharmacien() && (
                  <button
                    onClick={() => {
                      setEditingId(f.id);
                      setShowModal(true);
                    }}
                    className="p-1.5 text-orange-600
                               hover:bg-orange-50
                               rounded-lg transition">
                    <Edit size={16} />
                  </button>
                )}
              </div>

              {/* Infos contact */}
              <div className="space-y-2">
                {f.email && (
                  <div className="flex items-center gap-2
                                  text-xs text-gray-500">
                    <Mail size={14}
                          className="text-gray-400" />
                    <span className="truncate">{f.email}</span>
                  </div>
                )}
                {f.telephone && (
                  <div className="flex items-center gap-2
                                  text-xs text-gray-500">
                    <Phone size={14}
                           className="text-gray-400" />
                    {f.telephone}
                  </div>
                )}
                {f.adresse && (
                  <p className="text-xs text-gray-400
                                truncate">
                    📍 {f.adresse}
                  </p>
                )}
              </div>

              {/* Délai livraison */}
              <div className="mt-3 pt-3 border-t
                              border-gray-100">
                <span className="text-xs text-gray-500">
                  Délai livraison :
                  <strong className="text-gray-700 ml-1">
                    {f.delaiLivraisonJours} jour(s)
                  </strong>
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FournisseursPage;