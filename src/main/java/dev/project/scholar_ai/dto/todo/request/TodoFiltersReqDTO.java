package dev.project.scholar_ai.dto.todo.request;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoFiltersReqDTO {
    private String userId;
    private List<String> status;
    private List<String> priority;
    private List<String> category;
    private String dueDateStart;
    private String dueDateEnd;
    private String search;
    private Set<String> tags;
    private String projectId;
    private String sortField;
    private String sortDirection;
}
