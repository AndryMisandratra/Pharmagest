import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { Save, ArrowLeft, Truck } from 'lucide-react';
import fournisseurApi from '../../api/fournisseurApi';
import { validateTelephone, sanitizeDigits } from '../../utils/Validators';

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

const FournisseurFormPage = ({ 
  fournisseurId, 
  onSuccess, 
  onCancel 
}) => {
  const navigate = useNavigate();
  const paramsId = useParams().id;
  const id       = fournisseurId || paramsId;
  const isEdit   = !!id;
  const isModal  = !!onSuccess;
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors }
  } = useForm({
    defaultValues: { delaiLivraisonJours: 3 }
  });

  useEffect(() => {
    if (isEdit) {
      fournisseurApi.getById(id)
        .then(res => {
          const f = res.data;
          Object.entries({
            raisonSociale:        f.raisonSociale,
            email:                f.email,
            telephone:            f.telephone,
            adresse:              f.adresse,
            delaiLivraisonJours:  f.delaiLivraisonJours,
          }).forEach(([k, v]) => setValue(k, v));
        })
        .catch(() =>
          toast.error('Erreur chargement fournisseur')
        );
    }
  }, [id, isEdit, setValue]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const payload = {
        ...data,
        delaiLivraisonJours: Number(data.delaiLivraisonJours),
      };

      if (isEdit) {
        await fournisseurApi.update(id, payload);
        toast.success('Fournisseur modifié !');
      } else {
        await fournisseurApi.create(payload);
        toast.success('Fournisseur créé !');
      }
      
      if (isModal && onSuccess) {
        onSuccess();
      } else {
        navigate('/fournisseurs');
      }
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de la sauvegarde'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={isModal ? '' : 'max-w-2xl mx-auto space-y-6'}>

      {/* En-tête - masquée en mode modal */}
      {!isModal && (
        <div className="flex items-center gap-4">
          <button
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/fournisseurs');
              }
            }}
            className="p-2 hover:bg-gray-100
                       rounded-xl transition">
            <ArrowLeft size={20}
                       className="text-gray-600" />
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-800">
              {isEdit ? 'Modifier le fournisseur'
                      : 'Nouveau fournisseur'}
            </h2>
            <p className="text-gray-500 text-sm mt-1">
              {isEdit
                ? 'Modifiez les informations'
                : 'Ajoutez un nouveau fournisseur'}
            </p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6
                        space-y-4">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-2
                         flex items-center gap-2">
            <Truck size={20}
                   className="text-primary-600" />
            Informations du fournisseur
          </h3>

          <FormField label="Raison sociale" required
                     error={errors.raisonSociale?.message}>
            <input
              type="text"
              placeholder="Ex: Pharmacie Distribution SARL"
              className={inputClass(errors.raisonSociale)}
              {...register('raisonSociale', {
                required: 'La raison sociale est obligatoire'
              })}
            />
          </FormField>

          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">
            <FormField label="Email"
                       error={errors.email?.message}>
              <input
                type="email"
                placeholder="contact@fournisseur.com"
                className={inputClass(errors.email)}
                {...register('email', {
                  pattern: {
                    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                    message: 'Format email invalide'
                  }
                })}
              />
            </FormField>

            <FormField label="Téléphone"
                       error={errors.telephone?.message}>
              <input
                type="tel"
                inputMode="numeric"
                placeholder="Ex: 0320000000"
                maxLength={10}
                className={inputClass(errors.telephone)}
                onInput={e => { e.target.value = sanitizeDigits(e.target.value); }}
                {...register('telephone', {
                  validate: v =>
                    !v || validateTelephone(v)
                })}
              />
              <p className="text-xs text-gray-400 mt-1">
                032, 033, 034, 037 ou 038 (10 chiffres)
              </p>
            </FormField>
          </div>

          <FormField label="Adresse"
                     error={errors.adresse?.message}>
            <textarea
              rows={2}
              placeholder="Adresse complète du fournisseur"
              className={`${inputClass(errors.adresse)}
                         resize-none`}
              {...register('adresse')}
            />
          </FormField>

          <FormField
            label="Délai de livraison moyen (jours)"
            required
            error={errors.delaiLivraisonJours?.message}>
            <input
              type="number"
              min="1"
              max="90"
              className={inputClass(
                errors.delaiLivraisonJours)}
              {...register('delaiLivraisonJours', {
                required: 'Le délai est obligatoire',
                min: {
                  value: 1,
                  message: 'Minimum 1 jour'
                },
                max: {
                  value: 90,
                  message: 'Maximum 90 jours'
                }
              })}
            />
          </FormField>
        </div>

        {/* Boutons */}
        <div className="flex items-center
                        justify-end gap-3 mt-6">
          <button
            type="button"
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/fournisseurs');
              }
            }}
            className="px-6 py-2.5 border-2
                       border-gray-200 text-gray-600
                       hover:bg-gray-50 rounded-xl
                       text-sm transition font-medium">
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
                {isEdit ? 'Modifier' : 'Créer'}
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default FournisseurFormPage;