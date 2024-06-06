package com.example.authorizationserver.mappers;

import com.example.authorizationserver.entities.UserEntity;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

public class OidcUserInfoMapper {

    public static OidcUserInfo toModel(UserEntity userEntity) {
        return OidcUserInfo.builder()
//                .subject(userEntity.getUsername())
//                .name(userEntity.getName().getName())
//                .givenName(userEntity.getName().getGivenName())
//                .familyName(userEntity.getName().getFamilyName())
//                .middleName(userEntity.getName().getMiddleName())
//                .nickname(userEntity.getName().getNickname())
//                .preferredUsername(userEntity.getName().getPreferredUsername())
//                .profile(userEntity.getProfileLink())
//                .picture(userEntity.getPictureLink())
//                .website(userEntity.getWebsiteLink())
//                .email(userEntity.getEmail().getEmail())
//                .emailVerified(Boolean.valueOf(userEntity.getEmail().getEmailVerified()))
//                .gender(userEntity.getGender())
//                .birthdate(userEntity.getBirthdate())
//                .zoneinfo(userEntity.getAddress().getZoneInfo())
//                .locale(userEntity.getAddress().getLocale())
//                .phoneNumber(userEntity.getPhone().getPhoneNumber())
//                .phoneNumberVerified(Boolean.valueOf(userEntity.getPhone().getPhoneNumberVerified()))
                .build();
    }
}
