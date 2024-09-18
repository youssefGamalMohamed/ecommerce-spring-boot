package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.UserDto;
import com.app.ecommerce.entity.User;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User mapToEntity(UserDto userDto);  
    UserDto mapToDto(User user);
    List<UserDto> mapToDtos(List<User> users);
    Set<UserDto> mapToDtos(Set<User> usersDtos);
}
