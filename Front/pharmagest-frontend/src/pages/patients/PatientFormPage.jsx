import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { Save, ArrowLeft, User } from 'lucide-react';
import patientApi from '../../api/patientApi';
import {
  validateTelephone,
  validateNumSecuSociale,
  validateName,
  sanitizeDigits,
  sanitizeNameInput
} from '../../utils/Validators';

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

const PatientFormPage = ({ 
  patientId, 
  onSuccess, 
  onCancel 
}) => {
  const navigate    = useNavigate();
  const paramsId    = useParams().id;
  const id          = patientId || paramsId;
  const isEdit      = !!id;
  const isModal     = !!onSuccess;
  const [loading, setLoading]     = useState(false);
  const [loadingData, setLoadingData] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors }
  } = useForm();

  useEffect(() => {
    if (isEdit) {
      setLoadingData(true);
      patientApi.getById(id)
        .then(res => {
          const p = res.data;
          Object.entries({
            nom:              p.nom,
            prenom:           p.prenom,
            dateNaissance:    p.dateNaissance,
            telephone:        p.telephone,
            numSecuSociale:   p.numSecuSociale,
            mutuelle:         p.mutuelle,
            tauxMutuelle:     p.tauxMutuelle,
            allergiesConnues: p.allergiesConnues,
          }).forEach(([k, v]) => setValue(k, v));
        })
        .catch(() => toast.error('Erreur chargement patient'))
        .finally(() => setLoadingData(false));
    }
  }, [id]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const payload = {
        ...data,
        tauxMutuelle: data.tauxMutuelle
          ? Number(data.tauxMutuelle) : null,
      };

      if (isEdit) {
        await patientApi.update(id, payload);
        toast.success('Patient modifié avec succès !');
      } else {
        await patientApi.create(payload);
        toast.success('Patient créé avec succès !');
      }
      
      if (isModal && onSuccess) {
        onSuccess();
      } else {
        navigate('/patients');
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

  if (loadingData) {
    return (
      <div className="flex items-center
                      justify-center h-64">
        <div className="animate-spin rounded-full
                        h-10 w-10 border-b-2
                        border-primary-600"/>
      </div>
    );
  }

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
                navigate('/patients');
              }
            }}
            className="p-2 hover:bg-gray-100
                       rounded-xl transition">
            <ArrowLeft size={20}
                       className="text-gray-600" />
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-800">
              {isEdit ? 'Modifier le patient'
                      : 'Nouveau patient'}
            </h2>
            <p className="text-gray-500 text-sm mt-1">
              {isEdit
                ? 'Modifiez les informations du patient'
                : 'Créez un nouveau dossier patient'}
            </p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">

        {/* Identité */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <User size={20}
                  className="text-primary-600" />
            Identité
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <FormField label="Nom" required
                       error={errors.nom?.message}>
              <input
                type="text"
                placeholder="Ex: RAKOTO"
                className={inputClass(errors.nom)}
                onInput={e => {
                  e.target.value = sanitizeNameInput(e.target.value);
                }}
                {...register('nom', {
                  required: 'Le nom est obligatoire',
                  validate: validateName
                })}
              />
            </FormField>

            <FormField label="Prénom" required
                       error={errors.prenom?.message}>
              <input
                type="text"
                placeholder="Ex: Jean"
                className={inputClass(errors.prenom)}
                onInput={e => {
                  e.target.value = sanitizeNameInput(e.target.value);
                }}
                {...register('prenom', {
                  required: 'Le prénom est obligatoire',
                  validate: validateName
                })}
              />
            </FormField>

            <FormField label="Date de naissance"
                       error={errors.dateNaissance?.message}>
              <input
                type="date"
                className={inputClass(errors.dateNaissance)}
                {...register('dateNaissance')}
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
                  validate: v => !v || validateTelephone(v)
                })}
              />
              <p className="text-xs text-gray-400 mt-1">
                Commence par 032, 033, 034, 037 ou 038
              </p>
            </FormField>
          </div>
        </div>

        {/* Sécurité Sociale & Mutuelle */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4">
            🏥 Sécurité Sociale & Mutuelle
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <FormField
              label="N° Sécurité Sociale (15 chiffres)"
              error={errors.numSecuSociale?.message}>
              <input
                type="text"
                inputMode="numeric"
                pattern="\d*"
                placeholder="Ex: 123456789012345"
                maxLength={15}
                className={inputClass(
                  errors.numSecuSociale)}
                onInput={e => {
                  e.target.value = sanitizeDigits(e.target.value);
                }}
                {...register('numSecuSociale', {
                  validate: validateNumSecuSociale
                })}
              />
            </FormField>

            <FormField label="Mutuelle"
                       error={errors.mutuelle?.message}>
              <input
                type="text"
                placeholder="Ex: MGEN, Harmonie..."
                className={inputClass(errors.mutuelle)}
                {...register('mutuelle')}
              />
            </FormField>

            <FormField label="Taux prise en charge mutuelle (%)"
                       error={errors.tauxMutuelle?.message}>
              <input
                type="number"
                min="0"
                max="100"
                step="0.01"
                placeholder="Ex: 80"
                className={inputClass(errors.tauxMutuelle)}
                {...register('tauxMutuelle', {
                  min: {
                    value: 0,
                    message: 'Doit être positif'
                  },
                  max: {
                    value: 100,
                    message: 'Maximum 100%'
                  }
                })}
              />
            </FormField>
          </div>
        </div>

        {/* Allergies */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4">
            ⚠️ Allergies médicamenteuses
          </h3>
          <FormField
            label="Allergies connues"
            error={errors.allergiesConnues?.message}>
            <textarea
              rows={3}
              placeholder="Ex: Pénicilline, Aspirine..."
              className={`${inputClass(
                errors.allergiesConnues)} resize-none`}
              {...register('allergiesConnues')}
            />
          </FormField>
          <p className="text-xs text-orange-600 mt-2
                        flex items-center gap-1">
            ⚠️ Ces informations seront affichées lors
            de la vente pour alerter le pharmacien.
          </p>
        </div>

        {/* Boutons */}
        <div className="flex items-center
                        justify-end gap-3">
          <button
            type="button"
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/patients');
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

export default PatientFormPage;