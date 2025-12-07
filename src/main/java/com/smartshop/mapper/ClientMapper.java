package com.smartshop.mapper;

import com.smartshop.dto.ClientDTO;
import com.smartshop.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "niveauFidelite", source = "niveauFidelite")
    ClientDTO toDTO(Client client);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    Client toEntity(ClientDTO clientDTO);
}