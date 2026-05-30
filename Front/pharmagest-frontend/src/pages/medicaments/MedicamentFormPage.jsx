import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { Save, ArrowLeft, Pill } from 'lucide-react';
import medicamentApi from '../../api/medicamentApi';
import fournisseurApi from '../../api/fournisseurApi';
import { sanitizeDigits, validateCodeCip } from '../../utils/Validators';

// Composant champ de formulaire réutilisable
const FormField = ({ label, required,
                     error, children }) => (
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
     ? 'border-red-400 bg-red-50 focus:border-red-500'
     : 'border-gray-200 focus:border-primary-500 bg-white'
   }`;

const MedicamentFormPage = ({ 
  medicamentId, 
  onSuccess, 
  onCancel 
}) => {
  const navigate = useNavigate();
  const paramsId = useParams().id;
  const id = medicamentId || paramsId;
  const isEdit = !!id;
  const isModal = !!onSuccess;

  const [categories, setCategories]   = useState([]);
  const [fournisseurs, setFournisseurs] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors }
  } = useForm({
    defaultValues: {
      tauxTva: '20',
      estRemboursableSs: false,
      statutLegal: 'LIBRE_ACCES',
    }
  });

  const estRemboursable = watch('estRemboursableSs');

  // Charger les données initiales
  useEffect(() => {
    const fetchData = async () => {
      setLoadingData(true);
      try {
        const [catRes, fourRes] = await Promise.all([
          medicamentApi.getCategories(),
          fournisseurApi.getAll(),
        ]);
        setCategories(catRes.data);
        setFournisseurs(fourRes.data);

        // Si modification → charger le médicament
        if (isEdit) {
          const medRes =
            await medicamentApi.getById(id);
          const m = medRes.data;
          // Pré-remplir le formulaire
          Object.entries({
            codeCip:           m.codeCip,
            denomination:      m.denomination,
            dci:               m.dci,
            formeGalenique:    m.formeGalenique,
            dosage:            m.dosage,
            categorieId:       m.categorieId,
            fournisseurId:     m.fournisseurId,
            statutLegal:       m.statutLegal,
            prixVenteTtc:      m.prixVenteTtc,
            prixAchatHt:       m.prixAchatHt,
            tauxTva:           m.tauxTva != null ? String(m.tauxTva) : '20',
            stockSeuilAlerte:  m.stockSeuilAlerte,
            emplacement:       m.emplacement,
            fabricant:         m.fabricant,
            estRemboursableSs: m.estRemboursableSs,
            tauxRemboursementSs: m.tauxRemboursementSs,
          }).forEach(([key, val]) => setValue(key, val));
        }
      } catch (error) {
        toast.error('Erreur chargement des données');
      } finally {
        setLoadingData(false);
      }
    };
    fetchData();
  }, [id]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Convertir les types
      const payload = {
        ...data,
        categorieId:     Number(data.categorieId),
        fournisseurId:   data.fournisseurId
                         ? Number(data.fournisseurId)
                         : null,
        prixVenteTtc:    Number(data.prixVenteTtc),
        prixAchatHt:     Number(data.prixAchatHt),
        tauxTva:         Number(data.tauxTva),
        stockSeuilAlerte: Number(data.stockSeuilAlerte),
        estRemboursableSs: data.estRemboursableSs === true
                           || data.estRemboursableSs === 'true',
        tauxRemboursementSs: data.tauxRemboursementSs
                             ? Number(data.tauxRemboursementSs)
                             : null,
      };

      if (isEdit) {
        await medicamentApi.update(id, payload);
        toast.success('Médicament modifié avec succès !');
      } else {
        await medicamentApi.create(payload);
        toast.success('Médicament créé avec succès !');
      }
      
      if (isModal && onSuccess) {
        onSuccess();
      } else {
        navigate('/medicaments');
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
      <div className="flex items-center justify-center
                      h-64">
        <div className="animate-spin rounded-full h-10 w-10
                        border-b-2 border-primary-600"/>
      </div>
    );
  }

  return (
    <div className={isModal ? '' : 'max-w-4xl mx-auto space-y-6'}>

      {/* En-tête - masquée en mode modal */}
      {!isModal && (
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/medicaments')}
            className="p-2 hover:bg-gray-100
                       rounded-xl transition">
            <ArrowLeft size={20} className="text-gray-600" />
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-800">
              {isEdit
                ? 'Modifier le médicament'
                : 'Nouveau médicament'}
            </h2>
            <p className="text-gray-500 text-sm mt-1">
              {isEdit
                ? 'Modifiez les informations du médicament'
                : 'Remplissez les informations du médicament'}
            </p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">

        {/* Section 1 — Identification */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <Pill size={20} className="text-primary-600" />
            Identification
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <FormField label="Code CIP (13 chiffres)"
                       required
                       error={errors.codeCip?.message}>
              <input
                type="text"
                inputMode="numeric"
                pattern="\d*"
                placeholder="1234567890123"
                maxLength={13}
                className={inputClass(errors.codeCip)}
                onInput={e => {
                  e.target.value = sanitizeDigits(e.target.value);
                }}
                {...register('codeCip', {
                  required: 'Le code CIP est obligatoire',
                  validate: validateCodeCip
                })}
              />
            </FormField>

            <FormField label="Dénomination commerciale"
                       required
                       error={errors.denomination?.message}>
              <input
                type="text"
                placeholder="Ex: Doliprane 1000mg"
                className={inputClass(errors.denomination)}
                {...register('denomination', {
                  required: 'La dénomination est obligatoire'
                })}
              />
            </FormField>

            <FormField label="DCI (principe actif)"
                       required
                       error={errors.dci?.message}>
              <input
                type="text"
                placeholder="Ex: Paracétamol"
                className={inputClass(errors.dci)}
                {...register('dci', {
                  required: 'La DCI est obligatoire'
                })}
              />
            </FormField>

            <FormField label="Forme galénique"
                       required
                       error={errors.formeGalenique?.message}>
              <select
                className={inputClass(errors.formeGalenique)}
                {...register('formeGalenique', {
                  required: 'La forme est obligatoire'
                })}>
                <option value="">Sélectionner...</option>
                {['Comprimé', 'Gélule', 'Sirop',
                  'Injectable', 'Pommade', 'Crème',
                  'Suppositoire', 'Patch', 'Collyre',
                  'Spray', 'Solution buvable'
                ].map(f => (
                  <option key={f} value={f}>{f}</option>
                ))}
              </select>
            </FormField>

            <FormField label="Dosage"
                       error={errors.dosage?.message}>
              <input
                type="text"
                placeholder="Ex: 500mg, 1g, 5mg/ml"
                className={inputClass(errors.dosage)}
                {...register('dosage')}
              />
            </FormField>

            <FormField label="Catégorie thérapeutique"
                       required
                       error={errors.categorieId?.message}>
              <select
                className={inputClass(errors.categorieId)}
                {...register('categorieId', {
                  required: 'La catégorie est obligatoire'
                })}>
                <option value="">Sélectionner...</option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.libelle}
                  </option>
                ))}
              </select>
            </FormField>
          </div>
        </div>

        {/* Section 2 — Statut et Prix */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4">
            💰 Statut légal et Prix
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <FormField label="Statut légal"
                       required
                       error={errors.statutLegal?.message}>
              <select
                className={inputClass(errors.statutLegal)}
                {...register('statutLegal', {
                  required: 'Le statut est obligatoire'
                })}>
                <option value="LIBRE_ACCES">
                  Libre accès
                </option>
                <option value="CONSEIL">Conseil</option>
                <option value="ORDONNANCE">Ordonnance</option>
                <option value="ORDONNANCE_SECURISEE">
                  Ordonnance sécurisée
                </option>
              </select>
            </FormField>

            <FormField label="Taux TVA (%)"
                       required
                       error={errors.tauxTva?.message}>
              <select
                className={inputClass(errors.tauxTva)}
                {...register('tauxTva', {
                  required: 'Le taux TVA est obligatoire'
                })}>
                <option value="2.1">2.1% (remboursables)</option>
                <option value="5.5">5.5% (autres)</option>
                <option value="10">10% (autres)</option>
                <option value="20">20% (standard)</option>
              </select>
            </FormField>

            <FormField label="Prix de vente TTC (Ar)"
                       required
                       error={errors.prixVenteTtc?.message}>
              <input
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                className={inputClass(errors.prixVenteTtc)}
                {...register('prixVenteTtc', {
                  required: 'Le prix TTC est obligatoire',
                  min: {
                    value: 0.01,
                    message: 'Le prix doit être positif'
                  }
                })}
              />
            </FormField>

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
                    message: 'Le prix doit être positif'
                  }
                })}
              />
            </FormField>
          </div>

          {/* Remboursement SS */}
          <div className="mt-4 p-4 bg-blue-50 rounded-xl
                          border border-blue-200">
            <div className="flex items-center gap-3 mb-3">
              <input
                type="checkbox"
                id="estRemboursableSs"
                className="w-4 h-4 text-primary-600
                           rounded cursor-pointer"
                {...register('estRemboursableSs')}
              />
              <label htmlFor="estRemboursableSs"
                     className="text-sm font-medium
                                text-blue-800 cursor-pointer">
                Remboursable par la Sécurité Sociale
              </label>
            </div>
            {estRemboursable && (
              <FormField
                label="Taux de remboursement SS (%)"
                error={errors.tauxRemboursementSs?.message}>
                <select
                  className={inputClass(
                    errors.tauxRemboursementSs)}
                  {...register('tauxRemboursementSs')}>
                  <option value="">Sélectionner...</option>
                  <option value="15">15%</option>
                  <option value="30">30%</option>
                  <option value="65">65%</option>
                  <option value="100">100%</option>
                </select>
              </FormField>
            )}
          </div>
        </div>

        {/* Section 3 — Stock et Localisation */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4">
            📦 Stock et Localisation
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            <FormField label="Seuil d'alerte stock"
                       required
                       error={errors.stockSeuilAlerte?.message}>
              <input
                type="number"
                min="0"
                placeholder="Ex: 10"
                className={inputClass(
                  errors.stockSeuilAlerte)}
                {...register('stockSeuilAlerte', {
                  required: "Le seuil d'alerte est obligatoire",
                  min: {
                    value: 0,
                    message: 'Doit être positif'
                  }
                })}
              />
              <p className="text-xs text-gray-400 mt-1">
                Alerte rouge si stock ≤ ce seuil
              </p>
            </FormField>

            <FormField label="Emplacement"
                       error={errors.emplacement?.message}>
              <input
                type="text"
                placeholder="Ex: Rayon A - Étagère 3"
                className={inputClass(errors.emplacement)}
                {...register('emplacement')}
              />
            </FormField>

            <FormField label="Fabricant"
                       error={errors.fabricant?.message}>
              <input
                type="text"
                placeholder="Ex: Sanofi, Pfizer..."
                className={inputClass(errors.fabricant)}
                {...register('fabricant')}
              />
            </FormField>

            <FormField label="Fournisseur habituel"
                       error={errors.fournisseurId?.message}>
              <select
                className={inputClass(errors.fournisseurId)}
                {...register('fournisseurId')}>
                <option value="">Aucun fournisseur</option>
                {fournisseurs.map(f => (
                  <option key={f.id} value={f.id}>
                    {f.raisonSociale}
                  </option>
                ))}
              </select>
            </FormField>
          </div>
        </div>

        {/* Boutons action */}
        <div className="flex items-center
                        justify-end gap-3">
          <button
            type="button"
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/medicaments');
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

export default MedicamentFormPage;