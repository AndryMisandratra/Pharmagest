package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.MedicamentRequest;
import com.Web.Pharmagest.dto.response.MedicamentResponse;
import com.Web.Pharmagest.entity.Medicament;
import com.Web.Pharmagest.repository.CategorieMedicamentRepository;
import com.Web.Pharmagest.repository.FournisseurRepository;
import com.Web.Pharmagest.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // toutes les méthodes sont en lecture seule
// sauf celles annotées @Transactional
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;
    private final CategorieMedicamentRepository categorieRepository;
    private final FournisseurRepository fournisseurRepository;

    // ================================================
    // CRÉER un médicament
    // ================================================
    @Transactional  // écriture → override readOnly
    public MedicamentResponse creerMedicament(MedicamentRequest request) {

        // Vérifier que le code CIP n'existe pas déjà
        if (medicamentRepository.existsByCodeCip(request.getCodeCip())) {
            throw new RuntimeException(
                    "Code CIP déjà existant : " + request.getCodeCip()
            );
        }

        // Charger la catégorie
        var categorie = categorieRepository
                .findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException(
                        "Catégorie non trouvée : " + request.getCategorieId()
                ));

        // Charger le fournisseur (optionnel)
        var fournisseur = request.getFournisseurId() != null
                ? fournisseurRepository
                .findById(request.getFournisseurId())
                .orElseThrow(() -> new RuntimeException(
                        "Fournisseur non trouvé"
                ))
                : null;

        // Construire l'entité avec le Builder Lombok
        var medicament = Medicament.builder()
                .codeCip(request.getCodeCip())
                .denomination(request.getDenomination())
                .dci(request.getDci())
                .formeGalenique(request.getFormeGalenique())
                .dosage(request.getDosage())
                .categorie(categorie)
                .fournisseur(fournisseur)
                .statutLegal(Medicament.StatutLegal
                        .valueOf(request.getStatutLegal()))
                .prixVenteTtc(request.getPrixVenteTtc())
                .prixAchatHt(request.getPrixAchatHt())
                .tauxTva(request.getTauxTva())
                .stockSeuilAlerte(request.getStockSeuilAlerte())
                .emplacement(request.getEmplacement())
                .fabricant(request.getFabricant())
                .estRemboursableSs(request.getEstRemboursableSs() != null
                        ? request.getEstRemboursableSs() : false)
                .tauxRemboursementSs(request.getTauxRemboursementSs())
                .build();

        // Sauvegarder en BDD
        var saved = medicamentRepository.save(medicament);
        return toResponse(saved);
    }

    // ================================================
    // MODIFIER un médicament
    // ================================================
    @Transactional
    public MedicamentResponse modifierMedicament(Long id,
                                                 MedicamentRequest request) {
        var medicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé : " + id
                ));

        // Vérifier CIP uniquement si changé
        if (!medicament.getCodeCip().equals(request.getCodeCip())
                && medicamentRepository.existsByCodeCip(request.getCodeCip())) {
            throw new RuntimeException(
                    "Code CIP déjà existant : " + request.getCodeCip()
            );
        }

        var categorie = categorieRepository
                .findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException(
                        "Catégorie non trouvée"
                ));

        var fournisseur = request.getFournisseurId() != null
                ? fournisseurRepository
                .findById(request.getFournisseurId())
                .orElse(null)
                : null;

        // Mettre à jour les champs
        medicament.setCodeCip(request.getCodeCip());
        medicament.setDenomination(request.getDenomination());
        medicament.setDci(request.getDci());
        medicament.setFormeGalenique(request.getFormeGalenique());
        medicament.setDosage(request.getDosage());
        medicament.setCategorie(categorie);
        medicament.setFournisseur(fournisseur);
        medicament.setStatutLegal(Medicament.StatutLegal
                .valueOf(request.getStatutLegal()));
        medicament.setPrixVenteTtc(request.getPrixVenteTtc());
        medicament.setPrixAchatHt(request.getPrixAchatHt());
        medicament.setTauxTva(request.getTauxTva());
        medicament.setStockSeuilAlerte(request.getStockSeuilAlerte());
        medicament.setEmplacement(request.getEmplacement());
        medicament.setFabricant(request.getFabricant());
        medicament.setEstRemboursableSs(request.getEstRemboursableSs());
        medicament.setTauxRemboursementSs(request.getTauxRemboursementSs());

        return toResponse(medicamentRepository.save(medicament));
    }

    // ================================================
    // ARCHIVER / RÉACTIVER un médicament
    // (jamais de suppression physique selon le CDC)
    // ================================================
    @Transactional
    public MedicamentResponse changerDisponibilite(Long id,
                                                   Boolean disponible) {
        var medicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé : " + id
                ));
        medicament.setEstDisponible(disponible);
        return toResponse(medicamentRepository.save(medicament));
    }

    // ================================================
    // LIRE — Tous les médicaments
    // ================================================
    public List<MedicamentResponse> listerMedicaments() {
        return medicamentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // LIRE — Un médicament par ID
    // ================================================
    public MedicamentResponse getMedicamentById(Long id) {
        var medicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé : " + id
                ));
        return toResponse(medicament);
    }

    // ================================================
    // LIRE — Par code CIP
    // ================================================
    public MedicamentResponse getMedicamentByCip(String cip) {
        var medicament = medicamentRepository.findByCodeCip(cip)
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé avec CIP : " + cip
                ));
        return toResponse(medicament);
    }

    // ================================================
    // RECHERCHE — Par nom, DCI ou CIP
    // ================================================
    public List<MedicamentResponse> rechercher(String search) {
        return medicamentRepository.rechercherMedicaments(search)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // ALERTES STOCK
    // ================================================
    public List<MedicamentResponse> getMedicamentsStockBas() {
        return medicamentRepository.findMedicamentsStockBas()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MedicamentResponse> getMedicamentsStockFaible() {
        return medicamentRepository.findMedicamentsStockFaible()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // CONVERSION Entité → DTO (méthode privée réutilisable)
    // ================================================
    private MedicamentResponse toResponse(Medicament m) {

        // Calcul du niveau d'alerte
        String niveauAlerte = "NORMAL";
        if (m.getStockQuantiteTotale() <= m.getStockSeuilAlerte()) {
            niveauAlerte = "ROUGE";
        } else if (m.getStockQuantiteTotale()
                <= m.getStockSeuilAlerte() * 2) {
            niveauAlerte = "ORANGE";
        }

        return MedicamentResponse.builder()
                .id(m.getId())
                .codeCip(m.getCodeCip())
                .denomination(m.getDenomination())
                .dci(m.getDci())
                .formeGalenique(m.getFormeGalenique())
                .dosage(m.getDosage())
                .categorieId(m.getCategorie().getId())
                .categorieLibelle(m.getCategorie().getLibelle())
                .fournisseurId(m.getFournisseur() != null
                        ? m.getFournisseur().getId() : null)
                .fournisseurRaisonSociale(m.getFournisseur() != null
                        ? m.getFournisseur().getRaisonSociale() : null)
                .statutLegal(m.getStatutLegal().name())
                .prixVenteTtc(m.getPrixVenteTtc())
                .prixAchatHt(m.getPrixAchatHt())
                .tauxTva(m.getTauxTva())
                .stockQuantiteTotale(m.getStockQuantiteTotale())
                .stockSeuilAlerte(m.getStockSeuilAlerte())
                .niveauAlerte(niveauAlerte)
                .emplacement(m.getEmplacement())
                .fabricant(m.getFabricant())
                .estRemboursableSs(m.getEstRemboursableSs())
                .tauxRemboursementSs(m.getTauxRemboursementSs())
                .estDisponible(m.getEstDisponible())
                .creeLe(m.getCreeLe())
                .build();
    }
}