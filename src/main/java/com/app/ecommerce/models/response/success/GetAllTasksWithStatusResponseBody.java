package com.app.ecommerce.models.response.success;


import com.app.ecommerce.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllTasksWithStatusResponseBody {
    private List<Task> taskList;
}
