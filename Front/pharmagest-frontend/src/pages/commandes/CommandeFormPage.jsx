import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { toast } from 'react-toastify';
import {
  Save, ArrowLeft, Plus, Trash2,
  ClipboardList, Search
} from 'lucide-react';
import commandeApi from '../../api/commandeApi';
import fournisseurApi from '../../api/fournisseurApi';
import medicamentApi from '../../api/medicamentApi';

const inputClass = (error) =>
  `w-full px-3 py-2 border-2 rounded-xl text-sm
   focus:outline-none transition
   ${error
     ? 'border-red-400 bg-red-50'
     : 'border-gray-200 focus:border-primary-500 bg-white'
   }`;

const CommandeFormPage = ({ 
  commandeId, 
  onSuccess, 
  onCancel,
  initialSuggestions = [] 
}) => {
  const navigate = useNavigate();
  const paramsId = useParams().id;
  const id = commandeId || paramsId;
  const isModification = Boolean(id);
  const isConsultation = Boolean(id);
  const isModal = !!onSuccess;

  const [fournisseurs, setFournisseurs] = useState([]);
  const [medicaments, setMedicaments]   = useState([]);
  const [loading, setLoading]           = useState(false);
  const [searchMed, setSearchMed]       = useState('');
  const [initialLoading, setInitialLoading] = useState(isModification);
  const [commandeStatut, setCommandeStatut] = useState(null);
  const [commandeData, setCommandeData]     = useState(null);

  const {
    register,
    handleSubmit,
    control,
    reset,
    watch,
    formState: { errors }
  } = useForm({
    defaultValues: {
      lignes: [
        {
          medicamentId:      '',
          quantiteCommandee: 1,
          prixAchatHt:       ''
        }
      ]
    }
  });

  const { fields, append, remove } =
      useFieldArray({ control, name: 'lignes' });

  useEffect(() => {
    const loadData = async () => {
      try {
        const [fourRes, medRes] = await Promise.all([
          fournisseurApi.getAll(),
          medicamentApi.getAll(),
        ]);
        setFournisseurs(fourRes.data);
        setMedicaments(medRes.data);

        if (isModification) {
          const cmdRes = await commandeApi.getById(id);
          const cmd = cmdRes.data;
          setCommandeStatut(cmd.statut);
          setCommandeData(cmd);

          // CLOTUREE et autres statuts - mode consultation
          if (cmd.statut === 'CLOTUREE' || 
              cmd.statut === 'ENVOYEE' ||
              cmd.statut === 'EN_ATTENTE') {
            reset({
              fournisseurId: cmd.fournisseurId,
              notes: cmd.notes || '',
              lignes: cmd.lignes.map(l => ({
                medicamentId: l.medicamentId,
                quantiteCommandee: l.quantiteCommandee,
                prixAchatHt: l.prixAchatHt || '',
              })),
            });
          } else if (cmd.statut !== 'BROUILLON') {
            toast.error('Statut invalide pour modification');
            navigate('/commandes');
            return;
          } else {
            reset({
              fournisseurId: cmd.fournisseurId,
              notes: cmd.notes || '',
              lignes: cmd.lignes.map(l => ({
                medicamentId: l.medicamentId,
                quantiteCommandee: l.quantiteCommandee,
                prixAchatHt: l.prixAchatHt || '',
              })),
            });
          }
        }
      } catch {
        toast.error('Erreur chargement données');
      } finally {
        setInitialLoading(false);
      }
    };

    loadData();
  }, [id, isModification]);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const payload = {
        fournisseurId: Number(data.fournisseurId),
        notes:         data.notes || null,
        lignes: data.lignes.map(l => ({
          medicamentId:      Number(l.medicamentId),
          quantiteCommandee: Number(l.quantiteCommandee),
          prixAchatHt: l.prixAchatHt
            ? Number(l.prixAchatHt) : null,
        })),
      };

      if (isModification) {
        await commandeApi.modifier(id, payload);
        toast.success('Commande modifiée avec succès !');
      } else {
        await commandeApi.create(payload);
        toast.success('Commande créée avec succès !');
      }
      
      if (isModal && onSuccess) {
        onSuccess();
      } else {
        navigate('/commandes');
      }
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de la création'
      );
    } finally {
      setLoading(false);
    }
  };

  // Médicaments filtrés par recherche
  const medicamentsFiltres = medicaments.filter(m =>
    !searchMed ||
    m.denomination.toLowerCase()
      .includes(searchMed.toLowerCase())
  );

  if (initialLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 
                        border-b-2 border-primary-600" />
      </div>
    );
  }

  const isReadOnly = commandeStatut === 'CLOTUREE';

  return (
    <div className={isModal ? '' : 'max-w-4xl mx-auto space-y-6'}>

      {/* En-tête - masquée en mode modal */}
      {!isModal && (
        <div className="flex items-center gap-4">
          <button
            onClick={() => {
              if (isModal && onCancel) {
                onCancel();
              } else {
                navigate('/commandes');
              }
            }}
            className="p-2 hover:bg-gray-100
                     rounded-xl transition">
          <ArrowLeft size={20}
                     className="text-gray-600" />
        </button>
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            {isReadOnly ? 'Détails de la commande' : 
             isModification ? 'Modifier la commande' : 
             'Nouvelle commande'}
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {isReadOnly ? 'Commande cloturée' : 
             isModification ? 'Modifier une commande' : 
             'Créer un bon de commande fournisseur'}
          </p>
        </div>
      </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">

        {/* Fournisseur + Notes */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <ClipboardList size={20}
                           className="text-primary-600" />
            Informations générales
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">
            <div>
              <label className="block text-sm font-medium
                                 text-gray-700 mb-1">
                Fournisseur
                <span className="text-red-500 ml-1">*</span>
              </label>
              <select
                disabled={isReadOnly}
                className={inputClass(errors.fournisseurId)}
                {...register('fournisseurId', {
                  required: 'Le fournisseur est obligatoire'
                })}>
                <option value="">
                 Sélectionner un fournisseur...
                </option>
                {fournisseurs.map(f => (
                  <option key={f.id} value={f.id}>
                    {f.raisonSociale}
                    {` (délai: ${f.delaiLivraisonJours}j)`}
                  </option>
                ))}
              </select>
              {errors.fournisseurId && (
                <p className="text-red-500 text-xs mt-1">
                  ⚠️ {errors.fournisseurId.message}
                </p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium
                                 text-gray-700 mb-1">
                Notes pour le fournisseur
              </label>
              <textarea
                rows={2}
                disabled={isReadOnly}
                placeholder="Instructions particulières..."
                className={`${inputClass(null)} resize-none`}
                {...register('notes')}
              />
            </div>
          </div>
        </div>

        {/* Lignes de commande */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <div className="flex items-center
                          justify-between mb-4">
            <h3 className="text-lg font-semibold
                           text-gray-800">
              Médicaments commandés
            </h3>
            {!isReadOnly && (
              <button
                type="button"
                onClick={() => append({
                  medicamentId:      '',
                  quantiteCommandee: 1,
                  prixAchatHt:       ''
                })}
                className="flex items-center gap-2
                           border-2 border-primary-300
                           text-primary-600
                           hover:bg-primary-50
                           px-3 py-1.5 rounded-xl
                           text-sm transition font-medium">
                <Plus size={16} />
                Ajouter un médicament
              </button>
            )}
          </div>

          {/* Filtre médicaments - caché en consultation */}
          {!isReadOnly && (
          <div className="relative mb-4">
            <Search size={16}
                    className="absolute left-3 top-1/2
                               -translate-y-1/2
                               text-gray-400" />
            <input
              type="text"
              placeholder="Filtrer les médicaments..."
              value={searchMed}
              onChange={e => setSearchMed(e.target.value)}
              className="w-full pl-9 pr-4 py-2
                         border border-gray-200
                         rounded-xl text-sm
                         focus:outline-none
                         focus:border-primary-500
                         transition"
            />
          </div>
          )}

          {/* Lignes */}
          <div className="space-y-3">
            {fields.map((field, index) => (
              <div key={field.id}
                   className="grid grid-cols-12 gap-3
                              items-start p-3 bg-gray-50
                              rounded-xl">

                {/* Médicament */}
                <div className="col-span-5">
                  {index === 0 && (
                    <label className="block text-xs
                                       font-medium
                                       text-gray-500 mb-1">
                      Médicament *
                    </label>
                  )}
                  {isReadOnly ? (
                    <div className="px-3 py-2 bg-gray-100
                                   border-2 border-gray-200
                                   rounded-xl text-sm
                                   text-gray-800 font-medium">
                      {commandeData?.lignes[index]?.medicamentDenomination || '-'}
                    </div>
                  ) : (
                    <select
                      disabled={isReadOnly}
                      className={inputClass(
                        errors.lignes?.[index]?.medicamentId
                      )}
                      {...register(
                        `lignes.${index}.medicamentId`,
                        { required: 'Obligatoire' }
                      )}>
                      <option value="">
                        Sélectionner...
                      </option>
                      {medicamentsFiltres.map(m => (
                        <option key={m.id} value={m.id}>
                          {m.denomination}
                          {` (stock: ${m.stockQuantiteTotale})`}
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                {/* Quantité */}
                <div className="col-span-3">
                  {index === 0 && (
                    <label className="block text-xs
                                       font-medium
                                       text-gray-500 mb-1">
                      Quantité *
                    </label>
                  )}
                  <input
                    type="number"
                    min="1"
                    disabled={isReadOnly}
                    placeholder="Qté"
                    className={inputClass(
                      errors.lignes?.[index]
                            ?.quantiteCommandee
                    )}
                    {...register(
                      `lignes.${index}.quantiteCommandee`,
                      {
                        required: 'Obligatoire',
                        min: {
                          value: 1,
                          message: 'Min 1'
                        }
                      }
                    )}
                  />
                </div>

                {/* Prix HT */}
                <div className="col-span-3">
                  {index === 0 && (
                    <label className="block text-xs
                                       font-medium
                                       text-gray-500 mb-1">
                      Prix achat HT (Ar)
                    </label>
                  )}
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    disabled={isReadOnly}
                    placeholder="Prix HT"
                    className={inputClass(null)}
                    {...register(
                      `lignes.${index}.prixAchatHt`
                    )}
                  />
                </div>

                {/* Supprimer */}
                <div className={`col-span-1 flex
                                 items-${index === 0
                                   ? 'end pb-1'
                                   : 'center'}`}>
                  {fields.length > 1 && !isReadOnly && (
                    <button
                      type="button"
                      onClick={() => remove(index)}
                      className="p-1.5 text-red-500
                                 hover:bg-red-50
                                 rounded-lg transition">
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {fields.length === 0 && (
            <p className="text-center text-gray-400
                          text-sm py-4">
              Ajoutez au moins un médicament
            </p>
          )}
        </div>

        {/* Boutons */}
        <div className="flex items-center
                        justify-end gap-3">
          {isReadOnly ? (
            <button
              type="button"
              onClick={() => {
                if (isModal && onCancel) {
                  onCancel();
                } else {
                  navigate('/commandes');
                }
              }}
              className="px-6 py-2.5 bg-primary-600
                         text-white hover:bg-primary-700
                         rounded-xl text-sm transition
                         font-medium">
              Retour aux commandes
            </button>
          ) : (
            <>
              <button
                type="button"
                onClick={() => {
                  if (isModal && onCancel) {
                    onCancel();
                  } else {
                    navigate('/commandes');
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
                    {isModification ? 'Mise à jour...' : 'Création...'}
                  </>
                ) : (
                  <>
                    <Save size={16} />
                    {isModification ? 'Enregistrer' : 'Créer la commande'}
                  </>
                )}
              </button>
            </>
          )}
        </div>
      </form>
    </div>
  );
};

export default CommandeFormPage;