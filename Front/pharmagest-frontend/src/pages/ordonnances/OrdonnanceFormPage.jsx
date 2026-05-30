import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import {
  Save, ArrowLeft, FileText,
  AlertTriangle, Search
} from 'lucide-react';
import ordonnanceApi from '../../api/ordonnanceApi';
import patientApi from '../../api/patientApi';
import {
  validateName,
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

const OrdonnanceFormPage = () => {
  const navigate  = useNavigate();
  const [loading, setLoading]       = useState(false);
  const [patients, setPatients]     = useState([]);
  const [searchPat, setSearchPat]   = useState('');
  const [selectedPat, setSelectedPat] = useState(null);
  const [checkingNum, setCheckingNum] = useState(false);
  const [numValid, setNumValid]     = useState(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors }
  } = useForm();

  const numero = watch('numero');

  // Recherche patient
  useEffect(() => {
    if (!searchPat.trim()) {
      setPatients([]);
      return;
    }
    const timer = setTimeout(async () => {
      try {
        const res = await patientApi.getAll(searchPat);
        setPatients(res.data);
      } catch {
        console.error('Erreur recherche patient');
      }
    }, 400);
    return () => clearTimeout(timer);
  }, [searchPat]);

  // Vérification anti-doublon numéro
  const verifierNumero = async () => {
    if (!numero?.trim()) return;
    setCheckingNum(true);
    try {
      await ordonnanceApi.getByNumero(numero);
      // Si trouvé → doublon
      setNumValid(false);
      toast.error(
        '⚠️ Ce numéro d\'ordonnance existe déjà !'
      );
    } catch {
      // Si 404 → numéro disponible
      setNumValid(true);
      toast.success('✅ Numéro disponible');
    } finally {
      setCheckingNum(false);
    }
  };

  const onSubmit = async (data) => {
    if (!selectedPat) {
      toast.error('Veuillez sélectionner un patient');
      return;
    }
    if (numValid === false) {
      toast.error(
        'Ce numéro d\'ordonnance existe déjà !'
      );
      return;
    }

    setLoading(true);
    try {
      await ordonnanceApi.create({
        patientId:          selectedPat.id,
        numero:             data.numero,
        medecinNom:         data.medecinNom,
        medecinRpps:        data.medecinRpps || null,
        datePrescription:   data.datePrescription,
        dateValidite:       data.dateValidite || null,
        aTiersPayant:       data.aTiersPayant === 'true' ||
                            data.aTiersPayant === true,
        fichierScanUrl:     null,
      });
      toast.success('Ordonnance enregistrée !');
      navigate('/ordonnances');
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de l\'enregistrement'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">

      {/* En-tête */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/ordonnances')}
          className="p-2 hover:bg-gray-100
                     rounded-xl transition">
          <ArrowLeft size={20}
                     className="text-gray-600" />
        </button>
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Nouvelle ordonnance
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            Enregistrement et archivage d'une ordonnance
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}
            className="space-y-6">

        {/* Sélection patient */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            Patient
            <span className="text-red-500">*</span>
          </h3>

          {selectedPat ? (
            <div className="flex items-center
                            justify-between p-4
                            bg-green-50 rounded-xl
                            border border-green-200">
              <div>
                <p className="font-semibold
                               text-green-800">
                  {selectedPat.prenom} {selectedPat.nom}
                </p>
                <p className="text-sm text-green-600">
                  {selectedPat.telephone || 'Pas de tél.'}
                  {selectedPat.numSecuSociale &&
                    ` — SS: ${selectedPat.numSecuSociale}`}
                </p>
                {selectedPat.allergiesConnues && (
                  <p className="text-xs text-orange-600
                                 mt-1 flex items-center
                                 gap-1">
                    <AlertTriangle size={12} />
                    Allergies : {selectedPat.allergiesConnues}
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={() => setSelectedPat(null)}
                className="text-red-500 text-sm
                           hover:underline">
                Changer
              </button>
            </div>
          ) : (
            <div className="space-y-2">
              <div className="relative">
                <Search size={18}
                        className="absolute left-3
                                   top-1/2 -translate-y-1/2
                                   text-gray-400" />
                <input
                  type="text"
                  placeholder="Rechercher un patient..."
                  value={searchPat}
                  onChange={e =>
                    setSearchPat(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5
                             border border-gray-200
                             rounded-xl text-sm
                             focus:outline-none
                             focus:border-primary-500
                             transition"
                />
              </div>
              {patients.length > 0 && (
                <div className="border border-gray-200
                                rounded-xl overflow-hidden
                                max-h-48 overflow-y-auto">
                  {patients.map(p => (
                    <button
                      key={p.id}
                      type="button"
                      onClick={() => {
                        setSelectedPat(p);
                        setSearchPat('');
                        setPatients([]);
                      }}
                      className="w-full text-left px-4
                                 py-3 hover:bg-gray-50
                                 transition border-b
                                 border-gray-100
                                 last:border-0 text-sm">
                      <span className="font-medium
                                       text-gray-800">
                        {p.prenom} {p.nom}
                      </span>
                      <span className="text-gray-400
                                       ml-2 text-xs">
                        {p.telephone}
                      </span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Informations ordonnance */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="text-lg font-semibold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <FileText size={20}
                      className="text-primary-600" />
            Informations de l'ordonnance
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">

            {/* Numéro avec vérification anti-doublon */}
            <div className="md:col-span-2">
              <FormField
                label="Numéro d'ordonnance"
                required
                error={errors.numero?.message}>
                <div className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Ex: ORD-2025-001234"
                    className={`flex-1 ${inputClass(
                      errors.numero ||
                      numValid === false
                    )}`}
                    {...register('numero', {
                      required: 'Le numéro est obligatoire'
                    })}
                  />
                  <button
                    type="button"
                    onClick={verifierNumero}
                    disabled={checkingNum || !numero}
                    className="px-4 py-2 bg-gray-100
                               hover:bg-gray-200
                               text-gray-700 rounded-xl
                               text-sm transition
                               font-medium
                               disabled:opacity-50">
                    {checkingNum
                      ? '...'
                      : 'Vérifier'}
                  </button>
                </div>
                {numValid === true && (
                  <p className="text-green-600 text-xs
                                mt-1">
                    ✅ Numéro disponible
                  </p>
                )}
                {numValid === false && (
                  <p className="text-red-500 text-xs mt-1">
                    ❌ Ce numéro existe déjà !
                  </p>
                )}
                <p className="text-xs text-gray-400 mt-1">
                  Cliquez sur "Vérifier" pour contrôler
                  l'anti-doublon
                </p>
              </FormField>
            </div>

            <FormField label="Médecin prescripteur"
                       required
                       error={errors.medecinNom?.message}>
              <input
                type="text"
                placeholder="Ex: Dr. RAKOTO Jean"
                className={inputClass(errors.medecinNom)}
                onInput={e => {
                  e.target.value = sanitizeNameInput(e.target.value);
                }}
                {...register('medecinNom', {
                  required: 'Le médecin est obligatoire',
                  validate: validateName
                })}
              />
            </FormField>

            <FormField label="N° RPPS du médecin"
                       error={errors.medecinRpps?.message}>
              <input
                type="text"
                placeholder="Ex: 10005678901"
                maxLength={20}
                className={inputClass(errors.medecinRpps)}
                {...register('medecinRpps')}
              />
            </FormField>

            <FormField label="Date de prescription"
                       required
                       error={errors.datePrescription?.message}>
              <input
                type="date"
                className={inputClass(
                  errors.datePrescription)}
                {...register('datePrescription', {
                  required: 'La date est obligatoire'
                })}
              />
            </FormField>

            <FormField label="Date de validité"
                       error={errors.dateValidite?.message}>
              <input
                type="date"
                className={inputClass(errors.dateValidite)}
                {...register('dateValidite')}
              />
            </FormField>

            <div className="md:col-span-2">
              <FormField label="Tiers payant"
                         error={errors.aTiersPayant?.message}>
                <div className="flex items-center gap-4">
                  <label className="flex items-center
                                     gap-2 cursor-pointer">
                    <input
                      type="radio"
                      value="false"
                      defaultChecked
                      className="w-4 h-4
                                 text-primary-600"
                      {...register('aTiersPayant')}
                    />
                    <span className="text-sm text-gray-700">
                      Non
                    </span>
                  </label>
                  <label className="flex items-center
                                     gap-2 cursor-pointer">
                    <input
                      type="radio"
                      value="true"
                      className="w-4 h-4
                                 text-primary-600"
                      {...register('aTiersPayant')}
                    />
                    <span className="text-sm text-gray-700">
                      Oui — Prise en charge tiers payant
                    </span>
                  </label>
                </div>
              </FormField>
            </div>
          </div>
        </div>

        {/* Info anti-doublon */}
        <div className="p-4 bg-orange-50 rounded-xl
                        border border-orange-200">
          <p className="text-sm text-orange-700
                         font-medium mb-1
                         flex items-center gap-2">
            <AlertTriangle size={16} />
            Contrôle anti-doublon activé
          </p>
          <p className="text-xs text-orange-600">
            Le système vérifie automatiquement que cette
            ordonnance n'a pas déjà été honorée.
            Une ordonnance ne peut être délivrée
            qu'une seule fois.
          </p>
        </div>

        {/* Boutons */}
        <div className="flex items-center
                        justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/ordonnances')}
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
                Enregistrement...
              </>
            ) : (
              <>
                <Save size={16} />
                Enregistrer
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default OrdonnanceFormPage;