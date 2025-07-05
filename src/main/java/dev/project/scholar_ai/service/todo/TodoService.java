package dev.project.scholar_ai.service.todo;

import dev.project.scholar_ai.dto.todo.request.*;
import dev.project.scholar_ai.dto.todo.response.TodoResponseDTO;
import dev.project.scholar_ai.dto.todo.response.TodoSummaryResDTO;

import java.util.List;

public interface TodoService {
    TodoResponseDTO createTodo(TodoCreateReqDTO request) throws Exception;
    TodoResponseDTO updateStatus(String id, TodoStatusUpdateReqDTO statusUpdate) throws Exception;
    TodoResponseDTO updateTodo(String id, TodoUpdateReqDTO updateReqDTO) throws Exception;
    void deleteTodo(String id) throws Exception;
    TodoResponseDTO getTodoById(String id) throws Exception;
    List<TodoResponseDTO> filterTodos(TodoFiltersReqDTO filters) throws Exception;
    TodoSummaryResDTO getSummary() throws Exception;
    TodoResponseDTO addSubtask(String todoId, String subtaskTitle) throws Exception;
    TodoResponseDTO toggleSubtaskCompletion(String subtaskId) throws Exception;
}
