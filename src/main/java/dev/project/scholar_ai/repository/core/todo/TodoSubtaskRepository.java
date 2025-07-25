package dev.project.scholar_ai.repository.core.todo;

import dev.project.scholar_ai.model.core.todo.TodoSubtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoSubtaskRepository extends JpaRepository<TodoSubtask, String> {}
