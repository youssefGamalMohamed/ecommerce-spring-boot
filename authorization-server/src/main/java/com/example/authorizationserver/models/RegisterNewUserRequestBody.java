package com.example.authorizationserver.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterNewUserRequestBody {


    @NotBlank(message = "Username Mustn't be Empty") @NotEmpty(message = "Username Mustn't be Empty")
    private String username;

    @NotBlank(message = "Password Mustn't be Empty") @NotEmpty(message = "Password Mustn't be Empty")
    private String password;

    @NotBlank(message = "Name Mustn't be Empty") @NotEmpty(message = "Name Mustn't be Empty")
    private String name;

    @NotBlank(message = "GivenName Mustn't be Empty") @NotEmpty(message = "GivenName Mustn't be Empty")
    private String given_name;

    @NotBlank(message = "FamilyName Mustn't be Empty") @NotEmpty(message = "FamilyName Mustn't be Empty")
    private String family_name;

    @NotBlank(message = "MiddleName Mustn't be Empty") @NotEmpty(message = "MiddleName Mustn't be Empty")
    private String middle_name;

    @NotBlank(message = "NickName Mustn't be Empty") @NotEmpty(message = "NickName Mustn't be Empty")
    private String nick_name;

    @NotBlank(message = "PreferredUsername Mustn't be Empty") @NotEmpty(message = "PreferredUsername Mustn't be Empty")
    private String preferred_username;

    @NotBlank(message = "Email Mustn't be Empty") @NotEmpty(message = "Email Mustn't be Empty")
    private String email;

    @NotNull(message = "Email Verified Mustn't be Empty Or NULL")
    private boolean email_verified;

    @NotBlank(message = "Gender Mustn't be Empty") @NotEmpty(message = "Gender Mustn't be Empty")
    private String gender;

    @NotBlank(message = "Birthdate Mustn't be Empty") @NotEmpty(message = "Birthdate Mustn't be Empty")
    private String birthdate;

    @NotBlank(message = "ZoneInfo Mustn't be Empty") @NotEmpty(message = "ZoneInfo Mustn't be Empty")
    private String zoneinfo;

    @NotBlank(message = "Locale Mustn't be Empty") @NotEmpty(message = "Locale Mustn't be Empty")
    private String locale;

    @NotBlank(message = "PhoneNumber Mustn't be Empty") @NotEmpty(message = "PhoneNumber Mustn't be Empty")
    private String phone_number;

    @NotNull(message = "Phone Number Verified Mustn't be Empty Or NULL")
    private boolean phone_number_verified;

    @NotBlank(message = "Profile URL Mustn't be Empty") @NotEmpty(message = "Profile URL Mustn't be Empty")
    private String profile;

    @NotBlank(message = "Picture URL Mustn't be Empty") @NotEmpty(message = "Picture URL Mustn't be Empty")
    private String picture;

    @NotBlank(message = "Website URL Mustn't be Empty") @NotEmpty(message = "Website URL Mustn't be Empty")
    private String website;

}
