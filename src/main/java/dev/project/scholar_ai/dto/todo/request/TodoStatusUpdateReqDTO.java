package dev.project.scholar_ai.dto.todo.request;

import dev.project.scholar_ai.model.core.todo.enums.TodoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoStatusUpdateReqDTO {
    @NotNull(message = "Status is required") private TodoStatus status;
}
