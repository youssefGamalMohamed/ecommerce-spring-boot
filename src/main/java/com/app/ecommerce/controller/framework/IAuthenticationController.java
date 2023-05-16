package com.app.ecommerce.controller.framework;

import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.response.other.UnAuthorizedResponse;
import com.app.ecommerce.models.response.success.LoginResponseBody;
import com.app.ecommerce.models.response.success.RegisterResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

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
                    )
            }
    )
    ResponseEntity<?> register(@RequestBody RegisterRequestBody registerRequestBody);


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
                            responseCode = "401", description = "Failed to Login",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UnAuthorizedResponse.class)
                                    )
                            }
                    )
            }
    )
    ResponseEntity<?> authenticate(@RequestBody LoginRequestBody loginRequestBody);
}