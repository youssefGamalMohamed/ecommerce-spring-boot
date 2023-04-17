package com.app.ecommerce.service.framework;

import com.app.ecommerce.enums.Status;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.TaskRequestBody;
import com.app.ecommerce.models.response.success.*;


public interface ITaskService {
    AddTaskResponseBody addTask(TaskRequestBody task);

    DeleteTaskResponseBody deleteTaskWithId(Long taskId) throws IdNotFoundException;

    UpdateTaskResponseBody updateTaskWithId(Long taskId, TaskRequestBody taskRequestBody) throws IdNotFoundException;


    GetAllTasksWithStatusResponseBody getAllTaskWithStatus(Status taskStatus);


    AssignTaskToUserResponseBody addTaskToUser(Long userId, Long taskId) throws IdNotFoundException;
}
