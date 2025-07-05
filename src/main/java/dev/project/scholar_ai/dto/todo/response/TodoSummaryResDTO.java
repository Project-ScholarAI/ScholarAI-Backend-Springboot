package dev.project.scholar_ai.dto.todo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoSummaryResDTO {
    private int total;

    @JsonProperty("by_status")
    private StatusCount byStatus;

    @JsonProperty("by_priority")
    private PriorityCount byPriority;

    private int overdue;

    @JsonProperty("due_today")
    private int dueToday;

    @JsonProperty("due_this_week")
    private int dueThisWeek;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private int pending;

        @JsonProperty("in_progress")
        private int inProgress;

        private int completed;
        private int cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityCount {
        private int urgent;
        private int high;
        private int medium;
        private int low;
    }
}
