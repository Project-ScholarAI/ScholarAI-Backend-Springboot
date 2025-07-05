package dev.project.scholar_ai.service.todo;

import dev.project.scholar_ai.dto.todo.request.TodoCreateReqDTO;
import dev.project.scholar_ai.dto.todo.request.TodoFiltersReqDTO;
import dev.project.scholar_ai.dto.todo.request.TodoUpdateReqDTO;
import dev.project.scholar_ai.dto.todo.response.TodoResponseDTO;
import dev.project.scholar_ai.dto.todo.response.TodoSummaryResDTO;
import dev.project.scholar_ai.model.core.todo.enums.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface TodoService {

    @Transactional(readOnly = true)
    Page<TodoResponseDTO> getAllTodos(TodoFiltersReqDTO filters, String userId);

    @Transactional(readOnly = true)
    TodoSummaryResDTO getTodoSummary();

    @Transactional(readOnly = true)
    TodoResponseDTO getTodoById(String id);

    @Transactional
    TodoResponseDTO createTodo(TodoCreateReqDTO createRequest);

    @Transactional
    TodoResponseDTO updateTodo(String id, TodoUpdateReqDTO updateRequest);

    @Transactional
    TodoResponseDTO updateTodoStatus(String id, TodoStatus status);

    @Transactional
    void deleteTodo(String id);

    @Transactional
    TodoResponseDTO addSubtask(String todoId, String subtaskTitle);

    @Transactional
    void toggleSubtask(String todoId, String subtaskId);
}
