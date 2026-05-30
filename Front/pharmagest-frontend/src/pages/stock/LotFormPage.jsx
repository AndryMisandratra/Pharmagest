import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { Save, ArrowLeft, Package } from 'lucide-react';
import stockApi from '../../api/stockApi';
import medicamentApi from '../../api/medicamentApi';

const FormField = ({ label, required, error, children }) => (
  <div>
    <label className="block text-sm font-medium
                       text-gray-700 mb-1">
      {label}
      {required && (
        <span className="text-red-500 ml-1">*</span>
      )}
    </label>
    {children}
    {error && (
      <p className="text-red-500 text-xs mt-1">
        ⚠️ {error}
      </p>
    )}
  </div>
);

const inputClass = (error) =>
  `w-full px-3 py-2.5 border-2 rounded-xl text-sm
   focus:outline-none transition duration-200
   ${error
     ? 'border-red-400 bg-red-50'
     : 'border-gray-200 focus:border-primary-500 bg-white'
   }`;

const LotFormPage = () => {
  const navigate = useNavigate();
  const [medicaments, setMedicaments] = useState([]);
  const [loading, setLoading]         = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm();

  useEffect(() => {
    medicamentApi.getAll()
      .then(res => setMedicaments(res.data))
      .catch(() => toast.error('Erreur chargement médicaments'));
  }, []);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await stockApi.creerLot({
        medicamentId:   Number(data.medicamentId),
        numero:         data.numero,
        quantiteRecue:  Number(data.quantiteRecue),
        dateFabrication: data.dateFabrication || null,
        datePeremption: data.datePeremption,
        prixAchatHt:    Number(data.prixAchatHt),
      });
      toast.success('Lot créé avec succès !');
      navigate('/stock');
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de la création du lot'
      );
    } finally {
      setLoading(false);
    }
  };

  // Date minimale = demain
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  return (
    <div className="max-w-2xl mx-auto space-y-6">

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
            Ajouter un lot
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            Réception manuelle d'un lot de médicaments
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <Package size={20}
                     className="text-primary-600" />
            Informations du lot
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <div className="md:col-span-2">
              <FormField label="Médicament"
                         required
                         error={errors.medicamentId?.message}>
                <select
                  className={inputClass(errors.medicamentId)}
                  {...register('medicamentId', {
                    required: 'Le médicament est obligatoire'
                  })}>
                  <option value="">
                    Sélectionner un médicament...
                  </option>
                  {medicaments.map(m => (
                    <option key={m.id} value={m.id}>
                      {m.denomination} — {m.codeCip}
                    </option>
                  ))}
                </select>
              </FormField>
            </div>

            <FormField label="Numéro de lot"
                       required
                       error={errors.numero?.message}>
              <input
                type="text"
                placeholder="Ex: LOT-2025-001"
                className={inputClass(errors.numero)}
                {...register('numero', {
                  required: 'Le numéro de lot est obligatoire'
                })}
              />
            </FormField>

            <FormField label="Quantité reçue"
                       required
                       error={errors.quantiteRecue?.message}>
              <input
                type="number"
                min="1"
                placeholder="Ex: 100"
                className={inputClass(errors.quantiteRecue)}
                {...register('quantiteRecue', {
                  required: 'La quantité est obligatoire',
                  min: {
                    value: 1,
                    message: 'Minimum 1 unité'
                  }
                })}
              />
            </FormField>

            <FormField label="Date de fabrication"
                       error={errors.dateFabrication?.message}>
              <input
                type="date"
                className={inputClass(errors.dateFabrication)}
                {...register('dateFabrication')}
              />
            </FormField>

            <FormField label="Date de péremption"
                       required
                       error={errors.datePeremption?.message}>
              <input
                type="date"
                min={minDate}
                className={inputClass(errors.datePeremption)}
                {...register('datePeremption', {
                  required: 'La date de péremption est obligatoire',
                  validate: v =>
                    new Date(v) > new Date() ||
                    'La date doit être dans le futur'
                })}
              />
            </FormField>

            <div className="md:col-span-2">
              <FormField label="Prix d'achat HT (Ar)"
                         required
                         error={errors.prixAchatHt?.message}>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  className={inputClass(errors.prixAchatHt)}
                  {...register('prixAchatHt', {
                    required: "Le prix d'achat est obligatoire",
                    min: {
                      value: 0.01,
                      message: 'Doit être positif'
                    }
                  })}
                />
              </FormField>
            </div>
          </div>

          {/* Info FEFO */}
          <div className="mt-4 p-3 bg-blue-50 rounded-xl
                          border border-blue-200">
            <p className="text-xs text-blue-700
                          font-medium mb-1">
              📋 Règle FEFO appliquée automatiquement
            </p>
            <p className="text-xs text-blue-600">
              Les lots les plus proches de la péremption
              seront utilisés en premier lors des ventes.
            </p>
          </div>
        </div>

        {/* Boutons */}
        <div className="flex items-center
                        justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/stock')}
            className="px-6 py-2.5 border-2 border-gray-200
                       text-gray-600 hover:bg-gray-50
                       rounded-xl text-sm transition
                       font-medium">
            Annuler
          </button>
          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2
                       bg-primary-600 hover:bg-primary-700
                       disabled:bg-primary-300
                       text-white px-6 py-2.5 rounded-xl
                       text-sm transition shadow-md
                       font-medium">
            {loading ? (
              <>
                <div className="animate-spin rounded-full
                                h-4 w-4 border-b-2
                                border-white"/>
                Sauvegarde...
              </>
            ) : (
              <>
                <Save size={16} />
                Créer le lot
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default LotFormPage;