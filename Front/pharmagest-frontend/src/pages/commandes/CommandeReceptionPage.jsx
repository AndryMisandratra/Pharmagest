import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import {
  ArrowLeft, Package, CheckCircle, Calendar, Hash, Factory
} from 'lucide-react';
import commandeApi from '../../api/commandeApi';

const inputClass = (error) =>
  `w-full px-3 py-2 border-2 rounded-xl text-sm
   focus:outline-none transition
   ${error
     ? 'border-red-400 bg-red-50'
     : 'border-gray-200 focus:border-primary-500 bg-white'
   }`;

const CommandeReceptionPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [commande, setCommande] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm();

  useEffect(() => {
    fetchCommande();
  }, [id]);

  const fetchCommande = async () => {
    setLoading(true);
    try {
      const res = await commandeApi.getById(id);
      console.log('Commande response:', res.data);
      setCommande(res.data);

      const defaultValues = {};
      res.data.lignes.forEach((ligne, index) => {
        defaultValues[`lignes.${index}.ligneCommandeId`] = ligne.id;
        defaultValues[`lignes.${index}.datePeremption`] =
          ligne.datePeremptionRecue || '';
        defaultValues[`lignes.${index}.dateFabrication`] =
          ligne.dateFabricationRecue || '';
        defaultValues[`lignes.${index}.prixAchatHt`] =
          ligne.prixAchatHt || '';
      });
      reset(defaultValues);
    } catch {
      toast.error('Erreur chargement commande');
      navigate('/commandes');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const lignes = commande.lignes.map((ligne, index) => ({
        ligneCommandeId: ligne.id,
        datePeremption: data.lignes[index].datePeremption || null,
        dateFabrication: data.lignes[index].dateFabrication || null,
        prixAchatHt: data.lignes[index].prixAchatHt
          ? Number(data.lignes[index].prixAchatHt) : null,
      }));

      await commandeApi.receptionner(id, { lignes });
      toast.success('Réception enregistrée ! Stock mis à jour.');
      navigate('/commandes');
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de la réception'
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10
                        border-b-2 border-primary-600" />
      </div>
    );
  }

  if (!commande) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6">

      {/* En-tête */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/commandes')}
          className="p-2 hover:bg-gray-100 rounded-xl transition">
          <ArrowLeft size={20} className="text-gray-600" />
        </button>
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Réception de commande
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {commande.reference} — {commande.fournisseurRaisonSociale}
          </p>
        </div>
      </div>

      {/* Info commande */}
      <div className="bg-white rounded-2xl shadow-sm
                      border border-gray-100 p-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4
                        text-sm">
          <div>
            <p className="text-gray-500">Référence</p>
            <p className="font-medium font-mono">
              {commande.reference}
            </p>
          </div>
          <div>
            <p className="text-gray-500">Fournisseur</p>
            <p className="font-medium">
              {commande.fournisseurRaisonSociale}
            </p>
          </div>
          <div>
            <p className="text-gray-500">Date commande</p>
            <p className="font-medium">
              {new Date(commande.dateCommande)
                .toLocaleDateString('fr-FR')}
            </p>
          </div>
          <div>
            <p className="text-gray-500">Montant HT</p>
            <p className="font-medium">
              {commande.montantTotalHt
                ? `${Number(commande.montantTotalHt)
                    .toLocaleString('fr-FR')} Ar`
                : '—'}
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

        {/* Lignes de réception */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold text-gray-800
                         mb-4 flex items-center gap-2">
            <Package size={20} className="text-primary-600" />
            Lignes à réceptionner
          </h3>

          <div className="space-y-4">
            {commande.lignes.map((ligne, index) => (
              <div key={ligne.id}
                   className="p-4 bg-gray-50 rounded-xl
                              border border-gray-100">
                <p className="font-medium text-gray-800 mb-3 text-lg font-bold">
                  Médicament: {ligne.medicamentDenomination || `ID: ${ligne.medicamentId}`}
                </p>

                <div className="grid grid-cols-1 md:grid-cols-4
                                gap-3">
                  {/* Numéro de lot (read-only) */}
                  <div>
                    <label className="block text-xs font-medium
                                       text-gray-500 mb-1">
                      <Hash size={12}
                            className="inline mr-1" />
                      N° de lot
                    </label>
                    <input
                      type="text"
                      readOnly
                      value={ligne.lotNumeroRecu || ''}
                      className="w-full px-3 py-2 border-2 rounded-xl
                             text-sm bg-gray-100 text-gray-600
                             cursor-not-allowed"
                    />
                  </div>

                  {/* Quantité commandé (read-only) */}
                  <div>
                    <label className="block text-xs font-medium
                                       text-gray-500 mb-1">
                      <Package size={12}
                              className="inline mr-1" />
                      Qté commandée
                    </label>
                    <input
                      type="number"
                      readOnly
                      value={ligne.quantiteCommandee}
                      className="w-full px-3 py-2 border-2 rounded-xl
                             text-sm bg-gray-100 text-gray-600
                             cursor-not-allowed"
                    />
                  </div>

                  {/* Date de fabrication */}
                  <div>
                    <label className="block text-xs font-medium
                                       text-gray-500 mb-1">
                      <Factory size={12}
                             className="inline mr-1" />
                      Date fabrication *
                    </label>
                    <input
                      type="date"
                      className={inputClass(
                        errors.lignes?.[index]?.dateFabrication
                      )}
                      {...register(
                        `lignes.${index}.dateFabrication`,
                        {
                          required: 'Date fabrication obligatoire'
                        }
                      )}
                    />
                    {errors.lignes?.[index]?.dateFabrication && (
                      <p className="text-red-500 text-xs mt-1">
                        {errors.lignes[index]
                          .dateFabrication.message}
                      </p>
                    )}
                  </div>

                  {/* Date de péremption */}
                  <div>
                    <label className="block text-xs font-medium
                                       text-gray-500 mb-1">
                      <Calendar size={12}
                                className="inline mr-1" />
                      Date péremption *
                    </label>
                    <input
                      type="date"
                      min={(() => {
                        const tomorrow = new Date();
                        tomorrow.setDate(tomorrow.getDate() + 1);
                        return tomorrow.toISOString().split('T')[0];
                      })()}
                      className={inputClass(
                        errors.lignes?.[index]?.datePeremption
                      )}
                      {...register(
                        `lignes.${index}.datePeremption`,
                        {
                          required: 'Date péremption obligatoire',
                          validate: (value) => {
                            if (!value) return 'Date péremption obligatoire';
                            const today = new Date();
                            today.setHours(0, 0, 0, 0);
                            const datePerm = new Date(value);
                            return datePerm > today 
                              || 'La date doit être supérieure à aujourd\'hui';
                          }
                        }
                      )}
                    />
                    {errors.lignes?.[index]?.datePeremption && (
                      <p className="text-red-500 text-xs mt-1">
                        {errors.lignes[index]
                          .datePeremption.message}
                      </p>
                    )}
                  </div>
                </div>

                {/* Prix achat HT */}
                <div className="mt-3">
                  <label className="block text-xs font-medium
                                     text-gray-500 mb-1">
                    Prix achat HT (Ar)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="Optionnel"
                    className={inputClass(null)}
                    {...register(
                      `lignes.${index}.prixAchatHt`
                    )}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Boutons */}
        <div className="flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/commandes')}
            className="px-6 py-2.5 border-2 border-gray-200
                       text-gray-600 hover:bg-gray-50
                       rounded-xl text-sm transition
                       font-medium">
            Annuler
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="flex items-center gap-2
                       bg-green-600 hover:bg-green-700
                       disabled:bg-green-300
                       text-white px-6 py-2.5 rounded-xl
                       text-sm transition shadow-md
                       font-medium">
            {submitting ? (
              <>
                <div className="animate-spin rounded-full
                                h-4 w-4 border-b-2
                                border-white"/>
                Enregistrement...
              </>
            ) : (
              <>
                <CheckCircle size={16} />
                Confirmer la réception
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CommandeReceptionPage;