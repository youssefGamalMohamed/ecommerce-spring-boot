package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.DeliveryInfoDto;
import com.app.ecommerce.entity.DeliveryInfo;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryInfoMapper {

    DeliveryInfo mapToEntity(DeliveryInfoDto deliveryInfoDto);  

    DeliveryInfoDto mapToDto(DeliveryInfo deliveryInfo);
    List<DeliveryInfoDto> mapToDtos(List<DeliveryInfo> deliveryInfos);
    Set<DeliveryInfoDto> mapToDtos(Set<DeliveryInfo> deliveryInfosDtos);
}
