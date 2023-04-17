package com.app.ecommerce.controller;

import com.app.ecommerce.entity.User;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.UserRequestBody;
import com.app.ecommerce.models.response.success.AddUserResponseBody;
import com.app.ecommerce.models.response.error.BadRequestResponse;
import com.app.ecommerce.models.response.success.DeleteUserResponseBody;
import com.app.ecommerce.models.response.error.InternalServerResponse;
import com.app.ecommerce.service.framework.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {


    @Autowired
    private IUserService userService;

    @Operation(summary = "Add New User")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AddUserResponseBody.class)
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
    @PostMapping("/user")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRequestBody userRequestBody) {
        return new ResponseEntity<>(
                userService.addNewUser(userRequestBody),
                HttpStatus.OK
        );
    }



    @Operation(summary = "Get User By Id")
    @ApiResponses(
            value = {
            @ApiResponse(
                    responseCode = "200", description = "information about the user and his/her tasks",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BadRequestResponse.class))
                    }
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal Server Error",
                    content = {
                            @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InternalServerResponse.class))
                    }
            )
        }
    )
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable(name = "id") Long id) throws IdNotFoundException {
        return new ResponseEntity<>(
                userService.getUserById(id),
                HttpStatus.OK
        );
    }


    @Operation(summary = "Delete  User By Id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200", description = "user deleted successfully with his/her tasks",
                        content = {
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = DeleteUserResponseBody.class)
                                )
                        }
                ),
                @ApiResponse(
                        responseCode = "400", description = "Validation Error",
                        content = {
                                @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = BadRequestResponse.class))
                        }
                ),
                @ApiResponse(
                        responseCode = "500", description = "Internal Server Error",
                        content = {
                                @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = InternalServerResponse.class))
                        }
                )
            }
    )
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) throws IdNotFoundException {
        return new ResponseEntity<>(
                userService.deleteUserById(id),
                HttpStatus.OK
        );
    }


}
