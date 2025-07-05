package dev.project.scholar_ai.model.core.todo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum TodoStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    pending;

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static TodoStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(TodoStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(value.replace("_", "")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum type " + value + ", Allowed values are "
                        + Stream.of(values()).map(TodoStatus::name).toString()));
    }
}
