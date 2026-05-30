import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Search, Eye, Edit,
  User, RefreshCw, Phone
} from 'lucide-react';
import patientApi from '../../api/patientApi';
import Modal from '../../components/Modal';
import PatientFormPage from './PatientFormPage';

const PatientsPage = () => {
  const navigate = useNavigate();
  const [patients, setPatients] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [search, setSearch]     = useState('');

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    fetchPatients();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchPatients();
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  const fetchPatients = async () => {
    setLoading(true);
    try {
      const res = await patientApi.getAll(
        search || null
      );
      setPatients(res.data);
    } catch {
      toast.error('Erreur chargement patients');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Dossiers Patients
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {patients.length} patient(s) enregistré(s)
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
          Nouveau patient
        </button>
      </div>

      {/* Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingId(null);
          fetchPatients();
        }}
        title={editingId
          ? 'Modifier le patient'
          : 'Nouveau patient'}
        size="lg"
      >
        <PatientFormPage
          patientId={editingId}
          onSuccess={() => {
            setShowModal(false);
            setEditingId(null);
            fetchPatients();
          }}
          onCancel={() => {
            setShowModal(false);
            setEditingId(null);
          }}
        />
      </Modal>

      {/* Barre de recherche */}
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
              placeholder="Rechercher par nom ou prénom..."
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
            onClick={fetchPatients}
            className="flex items-center gap-2
                       border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-4 py-2.5 rounded-xl
                       text-sm transition">
            <RefreshCw size={16} />
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
        ) : patients.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48
                          text-gray-400">
            <User size={48}
                  className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucun patient trouvé
            </p>
            <p className="text-sm mt-1">
              Créez un nouveau dossier patient
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {['Nom', 'Prénom',
                    'Date naissance', 'Téléphone',
                    'N° Sécu. Sociale', 'Mutuelle',
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
                {patients.map(p => (
                  <tr key={p.id}
                      className="hover:bg-gray-50
                                 transition">
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {p.nom}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-600">
                      {p.prenom}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-500 text-xs">
                      {p.dateNaissance
                        ? new Date(p.dateNaissance)
                            .toLocaleDateString('fr-FR')
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      {p.telephone ? (
                        <span className="flex items-center
                                         gap-1 text-gray-600">
                          <Phone size={14} />
                          {p.telephone}
                        </span>
                      ) : '—'}
                    </td>
                    <td className="px-4 py-3 font-mono
                                   text-xs text-gray-500">
                      {p.numSecuSociale || '—'}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-600">
                      {p.mutuelle
                        ? `${p.mutuelle}${p.tauxMutuelle
                            ? ` (${p.tauxMutuelle}%)`
                            : ''}`
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center
                                      gap-2">
                        <button
                          onClick={() => {
                            setEditingId(p.id);
                            setShowModal(true);
                          }}
                          className="p-1.5 text-orange-600
                                     hover:bg-orange-50
                                     rounded-lg transition"
                          title="Modifier">
                          <Edit size={16} />
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

export default PatientsPage;