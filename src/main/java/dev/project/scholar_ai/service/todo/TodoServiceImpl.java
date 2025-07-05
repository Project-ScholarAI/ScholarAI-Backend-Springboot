package dev.project.scholar_ai.service.todo;

import dev.project.scholar_ai.dto.todo.request.*;
import dev.project.scholar_ai.dto.todo.response.TodoResponseDTO;
import dev.project.scholar_ai.dto.todo.response.TodoSummaryResDTO;
import dev.project.scholar_ai.mapping.todo.TodoMapper;
import dev.project.scholar_ai.model.core.todo.Todo;
import dev.project.scholar_ai.model.core.todo.TodoReminder;
import dev.project.scholar_ai.model.core.todo.TodoSubtask;
import dev.project.scholar_ai.repository.core.todo.TodoRepository;
import dev.project.scholar_ai.repository.core.todo.TodoSubtaskRepository;
import dev.project.scholar_ai.security.SecurityUtils;
import dev.project.scholar_ai.service.todo.TodoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoSubtaskRepository subtaskRepository;
    private final TodoMapper todoMapper;

    @Override
    public TodoResponseDTO createTodo(TodoCreateReqDTO request) throws Exception {
        Todo todo = todoMapper.todoCreateRequestToTodo(request);
        if (request.getSubtasks() != null) {
            request.getSubtasks().forEach(subtaskReq -> {
                TodoSubtask subtask = TodoSubtask.builder()
                        .title(subtaskReq.getTitle())
                        .completed(false)
                        .build();
                todo.addSubtask(subtask);
            });
        }
        if (request.getReminders() != null) {
            request.getReminders().forEach(reminderReq -> {
                TodoReminder reminder = TodoReminder.builder()
                        .remindAt(todoMapper.stringToLocalDateTime(reminderReq.getRemindAt()))
                        .message(reminderReq.getMessage())
                        .sent(false)
                        .build();
                todo.addReminder(reminder);
            });
        }
        return todoMapper.todoToTodoResponse(todoRepository.save(todo));
    }

   
}
