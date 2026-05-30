import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Plus, Search, Eye, FileText,
  RefreshCw, CheckCircle, Clock,
  XCircle, AlertTriangle
} from 'lucide-react';
import ordonnanceApi from '../../api/ordonnanceApi';

const StatutBadge = ({ statut }) => {
  const config = {
    EN_ATTENTE: {
      style: 'bg-yellow-100 text-yellow-700',
      icon:  <Clock size={12} />,
      label: 'En attente'
    },
    SERVIE: {
      style: 'bg-green-100 text-green-700',
      icon:  <CheckCircle size={12} />,
      label: 'Servie'
    },
    PARTIELLEMENT_SERVIE: {
      style: 'bg-blue-100 text-blue-700',
      icon:  <AlertTriangle size={12} />,
      label: 'Partielle'
    },
    EXPIREE: {
      style: 'bg-red-100 text-red-700',
      icon:  <XCircle size={12} />,
      label: 'Expirée'
    },
  };
  const c = config[statut] || config.EN_ATTENTE;
  return (
    <span className={`flex items-center gap-1
                     px-2 py-1 rounded-full
                     text-xs font-medium ${c.style}`}>
      {c.icon}
      {c.label}
    </span>
  );
};

const OrdonnancesPage = () => {
  const navigate = useNavigate();
  const [ordonnances, setOrdonnances] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [filtreStatut, setFiltreStatut] =
      useState('EN_ATTENTE');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchOrdonnances();
  }, [filtreStatut]);

  const fetchOrdonnances = async () => {
    setLoading(true);
    try {
      const res = await ordonnanceApi
          .getAll(null, filtreStatut);
      setOrdonnances(res.data);
    } catch {
      toast.error('Erreur chargement ordonnances');
    } finally {
      setLoading(false);
    }
  };

  const handleChangerStatut = async (id, statut) => {
    try {
      await ordonnanceApi.changerStatut(id, statut);
      toast.success('Statut mis à jour');
      fetchOrdonnances();
    } catch {
      toast.error('Erreur mise à jour statut');
    }
  };

  // Filtrage local par numéro
  const ordonnancesFiltrees = ordonnances.filter(o =>
    !search ||
    o.numero.toLowerCase()
      .includes(search.toLowerCase()) ||
    o.patientNom.toLowerCase()
      .includes(search.toLowerCase()) ||
    o.medecinNom.toLowerCase()
      .includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Ordonnances
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {ordonnancesFiltrees.length} ordonnance(s)
          </p>
        </div>
        <button
          onClick={() => navigate('/ordonnances/nouvelle')}
          className="flex items-center gap-2
                     bg-primary-600 hover:bg-primary-700
                     text-white px-4 py-2 rounded-xl
                     text-sm transition shadow-sm">
          <Plus size={18} />
          Nouvelle ordonnance
        </button>
      </div>

      {/* Filtres */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-4">
        <div className="flex flex-col md:flex-row
                        gap-4">
          <div className="flex-1 relative">
            <Search size={18}
                    className="absolute left-3 top-1/2
                               -translate-y-1/2
                               text-gray-400" />
            <input
              type="text"
              placeholder="N° ordonnance, patient, médecin..."
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

          {/* Filtre statut */}
          <div className="flex gap-2">
            {[
              { val: 'EN_ATTENTE', label: '⏳ En attente' },
              { val: 'SERVIE',     label: '✅ Servies' },
              { val: 'PARTIELLEMENT_SERVIE',
                label: '🔵 Partielles' },
              { val: 'EXPIREE',    label: '❌ Expirées' },
            ].map(s => (
              <button
                key={s.val}
                onClick={() => setFiltreStatut(s.val)}
                className={`px-3 py-2 rounded-xl text-xs
                           font-medium transition border-2
                           ${filtreStatut === s.val
                             ? 'border-primary-500 bg-primary-50 text-primary-700'
                             : 'border-gray-200 text-gray-600 hover:border-gray-300'
                           }`}>
                {s.label}
              </button>
            ))}
          </div>

          <button
            onClick={fetchOrdonnances}
            className="flex items-center gap-2
                       border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-4 py-2.5 rounded-xl
                       text-sm transition">
            <RefreshCw size={16} />
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
        ) : ordonnancesFiltrees.length === 0 ? (
          <div className="flex flex-col items-center
                          justify-center h-48
                          text-gray-400">
            <FileText size={48}
                      className="mb-3 opacity-30" />
            <p className="font-medium">
              Aucune ordonnance
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b
                                border-gray-100">
                <tr>
                  {['N° Ordonnance', 'Patient',
                    'Médecin', 'Date prescription',
                    'Date validité', 'Tiers payant',
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
                {ordonnancesFiltrees.map(o => (
                  <tr key={o.id}
                      className="hover:bg-gray-50
                                 transition">
                    <td className="px-4 py-3 font-mono
                                   text-xs font-medium
                                   text-gray-700">
                      {o.numero}
                    </td>
                    <td className="px-4 py-3 font-medium
                                   text-gray-800">
                      {o.patientPrenom} {o.patientNom}
                    </td>
                    <td className="px-4 py-3
                                   text-gray-600">
                      Dr. {o.medecinNom}
                      {o.medecinRpps && (
                        <span className="text-xs
                                         text-gray-400 ml-1">
                          ({o.medecinRpps})
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-xs
                                   text-gray-500">
                      {new Date(o.datePrescription)
                        .toLocaleDateString('fr-FR')}
                    </td>
                    <td className="px-4 py-3 text-xs
                                   text-gray-500">
                      {o.dateValidite
                        ? new Date(o.dateValidite)
                            .toLocaleDateString('fr-FR')
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      {o.aTiersPayant ? (
                        <span className="px-2 py-1
                                         bg-blue-100
                                         text-blue-700
                                         rounded-full
                                         text-xs">
                          🏥 Oui
                        </span>
                      ) : (
                        <span className="text-gray-400
                                         text-xs">Non</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <StatutBadge statut={o.statut} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center
                                      gap-2">
                        <button
                          onClick={() => navigate(
                            `/ordonnances/${o.id}`
                          )}
                          className="p-1.5 text-blue-600
                                     hover:bg-blue-50
                                     rounded-lg transition"
                          title="Voir détail">
                          <Eye size={16} />
                        </button>
                        {/* Changer statut */}
                        {o.statut === 'EN_ATTENTE' && (
                          <button
                            onClick={() =>
                              handleChangerStatut(
                                o.id, 'SERVIE'
                              )}
                            className="p-1.5 text-green-600
                                       hover:bg-green-50
                                       rounded-lg transition"
                            title="Marquer comme servie">
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

export default OrdonnancesPage;