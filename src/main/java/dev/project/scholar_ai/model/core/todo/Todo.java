package dev.project.scholar_ai.model.core.todo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.project.scholar_ai.model.core.todo.enums.TodoCategory;
import dev.project.scholar_ai.model.core.todo.enums.TodoPriority;
import dev.project.scholar_ai.model.core.todo.enums.TodoStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "todos", indexes = @Index(columnList = "userId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoCategory category;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private Integer estimatedTime; // in minutes

    private Integer actualTime; // in minutes

    private String relatedProjectId;

    private String relatedPaperId;

    @ElementCollection
    @CollectionTable(name = "todo_tags", joinColumns = @JoinColumn(name = "todo_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<TodoSubtask> subtasks = new ArrayList<>();

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<TodoReminder> reminders = new ArrayList<>();

    public void addSubtask(TodoSubtask subtask) {
        subtasks.add(subtask);
        subtask.setTodo(this);
    }

    public void removeSubtask(TodoSubtask subtask) {
        subtasks.remove(subtask);
        subtask.setTodo(null);
    }

    public void addReminder(TodoReminder reminder) {
        reminders.add(reminder);
        reminder.setTodo(this);
    }

    public void removeReminder(TodoReminder reminder) {
        reminders.remove(reminder);
        reminder.setTodo(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return getId() != null && Objects.equals(getId(), todo.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
