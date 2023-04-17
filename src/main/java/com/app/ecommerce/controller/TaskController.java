package com.app.ecommerce.controller;

import com.app.ecommerce.enums.Status;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.TaskRequestBody;
import com.app.ecommerce.models.response.error.BadRequestResponse;
import com.app.ecommerce.models.response.error.InternalServerResponse;
import com.app.ecommerce.models.response.success.*;
import com.app.ecommerce.service.framework.ITaskService;
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

import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    private ITaskService taskService;


    @Operation(summary = "Add New Task")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Task Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AddTaskResponseBody.class)
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
    @PostMapping("/task")
    public ResponseEntity<?> addTask(@RequestBody @Valid TaskRequestBody taskRequestBody) {
        return new ResponseEntity<>(
                Map.of("id" , taskService.addTask(taskRequestBody)) , HttpStatus.OK
        );
    }


    @Operation(summary = "Delete Task By Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "task deleted successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = DeleteTaskResponseBody.class)
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
    @DeleteMapping("/task/{id}")
    public ResponseEntity<?> deleteTaskById(@PathVariable(name = "id") Long taskId) throws IdNotFoundException {
        return new ResponseEntity<>(
                taskService.deleteTaskWithId(taskId),
                HttpStatus.OK
        );
    }


    @Operation(summary = "Get All Tasks By Status")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "tasks retrieved successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = GetAllTasksWithStatusResponseBody.class)
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
    @GetMapping("/task")
    public ResponseEntity<?> getAllTasksWithStatus(@RequestParam(value = "status") Status taskStatus) {
        return new ResponseEntity<>(
                    taskService.getAllTaskWithStatus(taskStatus),
                    HttpStatus.OK
                );
    }


    @Operation(summary = "Update Task By Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "task updated successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UpdateTaskResponseBody.class)
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
    @PutMapping("/task/{id}")
    public ResponseEntity<?> updateTask(@PathVariable(name = "id") Long taskId , @RequestBody @Valid TaskRequestBody taskRequestBody) throws IdNotFoundException {
        return new ResponseEntity<>(
                taskService.updateTaskWithId(taskId , taskRequestBody),
                HttpStatus.OK
        );
    }






    @Operation(summary = "Assign Task with id to User with Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "task assigned successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = AssignTaskToUserResponseBody.class)
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
    @PutMapping("/task/{taskId}/user/{userId}")
    public ResponseEntity<?> addTaskToUser(@PathVariable("taskId") Long taskId , @PathVariable("userId") Long userId) throws IdNotFoundException {
        return new ResponseEntity<>(
                taskService.addTaskToUser(userId,taskId),
                HttpStatus.OK
        );
    }
}
