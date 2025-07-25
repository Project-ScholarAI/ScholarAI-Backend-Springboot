package dev.project.scholar_ai.service.project;

import dev.project.scholar_ai.dto.project.CreateNoteDto;
import dev.project.scholar_ai.dto.project.NoteDto;
import dev.project.scholar_ai.dto.project.UpdateNoteDto;
import dev.project.scholar_ai.mapping.project.ProjectNoteMapper;
import dev.project.scholar_ai.model.core.project.Project;
import dev.project.scholar_ai.model.core.project.ProjectNote;
import dev.project.scholar_ai.repository.core.project.ProjectCollaboratorRepository;
import dev.project.scholar_ai.repository.core.project.ProjectNoteRepository;
import dev.project.scholar_ai.repository.core.project.ProjectRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager")
public class ProjectNoteService {

    private final ProjectNoteRepository projectNoteRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCollaboratorRepository projectCollaboratorRepository;
    private final ProjectNoteMapper projectNoteMapper;

    /**
     * Get all notes for a project
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<NoteDto> getNotesByProjectId(UUID projectId, UUID userId) {
        log.info("Fetching notes for project: {} for user: {}", projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        List<ProjectNote> notes = projectNoteRepository.findByProjectIdOrderByUpdatedAtDesc(projectId);
        return notes.stream().map(projectNoteMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get a specific note by ID
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public NoteDto getNoteById(UUID projectId, UUID noteId, UUID userId) {
        log.info("Fetching note: {} for project: {} for user: {}", noteId, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        ProjectNote note = projectNoteRepository
                .findByIdAndProjectId(noteId, projectId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        return projectNoteMapper.toDto(note);
    }

    /**
     * Create a new note
     */
    public NoteDto createNote(UUID projectId, CreateNoteDto createNoteDto, UUID userId) {
        log.info("Creating new note for project: {} for user: {}", projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        ProjectNote note = projectNoteMapper.fromCreateDto(createNoteDto, projectId);
        ProjectNote savedNote = projectNoteRepository.save(note);

        log.info("Note created successfully with ID: {}", savedNote.getId());
        return projectNoteMapper.toDto(savedNote);
    }

    /**
     * Update an existing note
     */
    public NoteDto updateNote(UUID projectId, UUID noteId, UpdateNoteDto updateNoteDto, UUID userId) {
        log.info("Updating note: {} for project: {} for user: {}", noteId, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        ProjectNote existingNote = projectNoteRepository
                .findByIdAndProjectId(noteId, projectId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Update fields if provided
        if (updateNoteDto.title() != null) {
            existingNote.setTitle(updateNoteDto.title());
        }
        if (updateNoteDto.content() != null) {
            existingNote.setContent(updateNoteDto.content());
        }
        if (updateNoteDto.tags() != null) {
            existingNote.setTags(updateNoteDto.tags());
        }

        ProjectNote savedNote = projectNoteRepository.save(existingNote);

        log.info("Note updated successfully with ID: {}", savedNote.getId());
        return projectNoteMapper.toDto(savedNote);
    }

    /**
     * Delete a note
     */
    public void deleteNote(UUID projectId, UUID noteId, UUID userId) {
        log.info("Deleting note: {} for project: {} for user: {}", noteId, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        ProjectNote note = projectNoteRepository
                .findByIdAndProjectId(noteId, projectId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        projectNoteRepository.delete(note);

        log.info("Note deleted successfully with ID: {}", noteId);
    }

    /**
     * Toggle favorite status of a note
     */
    public NoteDto toggleNoteFavorite(UUID projectId, UUID noteId, UUID userId) {
        log.info("Toggling favorite status for note: {} in project: {} for user: {}", noteId, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        ProjectNote note = projectNoteRepository
                .findByIdAndProjectId(noteId, projectId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setIsFavorite(!note.getIsFavorite());
        ProjectNote savedNote = projectNoteRepository.save(note);

        log.info("Note favorite status updated successfully with ID: {}", savedNote.getId());
        return projectNoteMapper.toDto(savedNote);
    }

    /**
     * Get favorite notes for a project
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<NoteDto> getFavoriteNotesByProjectId(UUID projectId, UUID userId) {
        log.info("Fetching favorite notes for project: {} for user: {}", projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        List<ProjectNote> notes =
                projectNoteRepository.findByProjectIdAndIsFavoriteOrderByUpdatedAtDesc(projectId, true);
        return notes.stream().map(projectNoteMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Search notes by tag
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<NoteDto> searchNotesByTag(UUID projectId, String tag, UUID userId) {
        log.info("Searching notes by tag: {} for project: {} for user: {}", tag, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        List<ProjectNote> notes = projectNoteRepository.findByProjectIdAndTag(projectId, tag);
        return notes.stream().map(projectNoteMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Search notes by content
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<NoteDto> searchNotesByContent(UUID projectId, String searchTerm, UUID userId) {
        log.info("Searching notes by content: {} for project: {} for user: {}", searchTerm, projectId, userId);

        // Validate user has access to the project
        validateProjectAccess(projectId, userId);

        List<ProjectNote> notes = projectNoteRepository.findByProjectIdAndSearchTerm(projectId, searchTerm);
        return notes.stream().map(projectNoteMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Validate that user has access to the project (either as owner or
     * collaborator)
     */
    private void validateProjectAccess(UUID projectId, UUID userId) {
        // Check if user is the project owner
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElse(null);

        // If not owner, check if user is a collaborator
        if (project == null) {
            boolean isCollaborator = projectCollaboratorRepository
                    .findByProjectIdAndCollaboratorId(projectId, userId)
                    .isPresent();

            if (!isCollaborator) {
                throw new RuntimeException("Project not found or access denied");
            }
        }
    }
}
