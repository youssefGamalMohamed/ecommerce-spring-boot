package com.app.ecommerce.order;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryInfoMapper {

    DeliveryInfo mapToEntity(DeliveryInfoDto deliveryInfoDto);

    DeliveryInfoDto mapToDto(DeliveryInfo deliveryInfo);

    List<DeliveryInfoDto> mapToDtos(List<DeliveryInfo> deliveryInfos);

    Set<DeliveryInfoDto> mapToDtos(Set<DeliveryInfo> deliveryInfosDtos);
}
