package com.smartshop.mapper;

import com.smartshop.dto.PaiementResponseDTO;
import com.smartshop.entity.Paiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaiementMapper {
    PaiementMapper INSTANCE = Mappers.getMapper(PaiementMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "numeroPaiement", source = "numeroPaiement")
    @Mapping(target = "montant", source = "montant")
    @Mapping(target = "typePaiement", source = "typePaiement")
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "datePaiement", source = "datePaiement")
    @Mapping(target = "dateEncaissement", source = "dateEncaissement")
    @Mapping(target = "numeroRecu", source = "numeroRecu")
    @Mapping(target = "numeroCheque", source = "numeroCheque")
    @Mapping(target = "banqueCheque", source = "banqueCheque")
    @Mapping(target = "echeanceCheque", source = "echeanceCheque")
    @Mapping(target = "referenceVirement", source = "referenceVirement")
    @Mapping(target = "banqueVirement", source = "banqueVirement")
    @Mapping(target = "commandeId", source = "commande.id")
    @Mapping(target = "reference", expression = "java(getReference(paiement))")
    PaiementResponseDTO toDTO(Paiement paiement);

    default String getReference(Paiement paiement) {
        if (paiement == null) return null;

        switch (paiement.getTypePaiement()) {
            case ESPECES:
                return paiement.getNumeroRecu();
            case CHEQUE:
                return paiement.getNumeroCheque();
            case VIREMENT:
                return paiement.getReferenceVirement();
            default:
                return null;
        }
    }
}