package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.LigneVenteRequest;
import com.Web.Pharmagest.dto.request.VenteRequest;
import com.Web.Pharmagest.entity.LigneVente;
import com.Web.Pharmagest.entity.LotStock;
import com.Web.Pharmagest.entity.Medicament;
import com.Web.Pharmagest.entity.Patient;
import com.Web.Pharmagest.entity.Utilisateur;
import com.Web.Pharmagest.entity.Vente;
import com.Web.Pharmagest.repository.LigneVenteRepository;
import com.Web.Pharmagest.repository.LotStockRepository;
import com.Web.Pharmagest.repository.MedicamentRepository;
import com.Web.Pharmagest.repository.MouvementStockRepository;
import com.Web.Pharmagest.repository.OrdonnanceRepository;
import com.Web.Pharmagest.repository.PatientRepository;
import com.Web.Pharmagest.repository.UtilisateurRepository;
import com.Web.Pharmagest.repository.VenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenteServiceTest {

    @Mock
    private VenteRepository venteRepository;
    @Mock
    private LigneVenteRepository ligneVenteRepository;
    @Mock
    private MedicamentRepository medicamentRepository;
    @Mock
    private LotStockRepository lotStockRepository;
    @Mock
    private MouvementStockRepository mouvementStockRepository;
    @Mock
    private OrdonnanceRepository ordonnanceRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private OrdonnanceService ordonnanceService;

    @InjectMocks
    private VenteService venteService;

    @Test
    void creerVente_appliqueMutuelleApresRemboursementSs() {
        String emailVendeur = "caissier@pharmagest.test";

        Utilisateur vendeur = new Utilisateur();
        vendeur.setId(1L);
        vendeur.setEmail(emailVendeur);
        when(utilisateurRepository.findByEmail(emailVendeur))
                .thenReturn(Optional.of(vendeur));

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setTauxMutuelle(new BigDecimal("20.00"));
        when(patientRepository.findById(2L))
                .thenReturn(Optional.of(patient));

        Medicament medicament = new Medicament();
        medicament.setId(10L);
        medicament.setPrixVenteTtc(new BigDecimal("100.00"));
        medicament.setPrixAchatHt(new BigDecimal("70.00"));
        medicament.setTauxTva(new BigDecimal("20.00"));
        medicament.setEstRemboursableSs(true);
        medicament.setTauxRemboursementSs(new BigDecimal("65.00"));
        medicament.setStockQuantiteTotale(5);
        when(medicamentRepository.findById(10L))
                .thenReturn(Optional.of(medicament));

        LotStock lot = new LotStock();
        lot.setId(11L);
        lot.setQuantiteRestante(5);
        lot.setEstActif(true);
        when(lotStockRepository.findLotsDisponiblesFEFO(10L))
                .thenReturn(List.of(lot));

        when(venteRepository.save(any(Vente.class)))
                .thenAnswer(invocation -> {
                    Vente saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(1L);
                    }
                    return saved;
                });
        when(ligneVenteRepository.save(any(LigneVente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mouvementStockRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VenteRequest request = new VenteRequest();
        request.setPatientId(2L);
        request.setHasOrdonnance(false);
        LigneVenteRequest ligne = new LigneVenteRequest();
        ligne.setMedicamentId(10L);
        ligne.setQuantite(1);
        request.setLignes(List.of(ligne));

        venteService.creerVente(request, emailVendeur);

        ArgumentCaptor<Vente> venteCaptor = ArgumentCaptor.forClass(Vente.class);
        verify(venteRepository, atLeastOnce()).save(venteCaptor.capture());
        List<Vente> savedValues = venteCaptor.getAllValues();
        Vente finalVente = savedValues.get(savedValues.size() - 1);

        assertThat(finalVente.getMontantTotalTtc())
                .isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(finalVente.getMontantRembourseSs())
                .isEqualByComparingTo(new BigDecimal("65.00"));
        assertThat(finalVente.getMontantRembourseMutuelle())
                .isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(finalVente.getMontantPayeClient())
                .isEqualByComparingTo(new BigDecimal("28.00"));
    }
}
