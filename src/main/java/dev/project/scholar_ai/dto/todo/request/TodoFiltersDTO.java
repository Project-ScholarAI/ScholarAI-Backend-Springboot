package dev.project.scholar_ai.dto.todo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoFiltersDTO {
    private String status;
    private String priority;
    private String category;
    private String search;
}
