package dev.project.scholar_ai.controller.project;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.project.AddCollaboratorRequest;
import dev.project.scholar_ai.dto.project.CollaboratorDto;
import dev.project.scholar_ai.dto.project.CreateProjectDto;
import dev.project.scholar_ai.dto.project.ProjectDto;
import dev.project.scholar_ai.dto.project.UpdateProjectDto;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.model.core.project.Project;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.service.project.ProjectService;
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
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
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
     * Create a new project
     */
    @PostMapping
    public ResponseEntity<APIResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectDto createProjectDto, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Create project endpoint hit by user: {}", principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ProjectDto createdProject = projectService.createProject(createProjectDto, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(
                            HttpStatus.CREATED.value(), "Project created successfully", createdProject));
        } catch (RuntimeException e) {
            log.error("Error creating project: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(
                            HttpStatus.BAD_REQUEST.value(), "Failed to create project: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error creating project: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create project", null));
        }
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<APIResponse<ProjectDto>> getProjectById(@PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ProjectDto project = projectService.getProjectById(projectId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Project retrieved successfully", project));
        } catch (RuntimeException e) {
            log.error("Error retrieving project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve project", null));
        }
    }

    /**
     * Get all projects for the authenticated user
     */
    @GetMapping
    public ResponseEntity<APIResponse<List<ProjectDto>>> getAllProjects(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get all projects endpoint hit by user: {}", principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<ProjectDto> projects = projectService.getProjectsByUserId(userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Projects retrieved successfully", projects));
        } catch (RuntimeException e) {
            log.error("Error retrieving projects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving projects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve projects", null));
        }
    }

    /**
     * Get projects by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<APIResponse<List<ProjectDto>>> getProjectsByStatus(
            @PathVariable String status, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get projects by status {} endpoint hit by user: {}", status, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            Project.Status projectStatus = Project.Status.valueOf(status.toUpperCase());
            List<ProjectDto> projects = projectService.getProjectsByUserIdAndStatus(userId, projectStatus);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Projects retrieved successfully", projects));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid status: " + status, null));
        } catch (RuntimeException e) {
            log.error("Error retrieving projects by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving projects by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve projects", null));
        }
    }

    /**
     * Get starred projects
     */
    @GetMapping("/starred")
    public ResponseEntity<APIResponse<List<ProjectDto>>> getStarredProjects(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get starred projects endpoint hit by user: {}", principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<ProjectDto> projects = projectService.getStarredProjects(userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Starred projects retrieved successfully", projects));
        } catch (RuntimeException e) {
            log.error("Error retrieving starred projects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving starred projects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve starred projects", null));
        }
    }

    /**
     * Update an existing project
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<APIResponse<ProjectDto>> updateProject(
            @PathVariable UUID projectId, @Valid @RequestBody UpdateProjectDto updateProjectDto, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Update project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ProjectDto updatedProject = projectService.updateProject(projectId, updateProjectDto, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Project updated successfully", updatedProject));
        } catch (RuntimeException e) {
            log.error("Error updating project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error updating project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update project", null));
        }
    }

    /**
     * Delete a project
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<APIResponse<String>> deleteProject(@PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Delete project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            projectService.deleteProject(projectId, userId);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Project deleted successfully", null));
        } catch (RuntimeException e) {
            log.error("Error deleting project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error deleting project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete project", null));
        }
    }

    /**
     * Toggle project starred status
     */
    @PostMapping("/{projectId}/toggle-star")
    public ResponseEntity<APIResponse<ProjectDto>> toggleProjectStar(
            @PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Toggle star for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ProjectDto updatedProject = projectService.toggleProjectStar(projectId, userId);

            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Project star status updated successfully", updatedProject));
        } catch (RuntimeException e) {
            log.error("Error toggling star for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error toggling star for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update project star status", null));
        }
    }

    /**
     * Get project statistics for the authenticated user
     */
    @GetMapping("/stats")
    public ResponseEntity<APIResponse<Object>> getProjectStats(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get project stats endpoint hit by user: {}", principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);

            long activeCount = projectService.getProjectCountByStatus(userId, Project.Status.ACTIVE);
            long pausedCount = projectService.getProjectCountByStatus(userId, Project.Status.PAUSED);
            long completedCount = projectService.getProjectCountByStatus(userId, Project.Status.COMPLETED);
            long archivedCount = projectService.getProjectCountByStatus(userId, Project.Status.ARCHIVED);

            Object stats = new Object() {
                public final long active = activeCount;
                public final long paused = pausedCount;
                public final long completed = completedCount;
                public final long archived = archivedCount;
                public final long total = activeCount + pausedCount + completedCount + archivedCount;
            };

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Project statistics retrieved successfully", stats));
        } catch (RuntimeException e) {
            log.error("Error retrieving project stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving project stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve project statistics", null));
        }
    }

    /**
     * Get collaborators for a project
     */
    @GetMapping("/{projectId}/collaborators")
    public ResponseEntity<APIResponse<List<CollaboratorDto>>> getProjectCollaborators(
            @PathVariable UUID projectId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get collaborators for project {} endpoint hit by user: {}", projectId, principal.getName());

            List<CollaboratorDto> collaborators = projectService.getCollaboratorsForProject(projectId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Collaborators retrieved successfully", collaborators));
        } catch (RuntimeException e) {
            log.error("Error retrieving collaborators for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving collaborators for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve collaborators", null));
        }
    }

    /**
     * Add a collaborator to a project
     */
    @PostMapping("/{projectId}/collaborators")
    public ResponseEntity<APIResponse<CollaboratorDto>> addCollaborator(
            @PathVariable UUID projectId, @Valid @RequestBody AddCollaboratorRequest request, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Add collaborator to project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            CollaboratorDto collaborator = projectService.addCollaborator(projectId, request, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(
                            HttpStatus.CREATED.value(), "Collaborator added successfully", collaborator));
        } catch (RuntimeException e) {
            log.error("Error adding collaborator to project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error adding collaborator to project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to add collaborator", null));
        }
    }

    /**
     * Remove a collaborator from a project
     */
    @DeleteMapping("/{projectId}/collaborators/{collaboratorId}")
    public ResponseEntity<APIResponse<String>> removeCollaborator(
            @PathVariable UUID projectId, @PathVariable UUID collaboratorId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Remove collaborator {} from project {} endpoint hit by user: {}",
                    collaboratorId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            projectService.removeCollaborator(projectId, collaboratorId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Collaborator removed successfully", null));
        } catch (RuntimeException e) {
            log.error("Error removing collaborator {} from project {}: {}", collaboratorId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error removing collaborator {} from project {}: {}",
                    collaboratorId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to remove collaborator", null));
        }
    }
}
