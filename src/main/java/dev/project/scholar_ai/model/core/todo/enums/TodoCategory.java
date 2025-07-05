package dev.project.scholar_ai.model.core.todo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum TodoCategory {
    RESEARCH,
    WRITING,
    REVIEW,
    ANALYSIS,
    MEETING,
    DEADLINE,
    PERSONAL,
    COLLABORATION;

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static TodoCategory fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(TodoCategory.values())
                .filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum type " + value + ", Allowed values are "
                        + Stream.of(values()).map(TodoCategory::name).toString()));
    }
}
