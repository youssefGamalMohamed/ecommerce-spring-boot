package com.app.ecommerce.controller.framework;

import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.models.request.ForgetPasswordRequestBody;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.request.ResetPasswordRequestBody;
import com.app.ecommerce.models.response.endpoints.LogoutResponseBody;
import com.app.ecommerce.models.response.endpoints.RefreshTokenResponseBody;
import com.app.ecommerce.models.response.http.BadRequestResponse;
import com.app.ecommerce.models.response.endpoints.LoginResponseBody;
import com.app.ecommerce.models.response.endpoints.RegisterResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.io.IOException;


@Tag(name = "Authentication", description = "contains Login and Register endpoints for authentication")
public interface IAuthenticationController {

    @Operation(summary = "Register New User / Create New User Account")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "User Created Successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = RegisterResponseBody.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Validation Error",
                            content = {
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BadRequestResponse.class))
                            }
                    )
            }
    )
    ResponseEntity<?> register(@Valid @RequestBody RegisterRequestBody registerRequestBody) throws JsonProcessingException;


    @Operation(summary = "Login with UserName and Password")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Successfully Login",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoginResponseBody.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Failed to Login"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Validation Error",
                            content = {
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BadRequestResponse.class))
                            }
                    )
            }
    )
    ResponseEntity<?> login(@Valid @RequestBody LoginRequestBody loginRequestBody);


    @Operation(summary = "Refresh Old Expired Token")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Return new Access Token and New Refresh Token",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = RefreshTokenResponseBody.class)
                                    )
                            }
                    )
            }
    )
    ResponseEntity<?> refreshToken( HttpServletRequest request, HttpServletResponse response ) throws IOException;


    @Operation(summary = "Logout User From System")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Logout Done",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LogoutResponseBody.class)
                                    )
                            }
                    ) ,
                    @ApiResponse(
                            responseCode = "401", description = "No Token Sent , Failed to Logout"
                    )
            }
    )
    ResponseEntity<?> logout( HttpServletRequest request, HttpServletResponse response );


    ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordRequestBody forgetPasswordRequestBody) throws JsonParsingException;


    ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestBody resetPasswordRequestBody);

}