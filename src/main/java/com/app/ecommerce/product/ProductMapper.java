package com.app.ecommerce.product;

import com.app.ecommerce.category.CategoryMapper;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    @org.mapstruct.Mapping(target = "createdBy", ignore = true)
    @org.mapstruct.Mapping(target = "updatedBy", ignore = true)
    Product mapToEntity(ProductDto productDto);

    ProductDto mapToDto(Product product);

    List<ProductDto> mapToDtos(List<Product> products);

    Set<ProductDto> mapToDtos(Set<Product> products);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "categories", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    @org.mapstruct.Mapping(target = "createdBy", ignore = true)
    @org.mapstruct.Mapping(target = "updatedBy", ignore = true)
    Product mapToEntity(Product product);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "categories", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    @org.mapstruct.Mapping(target = "createdBy", ignore = true)
    @org.mapstruct.Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromEntity(Product source, @org.mapstruct.MappingTarget Product target);
}
