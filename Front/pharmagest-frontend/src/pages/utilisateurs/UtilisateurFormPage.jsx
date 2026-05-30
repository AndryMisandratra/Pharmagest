import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import {
  Save, ArrowLeft, UserCog,
  Eye, EyeOff
} from 'lucide-react';
import utilisateurApi from '../../api/utilisateurApi';
import {
  validateName,
  validateGoogleEmail,
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

const UtilisateurFormPage = ({ 
  utilisateurId, 
  onSuccess, 
  onCancel 
}) => {
  const navigate = useNavigate();
  const paramsId = useParams().id;
  const id       = utilisateurId || paramsId;
  const isEdit   = !!id;
  const isModal  = !!onSuccess;

  const [loading, setLoading]       = useState(false);
  const [loadingData, setLoadingData] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors }
  } = useForm();

  useEffect(() => {
    if (isEdit) {
      setLoadingData(true);
      utilisateurApi.getById(id)
        .then(res => {
          const u = res.data;
          setValue('nom',    u.nom);
          setValue('prenom', u.prenom);
          setValue('email',  u.email);
          setValue('role',   u.role);
        })
        .catch(() =>
          toast.error('Erreur chargement utilisateur')
        )
        .finally(() => setLoadingData(false));
    }
  }, [id]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      if (isEdit) {
        await utilisateurApi.update(id, data);
        toast.success('Utilisateur modifié !');
      } else {
        await utilisateurApi.create(data);
        toast.success('Utilisateur créé !');
      }
      
      if (isModal && onSuccess) {
        onSuccess();
      } else {
        navigate('/utilisateurs');
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
    <div className={isModal ? '' : 'max-w-xl mx-auto space-y-6'}>

      {/* En-tête - masquée en mode modal */}
      {!isModal && (
        <div className="flex items-center gap-4">
          <button
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/utilisateurs');
              }
            }}
            className="p-2 hover:bg-gray-100
                       rounded-xl transition">
            <ArrowLeft size={20}
                       className="text-gray-600" />
          </button>
          <div>
          <h2 className="text-2xl font-bold text-gray-800">
            {isEdit
              ? 'Modifier l\'utilisateur'
              : 'Nouveau compte utilisateur'}
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {isEdit
              ? 'Modifiez les informations du compte'
              : 'Créez un nouveau compte pour le personnel'}
          </p>
        </div>
      </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6
                        space-y-4">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-2
                         flex items-center gap-2">
            <UserCog size={20}
                     className="text-primary-600" />
            Informations du compte
          </h3>

          <div className="grid grid-cols-2 gap-4">
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
          </div>

          <FormField label="Email de connexion"
                     required
                     error={errors.email?.message}>
            <input
              type="email"
              placeholder="exemple@gmail.com"
              className={inputClass(errors.email)}
              {...register('email', {
                required: "L'email est obligatoire",
                validate: validateGoogleEmail
              })}
            />
          </FormField>

          <FormField label="Rôle" required
                     error={errors.role?.message}>
            <select
              className={inputClass(errors.role)}
              {...register('role', {
                required: 'Le rôle est obligatoire'
              })}>
              <option value="">
                Sélectionner un rôle...
              </option>
              <option value="ROLE_PHARMACIEN">
                👨‍⚕️ Pharmacien — Accès complet
              </option>
              <option value="ROLE_PREPARATEUR">
                🧪 Préparateur — Stock + Ventes + Ordonnances
              </option>
              <option value="ROLE_CAISSIER">
                💰 Caissier — Ventes simples uniquement
              </option>
              <option value="ROLE_ADMIN">
                ⚙️ Administrateur — Configuration technique
              </option>
            </select>
          </FormField>

          {/* Mot de passe */}
          <FormField
            label={isEdit
              ? 'Nouveau mot de passe (laisser vide pour ne pas changer)'
              : 'Mot de passe'}
            required={!isEdit}
            error={errors.password?.message}>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder={isEdit
                  ? 'Laisser vide = pas de changement'
                  : 'Minimum 8 caractères'}
                className={`${inputClass(
                  errors.password)} pr-12`}
                {...register('password', {
                  required: isEdit
                    ? false
                    : 'Le mot de passe est obligatoire',
                  minLength: {
                    value: 8,
                    message: 'Minimum 8 caractères'
                  }
                })}
              />
              <button
                type="button"
                onClick={() =>
                  setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2
                           -translate-y-1/2 text-gray-400
                           hover:text-gray-600
                           transition">
                {showPassword
                  ? <EyeOff size={18} />
                  : <Eye size={18} />}
              </button>
            </div>
          </FormField>

          {/* Info sécurité */}
          <div className="p-3 bg-yellow-50 rounded-xl
                          border border-yellow-200">
            <p className="text-xs text-yellow-700">
              🔒 Le mot de passe est automatiquement
              hashé avec BCrypt avant d'être stocké.
              Il n'est jamais stocké en clair.
            </p>
          </div>
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
                navigate('/utilisateurs');
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

export default UtilisateurFormPage;