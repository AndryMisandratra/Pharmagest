import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import {
  Search, Plus, Minus, Trash2, ShoppingCart,
  CheckCircle, User, FileText, CreditCard,
  Printer, AlertCircle
} from 'lucide-react';
import venteApi from '../../api/venteApi';
import medicamentApi from '../../api/medicamentApi';
import patientApi from '../../api/patientApi';
import {
  validateName,
  sanitizeNameInput
} from '../../utils/Validators';

// ================================================
// Composant carte médicament dans le panier
// ================================================
const PanierItem = ({ item, onUpdate, onRemove }) => (
  <div className="flex items-center gap-3 p-3
                  bg-gray-50 rounded-xl border
                  border-gray-100">
    <div className="flex-1">
      <p className="font-medium text-sm text-gray-800">
        {item.denomination}
      </p>
      <p className="text-xs text-gray-500">
        {Number(item.prixVenteTtc)
          .toLocaleString('fr-FR')} Ar/unité
        {item.statutLegal !== 'LIBRE_ACCES' &&
         item.statutLegal !== 'CONSEIL' && (
          <span className="ml-2 px-1.5 py-0.5
                           bg-orange-100 text-orange-700
                           rounded text-xs">
            📋 Ordonnance requise
          </span>
        )}
      </p>
    </div>

    {/* Contrôle quantité */}
    <div className="flex items-center gap-2">
      <button
        onClick={() => onUpdate(
          item.id,
          Math.max(1, item.quantite - 1)
        )}
        className="p-1 rounded-lg bg-white border
                   border-gray-200 hover:bg-gray-50
                   transition">
        <Minus size={14} className="text-gray-600" />
      </button>
      <span className="w-10 text-center font-bold
                       text-gray-800 text-sm">
        {item.quantite}
      </span>
      <button
        onClick={() => onUpdate(
          item.id,
          Math.min(
            item.stockQuantiteTotale,
            item.quantite + 1
          )
        )}
        disabled={
          item.quantite >= item.stockQuantiteTotale
        }
        className="p-1 rounded-lg bg-white border
                   border-gray-200 hover:bg-gray-50
                   transition disabled:opacity-50">
        <Plus size={14} className="text-gray-600" />
      </button>
    </div>

    {/* Sous-total */}
    <div className="w-28 text-right">
      <p className="font-bold text-gray-800 text-sm">
        {(Number(item.prixVenteTtc) * item.quantite)
          .toLocaleString('fr-FR')} Ar
      </p>
    </div>

    {/* Supprimer */}
    <button
      onClick={() => onRemove(item.id)}
      className="p-1.5 text-red-500
                 hover:bg-red-50 rounded-lg
                 transition">
      <Trash2 size={16} />
    </button>
  </div>
);

// ================================================
  // Page principale Point de Vente
  // ================================================
  const VentePage = () => {
    const [panier, setPanier]           = useState([]);
    const [search, setSearch]           = useState('');
    const [resultats, setResultats]     = useState([]);
    const [searching, setSearching]     = useState(false);
    const [montantPaye, setMontantPaye] = useState('');
    const [patient, setPatient]         = useState(null);
    const [hasOrdonnance, setHasOrdonnance] = useState(false);
    const [ordonnanceInfo, setOrdonnanceInfo] = useState({
      numero: '',
      medecinNom: '',
      medecinRpps: '',
      datePrescription: '',
      aTiersPayant: false
    });
    const [loading, setLoading]         = useState(false);
    const [ticketLoading, setTicketLoading] =
        useState(false);
    const [venteReussie, setVenteReussie] = useState(null);
    const [patientNomError, setPatientNomError] = useState('');
    const [medecinNomError, setMedecinNomError] = useState('');

    // Recherche patient
    const [searchPatient, setSearchPatient] = useState('');
    const [patients, setPatients]       = useState([]);

  // ================================================
  // Recherche médicament (debounce 400ms)
  // ================================================
  useEffect(() => {
    if (!search.trim()) {
      setResultats([]);
      return;
    }
    const timer = setTimeout(async () => {
      setSearching(true);
      try {
        const res = await medicamentApi.getAll(search);
        setResultats(res.data.filter(
          m => m.estDisponible &&
               m.stockQuantiteTotale > 0
        ));
      } catch {
        toast.error('Erreur recherche médicament');
      } finally {
        setSearching(false);
      }
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  // ================================================
  // Recherche patient (debounce 400ms)
  // ================================================
  useEffect(() => {
    if (!searchPatient.trim()) {
      setPatients([]);
      return;
    }
    const timer = setTimeout(async () => {
      try {
        const res = await patientApi
            .getAll(searchPatient);
        setPatients(res.data);
      } catch {
        console.error('Erreur recherche patient');
      }
    }, 400);
    return () => clearTimeout(timer);
  }, [searchPatient]);

  // ================================================
  // Ajouter medicamento au panier
  // ================================================
  const ajouterAuPanier = (medicament) => {
    // Vérifier si le médicament nécessite une ordonnance
    const besoinOrdonnance = 
      medicament.statutLegal === 'ORDONNANCE' ||
      medicament.statutLegal === 'ORDONNANCE_SECURISEE';

    if (besoinOrdonnance && !hasOrdonnance) {
      toast.error(
        'Ce médicament nécessite une ordonnance. ' +
        'Cochez "Avec ordonnance" pour l\'ajouter.'
      );
      return;
    }

    const existant = panier.find(
      p => p.id === medicament.id
    );
    if (existant) {
      if (existant.quantite >=
          medicament.stockQuantiteTotale) {
        toast.warning('Stock maximum atteint !');
        return;
      }
      setPanier(panier.map(p =>
        p.id === medicament.id
          ? { ...p, quantite: p.quantite + 1 }
          : p
      ));
    } else {
      setPanier([
        ...panier,
        { ...medicament, quantite: 1 }
      ]);
    }
    setSearch('');
    setResultats([]);
    toast.success(`${medicament.denomination} ajouté`);
  };

  // ================================================
  // Modifier quantité dans le panier
  // ================================================
  const modifierQuantite = (id, nouvelleQte) => {
    setPanier(panier.map(p =>
      p.id === id
        ? { ...p, quantite: nouvelleQte }
        : p
    ));
  };

  // ================================================
  // Supprimer du panier
  // ================================================
  const supprimerDuPanier = (id) => {
    setPanier(panier.filter(p => p.id !== id));
  };

  // ================================================
  // Calculs du panier
  // ================================================
  const totalTtc = panier.reduce(
    (acc, item) =>
      acc + (Number(item.prixVenteTtc) * item.quantite),
    0
  );

  const totalRembourseSs = panier.reduce((acc, item) => {
    if (!item.estRemboursableSs) return acc;
    const taux = Number(item.tauxRemboursementSs) || 0;
    return acc + (Number(item.prixVenteTtc)
                  * item.quantite * taux / 100);
  }, 0);

  const aPayerClient = totalTtc - totalRembourseSs;

  // Vérifier si panier contient des médicaments nécessitant ordonnance
  const contientMedicamentOrdonnance = panier.some(
    item =>
      item.statutLegal === 'ORDONNANCE' ||
      item.statutLegal === 'ORDONNANCE_SECURISEE'
  );

  // ================================================
  // Valider la vente
  // ================================================
  const validerVente = async () => {
    if (panier.length === 0) {
      toast.warning('Le panier est vide !');
      return;
    }

    // Si le panier contient des médicaments avec ordonnance
    // et qu'on a coché "avec ordonnance", vérifier les infos
    if (searchPatient.trim() && !patient) {
      const validation = validateName(searchPatient.trim());
      if (validation !== true) {
        setPatientNomError(validation);
        toast.error(validation);
        return;
      }
    }

    if (hasOrdonnance) {
      if (!patient && !searchPatient.trim()) {
        toast.error('Veuillez sélectionner ou entrer un patient pour l\'ordonnance');
        return;
      }
      if (!ordonnanceInfo.numero.trim()) {
        toast.error('Veuillez entrer le numéro d\'ordonnance');
        return;
      }
      if (!ordonnanceInfo.medecinNom.trim()) {
        toast.error('Veuillez entrer le nom du médecin');
        return;
      }
      const medValidation = validateName(ordonnanceInfo.medecinNom.trim());
      if (medValidation !== true) {
        setMedecinNomError(medValidation);
        toast.error(medValidation);
        return;
      }
    }

    setLoading(true);
    try {
const montantRendu = montantPaye && aPayerClient
        ? Math.max(0, Number(montantPaye) - aPayerClient)
        : 0;

const payload = {
        patientId: patient?.id || null,
        patientNom: patient ? 
          `${patient.prenom} ${patient.nom}` : 
          (searchPatient?.trim() || null),
        hasOrdonnance: hasOrdonnance,
        numeroOrdonnance: hasOrdonnance ? 
          ordonnanceInfo.numero : null,
        medecinNom: hasOrdonnance ? 
          ordonnanceInfo.medecinNom : null,
        medecinRpps: hasOrdonnance && ordonnanceInfo.medecinRpps ? 
          ordonnanceInfo.medecinRpps : null,
        datePrescription: hasOrdonnance && ordonnanceInfo.datePrescription ? 
          ordonnanceInfo.datePrescription : null,
        aTiersPayant: hasOrdonnance ? 
          ordonnanceInfo.aTiersPayant : false,
        montantPaye: montantPaye ? Number(montantPaye) : aPayerClient,
        montantRendu: montantRendu,
        lignes: panier.map(item => ({
          medicamentId: item.id,
          quantite:     item.quantite,
        })),
      };

      const res = await venteApi.creerVente(payload);
      setVenteReussie(res.data);
      setPanier([]);
      setPatient(null);
      setSearchPatient('');
      setHasOrdonnance(false);
      setOrdonnanceInfo({ 
        numero: '', 
        medecinNom: '',
        medecinRpps: '',
        datePrescription: '',
        aTiersPayant: false
      });
      toast.success('Vente enregistrée ! 🎉');
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
        'Erreur lors de la vente'
      );
    } finally {
      setLoading(false);
    }
  };

  // ================================================
  // Télécharger le ticket PDF
  // ================================================
  const handleTelechargerTicket = async (venteId,
                                          reference) => {
    setTicketLoading(true);
    try {
      const res = await venteApi.getTicketPdf(venteId);
      venteApi.telechargerFichier(
        res.data,
        `ticket_${reference}.pdf`
      );
      toast.success('Ticket PDF téléchargé !');
    } catch {
      toast.error('Erreur génération du ticket');
    } finally {
      setTicketLoading(false);
    }
  };

  // ================================================
  // Affichage succès vente + ticket PDF
  // ================================================
  if (venteReussie) {
    return (
      <div className="max-w-lg mx-auto">
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-8
                        text-center">

          {/* Icône succès */}
          <div className="w-20 h-20 bg-green-100
                          rounded-full flex items-center
                          justify-center mx-auto mb-4">
            <CheckCircle size={40}
                         className="text-green-600" />
          </div>

          <h2 className="text-2xl font-bold
                         text-gray-800 mb-2">
            Vente enregistrée !
          </h2>

          <p className="text-gray-500 mb-1">
            Référence :
            <strong className="ml-1 text-gray-800">
              {venteReussie.reference}
            </strong>
          </p>

          {/* Total TTC */}
          <p className="text-3xl font-bold
                        text-primary-600 my-4">
            {Number(venteReussie.montantTotalTtc)
              .toLocaleString('fr-FR')} Ar
          </p>

          {/* Remboursement SS */}
          {Number(venteReussie.montantRembourseSs)
           > 0 && (
            <p className="text-green-600 text-sm mb-2">
              Remboursement SS : -
              {Number(venteReussie.montantRembourseSs)
                .toLocaleString('fr-FR')} Ar
            </p>
          )}

          {/* À payer */}
          <div className="bg-primary-50 rounded-xl
                          py-3 px-4 mb-6">
            <p className="text-sm text-primary-600
                           font-medium">
              À payer par le client
            </p>
            <p className="text-2xl font-bold
                          text-primary-700">
              {Number(venteReussie.montantPayeClient)
                .toLocaleString('fr-FR')} Ar
            </p>
          </div>

          {/* Détail lignes */}
          <div className="text-left bg-gray-50
                          rounded-xl p-4 mb-6">
            <h4 className="font-semibold text-sm
                           text-gray-700 mb-3">
              Détail de la vente :
            </h4>
            {venteReussie.lignes &&
             venteReussie.lignes.map((l, i) => (
              <div key={i}
                   className="flex justify-between
                              text-sm py-1.5 border-b
                              border-gray-100
                              last:border-0">
                <span className="text-gray-600">
                  {l.medicamentDenomination}
                  <span className="text-gray-400 ml-1
                                   text-xs">
                    x{l.quantite}
                  </span>
                </span>
                <span className="font-medium
                                 text-gray-800">
                  {Number(l.sousTotalTtc)
                    .toLocaleString('fr-FR')} Ar
                </span>
              </div>
            ))}
          </div>

          {/* ===== BOUTON TICKET PDF ===== */}
          <button
            onClick={() => handleTelechargerTicket(
              venteReussie.id,
              venteReussie.reference
            )}
            disabled={ticketLoading}
            className="w-full mb-3 flex items-center
                       justify-center gap-2
                       bg-green-600 hover:bg-green-700
                       disabled:bg-green-300
                       text-white py-3 rounded-xl
                       font-semibold text-sm
                       transition shadow-md">
            {ticketLoading ? (
              <>
                <div className="animate-spin
                                rounded-full h-5 w-5
                                border-b-2 border-white"/>
                Génération...
              </>
            ) : (
              <>
                <Printer size={18} />
                Télécharger le ticket PDF
              </>
            )}
          </button>

          {/* Bouton nouvelle vente */}
          <button
            onClick={() => setVenteReussie(null)}
            className="w-full bg-primary-600
                       hover:bg-primary-700 text-white
                       py-3 rounded-xl font-semibold
                       transition">
            Nouvelle vente
          </button>
        </div>
      </div>
    );
  }

  // ================================================
  // Interface principale POS
  // ================================================
  return (
    <div className="grid grid-cols-1 lg:grid-cols-3
                    gap-6 h-full">

      {/* Colonne gauche — Recherche + Résultats */}
      <div className="lg:col-span-2 space-y-4">

        {/* Barre de recherche médicament */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-4">
          <h3 className="font-semibold text-gray-800
                         mb-3 flex items-center gap-2">
            <Search size={18}
                    className="text-primary-600" />
            Rechercher un médicament
          </h3>
          <div className="relative">
            <Search size={18}
                    className="absolute left-3 top-1/2
                               -translate-y-1/2
                               text-gray-400" />
            <input
              type="text"
              placeholder="Nom, DCI ou code CIP..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              autoFocus
              className="w-full pl-10 pr-4 py-3
                         border-2 border-gray-200
                         rounded-xl text-sm
                         focus:outline-none
                         focus:border-primary-500
                         transition"
            />
          </div>

          {/* Résultats recherche */}
          {searching && (
            <div className="mt-3 text-center
                            text-gray-400 text-sm
                            py-4">
              <div className="animate-spin rounded-full
                              h-6 w-6 border-b-2
                              border-primary-500
                              mx-auto mb-2"/>
              Recherche en cours...
            </div>
          )}

          {resultats.length > 0 && (
            <div className="mt-3 space-y-2
                            max-h-72 overflow-y-auto">
              {resultats.map(m => (
                <button
                  key={m.id}
                  onClick={() => ajouterAuPanier(m)}
                  className="w-full text-left p-3
                             bg-gray-50
                             hover:bg-primary-50
                             hover:border-primary-300
                             border border-gray-100
                             rounded-xl transition
                             group">
                  <div className="flex items-center
                                  justify-between">
                    <div className="flex-1">
                      <p className="font-medium text-sm
                                    text-gray-800
                                    group-hover:
                                    text-primary-700">
                        {m.denomination}
                      </p>
                      <p className="text-xs text-gray-500">
                        {m.dci} — {m.formeGalenique}
                        {m.dosage && ` ${m.dosage}`}
                      </p>
                    </div>
                    <div className="text-right ml-4">
                      <p className="font-bold text-sm
                                    text-primary-600">
                        {Number(m.prixVenteTtc)
                          .toLocaleString('fr-FR')} Ar
                      </p>
                      <p className="text-xs text-gray-400">
                        Stock : {m.stockQuantiteTotale}
                      </p>
                    </div>
                    <Plus
                      size={18}
                      className="ml-3 text-primary-500
                                 opacity-0
                                 group-hover:opacity-100
                                 transition" />
                  </div>

                  {/* Badge ordonnance */}
                  {(m.statutLegal === 'ORDONNANCE' ||
                    m.statutLegal ===
                    'ORDONNANCE_SECURISEE') && (
                    <span className="mt-1 inline-block
                                     text-xs
                                     bg-orange-100
                                     text-orange-700
                                     px-2 py-0.5
                                     rounded">
                      📋 Ordonnance requise
                    </span>
                  )}

                  {/* Badge remboursable */}
                  {m.estRemboursableSs && (
                    <span className="mt-1 ml-1
                                     inline-block
                                     text-xs
                                     bg-green-100
                                     text-green-700
                                     px-2 py-0.5
                                     rounded">
                      ✅ Remboursable SS
                      {m.tauxRemboursementSs &&
                        ` (${m.tauxRemboursementSs}%)`}
                    </span>
                  )}
                </button>
              ))}
            </div>
          )}

          {search && !searching &&
           resultats.length === 0 && (
            <p className="mt-3 text-center text-sm
                          text-gray-400 py-4">
              Aucun médicament trouvé pour "{search}"
            </p>
          )}
        </div>

        {/* Section Patient / Client - Simplifiée */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-4">
          <h3 className="font-semibold text-gray-800
                         mb-3 flex items-center gap-2">
            <User size={18}
                  className="text-primary-600" />
            Patient / Client
            <span className="text-gray-400 text-xs
                             font-normal">
              (optionnel)
            </span>
          </h3>

          {patient ? (
            <div className="flex items-center
                            justify-between p-3
                            bg-green-50 rounded-xl
                            border border-green-200">
              <div>
                <p className="font-medium text-green-800">
                  {patient.prenom} {patient.nom}
                </p>
                <p className="text-xs text-green-600">
                  {patient.telephone || 'Pas de tél.'}
                  {patient.numSecuSociale &&
                    ` — N° SS: ${patient.numSecuSociale}`}
                </p>
                {patient.allergiesConnues && (
                  <p className="text-xs text-orange-600
                                 mt-1">
                    ⚠️ Allergies : {patient.allergiesConnues}
                  </p>
                )}
              </div>
              <button
                onClick={() => {
                  setPatient(null);
                  setSearchPatient('');
                  setPatients([]);
                }}
                className="py-3 hover:bg-primary-50 transition text-sm border-b border-gray-100 last:border-0">
                Changer
              </button>
            </div>
          ) : (
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium
                                   text-gray-700 mb-1">
                  Nom patient / client
                </label>
                <input
                  type="text"
                  placeholder="Entrez le nom du patient ou du client"
                  value={searchPatient}
                  onChange={e => {
                    setSearchPatient(
                      sanitizeNameInput(e.target.value)
                    );
                    if (patientNomError) {
                      setPatientNomError('');
                    }
                  }}
                  onBlur={() => {
                    if (searchPatient.trim()) {
                      const validation = validateName(searchPatient.trim());
                      if (validation !== true) {
                        setPatientNomError(validation);
                      }
                    } else {
                      setPatientNomError('');
                    }
                  }}
                  className="w-full px-3 py-2 border
                             border-gray-300 rounded-lg
                             text-sm focus:outline-none
                             focus:border-primary-500
                             bg-white transition"
                />
                {patientNomError && (
                  <p className="mt-2 text-xs text-red-600">
                    {patientNomError}
                  </p>
                )}
              </div>

              {patients.length > 0 && (
                <div className="max-h-36 overflow-y-auto
                                bg-white border
                                border-gray-200 rounded-lg">
                  {patients.map(p => (
                    <button
                      key={p.id}
                      type="button"
                      onClick={() => {
                        setPatient(p);
                        setSearchPatient('');
                        setPatients([]);
                      }}
                      className="w-full text-left px-3
                                 py-2 hover:bg-gray-50
                                 text-sm border-b
                                 border-gray-100
                                 last:border-0">
                      <span className="font-medium
                                       text-gray-800">
                        {p.prenom} {p.nom}
                      </span>
                      {p.telephone && (
                        <span className="text-gray-400
                                         ml-2 text-xs">
                          {p.telephone}
                        </span>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Section Ordonnance - Radio button */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-4">
          <h3 className="font-semibold text-gray-800
                         mb-3 flex items-center gap-2">
            <FileText size={18}
                  className="text-primary-600" />
            Ordonnance
            <span className="text-gray-400 text-xs
                             font-normal">
              (pour médicaments sur ordonnance)
            </span>
          </h3>

          <div className="flex gap-4 mb-4">
            <label className="flex items-center gap-2
                             cursor-pointer">
              <input
                type="radio"
                name="ordonnance"
                checked={!hasOrdonnance}
                onChange={() => {
                  setHasOrdonnance(false);
                  setOrdonnanceInfo({ 
                    numero: '', 
                    medecinNom: '',
                    medecinRpps: '',
                    datePrescription: '',
                    aTiersPayant: false
                  });
                }}
                className="w-4 h-4 text-primary-600
                           border-gray-300 focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">
                Sans ordonnance
              </span>
            </label>
            <label className="flex items-center gap-2
                             cursor-pointer">
              <input
                type="radio"
                name="ordonnance"
                checked={hasOrdonnance}
                onChange={() => setHasOrdonnance(true)}
                className="w-4 h-4 text-orange-600
                           border-gray-300 focus:ring-orange-500"
              />
              <span className="text-sm text-gray-700 font-medium">
                Avec ordonnance
              </span>
            </label>
          </div>

          {/* Info ordonnance - apparaît si coché */}
          {hasOrdonnance && (
            <div className="space-y-3 p-4 bg-orange-50
                            rounded-xl border border-orange-200">
              {/* Patient pour l'ordonnance */}
              <div>
                <label className="block text-xs font-medium
                                   text-orange-700 mb-1">
                  Patient *
                </label>
                {patient ? (
                  <div className="flex items-center
                                  justify-between p-2
                                  bg-white rounded-lg border
                                  border-orange-200">
                    <div>
                      <p className="text-sm font-medium
                                     text-gray-800">
                        {patient.prenom} {patient.nom}
                      </p>
                      {patient.allergiesConnues && (
                        <p className="text-xs text-orange-600">
                          ⚠️ Allergies: {patient.allergiesConnues}
                        </p>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={() => setPatient(null)}
                      className="text-orange-500 text-xs
                                 hover:underline">
                      Changer
                    </button>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <div className="relative">
                      <Search size={16}
                              className="absolute left-3
                                         top-1/2 -translate-y-1/2
                                         text-gray-400" />
                      <input
                        type="text"
                        placeholder="Rechercher patient..."
                        value={searchPatient}
                        onChange={e =>
                          setSearchPatient(e.target.value)}
                        className="w-full pl-9 pr-3 py-2
                                   border border-orange-300
                                   rounded-lg text-sm
                                   focus:outline-none
                                   focus:border-orange-500
                                   bg-white transition"
                      />
                    </div>
                    {patients.length > 0 && (
                      <div className="max-h-32 overflow-y-auto
                                      bg-white border
                                      border-orange-200 rounded-lg">
                        {patients.map(p => (
                          <button
                            key={p.id}
                            type="button"
                            onClick={() => {
                              setPatient(p);
                              setSearchPatient('');
                              setPatients([]);
                            }}
                            className="w-full text-left px-3
                                       py-2 hover:bg-orange-50
                                       text-sm border-b
                                       border-orange-100
                                       last:border-0">
                            <span className="font-medium
                                             text-gray-800">
                              {p.prenom} {p.nom}
                            </span>
                            {p.telephone && (
                              <span className="text-gray-400
                                               ml-2 text-xs">
                                {p.telephone}
                              </span>
                            )}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Numéro d'ordonnance */}
              <div>
                <label className="block text-xs font-medium
                                   text-orange-700 mb-1">
                  Numéro d'ordonnance *
                </label>
                <input
                  type="text"
                  placeholder="Ex: ORD-2025-001234"
                  value={ordonnanceInfo.numero}
                  onChange={e => setOrdonnanceInfo({
                    ...ordonnanceInfo,
                    numero: e.target.value
                  })}
                  className="w-full px-3 py-2 border
                             border-orange-300 rounded-lg
                             text-sm focus:outline-none
                             focus:border-orange-500
                             bg-white transition"
                />
              </div>

              {/* Médecin et RPPS */}
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs font-medium
                                     text-orange-700 mb-1">
                    Médecin *
                  </label>
                  <input
                    type="text"
                    placeholder="Dr. Nom médecin"
                    value={ordonnanceInfo.medecinNom}
                    onChange={e => {
                      setOrdonnanceInfo({
                        ...ordonnanceInfo,
                        medecinNom: e.target.value
                      });
                      if (medecinNomError) {
                        setMedecinNomError('');
                      }
                    }}
                    onBlur={() => {
                      if (ordonnanceInfo.medecinNom.trim()) {
                        const validation = validateName(
                          ordonnanceInfo.medecinNom.trim()
                        );
                        if (validation !== true) {
                          setMedecinNomError(validation);
                        }
                      }
                    }}
                    className="w-full px-3 py-2 border
                               border-orange-300 rounded-lg
                               text-sm focus:outline-none
                               focus:border-orange-500
                               bg-white transition"
                  />
                  {medecinNomError && (
                    <p className="text-xs text-red-600 mt-2">
                      {medecinNomError}
                    </p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-medium
                                     text-orange-700 mb-1">
                    N° RPPS
                  </label>
                  <input
                    type="text"
                    placeholder="Ex: 10005678901"
                    value={ordonnanceInfo.medecinRpps || ''}
                    onChange={e => setOrdonnanceInfo({
                      ...ordonnanceInfo,
                      medecinRpps: e.target.value
                    })}
                    className="w-full px-3 py-2 border
                               border-orange-300 rounded-lg
                               text-sm focus:outline-none
                               focus:border-orange-500
                               bg-white transition"
                  />
                </div>
              </div>

              {/* Date de prescription */}
              <div>
                <label className="block text-xs font-medium
                                   text-orange-700 mb-1">
                  Date de prescription
                </label>
                <input
                  type="date"
                  value={ordonnanceInfo.datePrescription || ''}
                  onChange={e => setOrdonnanceInfo({
                    ...ordonnanceInfo,
                    datePrescription: e.target.value
                  })}
                  className="w-full px-3 py-2 border
                             border-orange-300 rounded-lg
                             text-sm focus:outline-none
                             focus:border-orange-500
                             bg-white transition"
                />
              </div>

              {/* Tiers payant */}
              <div>
                <label className="block text-xs font-medium
                                   text-orange-700 mb-1">
                  Tiers payant
                </label>
                <div className="flex items-center gap-4">
                  <label className="flex items-center gap-1
                                     cursor-pointer">
                    <input
                      type="radio"
                      name="tiersPayant"
                      checked={!ordonnanceInfo.aTiersPayant}
                      onChange={() => setOrdonnanceInfo({
                        ...ordonnanceInfo,
                        aTiersPayant: false
                      })}
                      className="w-4 h-4 text-orange-600"
                    />
                    <span className="text-sm text-gray-700">
                      Non
                    </span>
                  </label>
                  <label className="flex items-center gap-1
                                     cursor-pointer">
                    <input
                      type="radio"
                      name="tiersPayant"
                      checked={ordonnanceInfo.aTiersPayant}
                      onChange={() => setOrdonnanceInfo({
                        ...ordonnanceInfo,
                        aTiersPayant: true
                      })}
                      className="w-4 h-4 text-orange-600"
                    />
                    <span className="text-sm text-gray-700">
                      Oui
                    </span>
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* Info si pas d'ordonnance sélectionnée mais medicament ordonnance dans panier */}
          {!hasOrdonnance && contientMedicamentOrdonnance && (
            <div className="mt-3 p-3 bg-red-50 rounded-xl
                            border border-red-200 flex items-center
                            gap-2">
              <AlertCircle size={16} className="text-red-500" />
              <p className="text-xs text-red-600">
                Le panier contient des médicaments sur ordonnance.
                Cochez "Avec ordonnance" pour les vendre.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Colonne droite — Panier */}
      <div className="space-y-4">
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-4
                        sticky top-6">

          <h3 className="font-semibold text-gray-800
                         mb-4 flex items-center gap-2">
            <ShoppingCart size={18}
                          className="text-primary-600" />
            Panier
            {panier.length > 0 && (
              <span className="bg-primary-100
                               text-primary-700 text-xs
                               font-bold px-2 py-0.5
                               rounded-full">
                {panier.length}
              </span>
            )}
          </h3>

          {/* Items panier */}
          {panier.length === 0 ? (
            <div className="text-center py-8
                            text-gray-300">
              <ShoppingCart size={48}
                            className="mx-auto mb-3" />
              <p className="text-sm font-medium">
                Panier vide
              </p>
              <p className="text-xs mt-1">
                Recherchez un médicament pour commencer
              </p>
            </div>
          ) : (
            <div className="space-y-2 mb-4
                            max-h-64 overflow-y-auto">
              {panier.map(item => (
                <PanierItem
                  key={item.id}
                  item={item}
                  onUpdate={modifierQuantite}
                  onRemove={supprimerDuPanier}
                />
              ))}
            </div>
          )}

          {/* Mode paiement + Totaux + Boutons */}
          {panier.length > 0 && (
            <>
              {/* Montant payé et rendu */}
              <div className="mb-4">
                <label className="block text-sm
                                   font-medium
                                   text-gray-700 mb-2">
                  <CreditCard size={14}
                              className="inline mr-1" />
                  Paiement en espèces
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs
                                       text-gray-500 mb-1">
                      Montant donné
                    </label>
                    <input
                      type="number"
                      placeholder="0"
                      value={montantPaye}
                      onChange={e => 
                        setMontantPaye(e.target.value)}
                      className="w-full px-3 py-2
                                 border border-gray-200
                                 rounded-xl text-sm
                                 focus:outline-none
                                 focus:border-primary-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs
                                       text-gray-500 mb-1">
                      Montant rendu
                    </label>
                    <div className="w-full px-3 py-2
                               bg-gray-100 rounded-xl
                               text-sm font-medium
                               text-gray-700">
                      {montantPaye && aPayerClient
                        ? Math.max(0, Number(montantPaye) - aPayerClient)
                          .toLocaleString('fr-FR')
                        : '0'} Ar
                    </div>
                  </div>
                </div>
              </div>

              {/* Totaux */}
              <div className="border-t border-gray-100
                              pt-4 space-y-2">
                <div className="flex justify-between
                                text-sm text-gray-600">
                  <span>Sous-total TTC</span>
                  <span>
                    {totalTtc.toLocaleString('fr-FR')} Ar
                  </span>
                </div>
                {totalRembourseSs > 0 && (
                  <div className="flex justify-between
                                  text-sm text-green-600">
                    <span>Remboursement SS</span>
                    <span>
                      -{totalRembourseSs
                        .toLocaleString('fr-FR')} Ar
                    </span>
                  </div>
                )}
                <div className="flex justify-between
                                font-bold text-lg
                                text-gray-800 border-t
                                border-gray-100 pt-2">
                  <span>Total à payer</span>
                  <span className="text-primary-600">
                    {aPayerClient
                      .toLocaleString('fr-FR')} Ar
                  </span>
                </div>
              </div>

              {/* Alerte ordonnance manquante */}
              {contientMedicamentOrdonnance && !hasOrdonnance && (
                <div className="mt-3 p-3 bg-red-50
                                rounded-xl border
                                border-red-200 text-xs
                                text-red-600
                                flex items-center gap-2">
                  ⚠️ Ordonnance manquante pour
                  certains médicaments du panier
                </div>
              )}

              {/* Bouton valider vente */}
              <button
                onClick={validerVente}
                disabled={
                  loading ||
                  (contientMedicamentOrdonnance && !hasOrdonnance)
                }
                className="w-full mt-4 bg-green-600
                           hover:bg-green-700
                           disabled:bg-gray-300
                           disabled:cursor-not-allowed
                           text-white py-3 rounded-xl
                           font-semibold text-sm
                           flex items-center
                           justify-center gap-2
                           transition shadow-md">
                {loading ? (
                  <>
                    <div className="animate-spin
                                    rounded-full h-5 w-5
                                    border-b-2
                                    border-white"/>
                    Traitement en cours...
                  </>
                ) : (
                  <>
                    <CheckCircle size={18} />
                    Valider la vente
                  </>
                )}
              </button>

              {/* Vider panier */}
              <button
                onClick={() => {
                  if (window.confirm(
                    'Vider le panier ?'
                  )) setPanier([]);
                }}
                className="w-full mt-2 text-red-500
                           hover:bg-red-50 py-2
                           rounded-xl text-sm
                           transition font-medium">
                Vider le panier
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default VentePage;
