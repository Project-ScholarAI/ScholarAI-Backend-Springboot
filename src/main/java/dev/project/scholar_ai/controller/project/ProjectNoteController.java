package dev.project.scholar_ai.controller.project;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.project.CreateNoteDto;
import dev.project.scholar_ai.dto.project.NoteDto;
import dev.project.scholar_ai.dto.project.UpdateNoteDto;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.service.project.ProjectNoteService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/projects/{projectId}/notes")
@RequiredArgsConstructor
public class ProjectNoteController {

    private final ProjectNoteService projectNoteService;
    private final AuthUserRepository authUserRepository;

    /**
     * Helper method to get user ID from Principal (email)
     */
    private UUID getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Authentication required");
        }
        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Invalid authentication token");
        }
        AuthUser user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }

    /**
     * Get all notes for a project
     */
    @GetMapping
    public ResponseEntity<APIResponse<List<NoteDto>>> getNotes(@PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get notes for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<NoteDto> notes = projectNoteService.getNotesByProjectId(projectId, userId);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Notes retrieved successfully", notes));
        } catch (RuntimeException e) {
            log.error("Error retrieving notes for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving notes for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve notes", null));
        }
    }

    /**
     * Create a new note
     */
    @PostMapping
    public ResponseEntity<APIResponse<NoteDto>> createNote(
            @PathVariable UUID projectId, @Valid @RequestBody CreateNoteDto createNoteDto, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Create note for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            NoteDto createdNote = projectNoteService.createNote(projectId, createNoteDto, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(HttpStatus.CREATED.value(), "Note created successfully", createdNote));
        } catch (RuntimeException e) {
            log.error("Error creating note for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error creating note for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create note", null));
        }
    }

    /**
     * Update an existing note
     */
    @PutMapping("/{noteId}")
    public ResponseEntity<APIResponse<NoteDto>> updateNote(
            @PathVariable UUID projectId,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteDto updateNoteDto,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Update note {} for project {} endpoint hit by user: {}", noteId, projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            NoteDto updatedNote = projectNoteService.updateNote(projectId, noteId, updateNoteDto, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Note updated successfully", updatedNote));
        } catch (RuntimeException e) {
            log.error("Error updating note {} for project {}: {}", noteId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error updating note {} for project {}: {}", noteId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update note", null));
        }
    }

    /**
     * Delete a note
     */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<APIResponse<String>> deleteNote(
            @PathVariable UUID projectId, @PathVariable UUID noteId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Delete note {} for project {} endpoint hit by user: {}", noteId, projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            projectNoteService.deleteNote(projectId, noteId, userId);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Note deleted successfully", null));
        } catch (RuntimeException e) {
            log.error("Error deleting note {} for project {}: {}", noteId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error deleting note {} for project {}: {}", noteId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete note", null));
        }
    }

    /**
     * Toggle favorite status of a note
     */
    @PutMapping("/{noteId}/favorite")
    public ResponseEntity<APIResponse<NoteDto>> toggleNoteFavorite(
            @PathVariable UUID projectId, @PathVariable UUID noteId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Toggle favorite for note {} in project {} endpoint hit by user: {}",
                    noteId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            NoteDto updatedNote = projectNoteService.toggleNoteFavorite(projectId, noteId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Note favorite status updated", updatedNote));
        } catch (RuntimeException e) {
            log.error("Error toggling favorite for note {} in project {}: {}", noteId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error toggling favorite for note {} in project {}: {}",
                    noteId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update note favorite status", null));
        }
    }

    /**
     * Get favorite notes for a project
     */
    @GetMapping("/favorites")
    public ResponseEntity<APIResponse<List<NoteDto>>> getFavoriteNotes(
            @PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get favorite notes for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<NoteDto> notes = projectNoteService.getFavoriteNotesByProjectId(projectId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Favorite notes retrieved successfully", notes));
        } catch (RuntimeException e) {
            log.error("Error retrieving favorite notes for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving favorite notes for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve favorite notes", null));
        }
    }

    /**
     * Search notes by tag
     */
    @GetMapping("/search/tag")
    public ResponseEntity<APIResponse<List<NoteDto>>> searchNotesByTag(
            @PathVariable UUID projectId, @RequestParam String tag, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Search notes by tag {} for project {} endpoint hit by user: {}",
                    tag,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<NoteDto> notes = projectNoteService.searchNotesByTag(projectId, tag, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Notes search completed successfully", notes));
        } catch (RuntimeException e) {
            log.error("Error searching notes by tag {} for project {}: {}", tag, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error searching notes by tag {} for project {}: {}", tag, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to search notes", null));
        }
    }

    /**
     * Search notes by content
     */
    @GetMapping("/search/content")
    public ResponseEntity<APIResponse<List<NoteDto>>> searchNotesByContent(
            @PathVariable UUID projectId, @RequestParam String q, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Search notes by content {} for project {} endpoint hit by user: {}",
                    q,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<NoteDto> notes = projectNoteService.searchNotesByContent(projectId, q, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Notes search completed successfully", notes));
        } catch (RuntimeException e) {
            log.error("Error searching notes by content {} for project {}: {}", q, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error searching notes by content {} for project {}: {}", q, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to search notes", null));
        }
    }
}
