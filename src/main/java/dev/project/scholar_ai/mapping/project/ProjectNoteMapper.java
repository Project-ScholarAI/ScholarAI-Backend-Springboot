package dev.project.scholar_ai.mapping.project;

import dev.project.scholar_ai.dto.project.CreateNoteDto;
import dev.project.scholar_ai.dto.project.NoteDto;
import dev.project.scholar_ai.dto.project.UpdateNoteDto;
import dev.project.scholar_ai.model.core.project.ProjectNote;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProjectNoteMapper {

    ProjectNoteMapper INSTANCE = Mappers.getMapper(ProjectNoteMapper.class);

    NoteDto toDto(ProjectNote entity);

    @Mapping(target = "id", ignore = true) // ID will be generated
    @Mapping(target = "createdAt", ignore = true) // Auto-generated
    @Mapping(target = "updatedAt", ignore = true) // Auto-generated
    @Mapping(target = "isFavorite", constant = "false") // Default to false
    ProjectNote fromCreateDto(CreateNoteDto dto, UUID projectId);

    @Mapping(target = "id", ignore = true) // Don't update ID
    @Mapping(target = "projectId", ignore = true) // Don't update project ID
    @Mapping(target = "createdAt", ignore = true) // Don't update creation time
    @Mapping(target = "updatedAt", ignore = true) // Auto-generated
    @Mapping(target = "isFavorite", ignore = true) // Don't update favorite status
    ProjectNote fromUpdateDto(UpdateNoteDto dto);
}
