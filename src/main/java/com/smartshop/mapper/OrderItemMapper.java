package com.smartshop.mapper;

import com.smartshop.dto.OrderItemResponseDTO;
import com.smartshop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    @Mapping(target = "productId", source = "produit.id")
    @Mapping(target = "productNom", source = "produit.nom")
    @Mapping(target = "prixUnitaire", source = "prixUnitaire")
    @Mapping(target = "quantite", source = "quantite")
    @Mapping(target = "totalLigne", source = "totalLigne")
    OrderItemResponseDTO toDTO(OrderItem orderItem);
}