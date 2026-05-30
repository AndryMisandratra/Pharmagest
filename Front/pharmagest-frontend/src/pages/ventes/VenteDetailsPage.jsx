import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  ArrowLeft, ShoppingCart, User,
  CreditCard, Calendar, Package,
  CheckCircle, Printer
} from 'lucide-react';
import venteApi from '../../api/venteApi';

// Badge statut vente
const StatutBadge = ({ statut }) => {
  const styles = {
    VALIDEE:    'bg-green-100 text-green-700',
    REMBOURSEE: 'bg-blue-100 text-blue-700'
  };
  const icons = {
    VALIDEE:    <CheckCircle size={14} />,
    REMBOURSEE: <CheckCircle size={14} />
  };
  const labels = {
    VALIDEE:    'Validée',
    REMBOURSEE: 'Remboursée'
  };

  return (
    <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${styles[statut] || 'bg-gray-100 text-gray-700'}`}>
      {icons[statut] || <CheckCircle size={14} />}
      {labels[statut] || statut}
    </span>
  );
};

// Badge mode paiement
const PaiementBadge = ({ mode }) => {
  return (
    <span className="px-3 py-1 bg-gray-100
                     text-gray-700 rounded-full
                     text-sm font-medium">
      💵 Espèces
    </span>
  );
};

const VenteDetailPage = () => {
  const navigate         = useNavigate();
  const { id }           = useParams();

  const [vente, setVente]         = useState(null);
  const [loading, setLoading]     = useState(true);
  const [ticketLoading, setTicketLoading] =
      useState(false);

  useEffect(() => {
    fetchVente();
  }, [id]);

  const fetchVente = async () => {
    setLoading(true);
    try {
      const res = await venteApi.getById(id);
      setVente(res.data);
    } catch {
      toast.error('Vente non trouvée');
      navigate('/ventes');
    } finally {
      setLoading(false);
    }
  };

  // ================================================
  // Télécharger le ticket PDF
  // ================================================
  const handleTelechargerTicket = async () => {
    setTicketLoading(true);
    try {
      const res = await venteApi.getTicketPdf(id);
      venteApi.telechargerFichier(
        res.data,
        `ticket_${vente.reference}.pdf`
      );
      toast.success('Ticket PDF téléchargé !');
    } catch {
      toast.error('Erreur génération du ticket');
    } finally {
      setTicketLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center
                      justify-center h-64">
        <div className="animate-spin rounded-full
                        h-10 w-10 border-b-2
                        border-primary-600"/>
      </div>
    );
  }

  if (!vente) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* En-tête */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/ventes')}
          className="p-2 hover:bg-gray-100
                     rounded-xl transition">
          <ArrowLeft size={20}
                     className="text-gray-600" />
        </button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h2 className="text-2xl font-bold text-gray-800">
              Vente {vente.reference}
            </h2>
            <StatutBadge statut={vente.statut} />
          </div>
          <p className="text-gray-500 text-sm mt-1">
            Détails de la vente
          </p>
        </div>
      </div>

      {/* Boutons d'action */}
      <div className="flex gap-3">
        <button
          onClick={handleTelechargerTicket}
          disabled={ticketLoading}
          className="flex items-center gap-2
                     bg-primary-600 hover:bg-primary-700
                     disabled:bg-primary-300
                     text-white px-4 py-2 rounded-xl
                     text-sm transition font-medium
                     shadow-md">
          {ticketLoading ? (
            <>
              <div className="animate-spin
                              rounded-full h-4 w-4
                              border-b-2
                              border-white"/>
              Génération...
            </>
          ) : (
            <>
              <Printer size={16} />
              Ticket PDF
            </>
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2
                      gap-6">

        {/* Infos générales */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800
                         mb-4 flex items-center gap-2">
            <ShoppingCart size={18}
                          className="text-primary-600" />
            Informations de la vente
          </h3>
          <div className="space-y-3">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">
                Référence
              </span>
              <span className="font-medium text-gray-800">
                {vente.reference}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">
                Date
              </span>
              <span className="font-medium text-gray-800">
                {vente.effectueLe
                  ? new Date(vente.effectueLe)
                      .toLocaleString('fr-FR')
                  : '—'}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">
                Vendeur
              </span>
              <span className="font-medium text-gray-800">
                {vente.vendeurPrenom} {vente.vendeurNom}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">
                Mode de paiement
              </span>
              <PaiementBadge mode={vente.modePaiement} />
            </div>
          </div>
        </div>

        {/* Infos patient */}
        <div className="bg-white rounded-2xl shadow-sm
                        border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800
                         mb-4 flex items-center gap-2">
            <User size={18}
                  className="text-primary-600" />
            Client / Patient
          </h3>
          {vente.patientNom ? (
            <div className="space-y-2">
              <p className="font-medium text-gray-800">
                {vente.patientPrenom
                  ? `${vente.patientPrenom} ${vente.patientNom}`
                  : vente.patientNom}
              </p>
              {vente.patientId && (
                <p className="text-xs text-gray-500">
                  ID Patient: {vente.patientId}
                </p>
              )}
            </div>
          ) : (
            <p className="text-gray-400 text-sm">
              Client non identifié
            </p>
          )}
          
          {/* Infos ordonnance */}
          {vente.hasOrdonnance && (
            <div className="mt-4 pt-4 border-t
                            border-gray-100">
              <h4 className="text-sm font-medium
                             text-orange-700 mb-2">
                📋 Ordonnance
              </h4>
              <div className="space-y-1 text-sm">
                <p>
                  <span className="text-gray-500">
                    N°:
                  </span>
                  <span className="ml-2 font-medium">
                    {vente.numeroOrdonnance}
                  </span>
                </p>
                <p>
                  <span className="text-gray-500">
                    Médecin:
                  </span>
                  <span className="ml-2">
                    {vente.medecinNom}
                  </span>
                </p>
                {vente.medecinRpps && (
                  <p>
                    <span className="text-gray-500">
                      RPPS:
                    </span>
                    <span className="ml-2">
                      {vente.medecinRpps}
                    </span>
                  </p>
                )}
                {vente.datePrescription && (
                  <p>
                    <span className="text-gray-500">
                      Date:
                    </span>
                    <span className="ml-2">
                      {vente.datePrescription}
                    </span>
                  </p>
                )}
                {vente.aTiersPayant && (
                  <p className="text-green-600">
                    ✅ Tiers payant
                  </p>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Détail lignes */}
        <div className="md:col-span-2 bg-white rounded-2xl
                        shadow-sm border border-gray-100
                        p-6">
          <h3 className="font-semibold text-gray-800
                         mb-4 flex items-center gap-2">
            <Package size={18}
                      className="text-primary-600" />
            Médicaments vendus
          </h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="text-left py-2 px-2
                                  font-medium text-gray-500">
                    Médicament
                  </th>
                  <th className="text-center py-2 px-2
                                  font-medium text-gray-500">
                    Qté
                  </th>
                  <th className="text-right py-2 px-2
                                  font-medium text-gray-500">
                    Prix unit.
                  </th>
                  <th className="text-right py-2 px-2
                                  font-medium text-gray-500">
                    Sous-total
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {vente.lignes.map((ligne, idx) => (
                  <tr key={idx}>
                    <td className="py-2 px-2">
                      <p className="font-medium
                                     text-gray-800">
                        {ligne.medicamentDenomination}
                      </p>
                      <p className="text-xs text-gray-400">
                        Lot: {ligne.lotNumero}
                      </p>
                    </td>
                    <td className="py-2 px-2
                                   text-center">
                      {ligne.quantite}
                    </td>
                    <td className="py-2 px-2
                                   text-right">
                      {Number(ligne.prixUnitaireTtc)
                        .toLocaleString('fr-FR')} Ar
                    </td>
                    <td className="py-2 px-2
                                   text-right font-medium">
                      {Number(ligne.sousTotalTtc)
                        .toLocaleString('fr-FR')} Ar
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Totaux */}
        <div className="md:col-span-2 bg-gray-50 rounded-2xl
                        p-6">
          <div className="space-y-2">
            <div className="flex justify-between
                            text-sm">
              <span className="text-gray-500">
                Total HT
              </span>
              <span className="font-medium text-gray-800">
                {Number(vente.montantTotalHt)
                  .toLocaleString('fr-FR')} Ar
              </span>
            </div>
            <div className="flex justify-between
                            text-sm">
              <span className="text-gray-500">
                TVA
              </span>
              <span className="font-medium text-gray-800">
                {Number(vente.montantTva)
                  .toLocaleString('fr-FR')} Ar
              </span>
            </div>
            {Number(vente.montantRembourseSs) > 0 && (
              <div className="flex justify-between
                              text-sm text-green-600">
                <span>Remboursement SS</span>
                <span>
                  -{Number(vente.montantRembourseSs)
                    .toLocaleString('fr-FR')} Ar
                </span>
              </div>
            )}
            <div className="flex justify-between
                            font-bold text-lg
                            border-t border-gray-200
                            pt-2">
              <span>Total TTC</span>
              <span className="text-primary-600">
                {Number(vente.montantTotalTtc)
                  .toLocaleString('fr-FR')} Ar
              </span>
            </div>
            {Number(vente.montantPayeClient) <
              Number(vente.montantTotalTtc) && (
              <div className="flex justify-between
                              font-bold text-green-600">
                <span>À payer par client</span>
                <span>
                  {Number(vente.montantPayeClient)
                    .toLocaleString('fr-FR')} Ar
                </span>
              </div>
            )}
            <div className="flex justify-between
                            text-sm text-gray-600
                            pt-2 border-t border-gray-200">
              <span>Montant payé</span>
              <span>
                {vente.montantPaye 
                  ? Number(vente.montantPaye)
                    .toLocaleString('fr-FR')
                  : Number(vente.montantPayeClient)
                    .toLocaleString('fr-FR')} Ar
              </span>
            </div>
            <div className="flex justify-between
                            text-sm text-green-600">
              <span>Montant rendu</span>
              <span>
                {vente.montantRendu 
                  ? Number(vente.montantRendu)
                    .toLocaleString('fr-FR')
                  : '0'} Ar
              </span>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default VenteDetailPage;