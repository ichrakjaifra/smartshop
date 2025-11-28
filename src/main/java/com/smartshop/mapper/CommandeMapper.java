package com.smartshop.mapper;

import com.smartshop.dto.CommandeResponseDTO;
import com.smartshop.entity.Commande;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, PaiementMapper.class})
public interface CommandeMapper {
    CommandeMapper INSTANCE = Mappers.getMapper(CommandeMapper.class);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientNom", source = "client.nom")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "paiements", source = "paiements")
    CommandeResponseDTO toDTO(Commande commande);
}