package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.DeliveryInfoDto;
import com.app.ecommerce.entity.DeliveryInfo;

@Mapper
public interface DeliveryInfoMapper {

    DeliveryInfoMapper INSTANCE = Mappers.getMapper(DeliveryInfoMapper.class);

    DeliveryInfo mapToEntity(DeliveryInfoDto deliveryInfoDto);  
    DeliveryInfoDto mapToDto(DeliveryInfo deliveryInfo);
    List<DeliveryInfoDto> mapToDtos(List<DeliveryInfo> deliveryInfos);
    Set<DeliveryInfoDto> mapToDtos(Set<DeliveryInfo> deliveryInfosDtos);
}
