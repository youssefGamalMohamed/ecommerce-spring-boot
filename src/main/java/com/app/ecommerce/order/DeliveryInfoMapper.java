package com.app.ecommerce.order;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryInfoMapper {

    DeliveryInfoResponse mapToResponse(DeliveryInfo deliveryInfo);
}
