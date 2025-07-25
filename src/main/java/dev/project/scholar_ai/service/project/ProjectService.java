package dev.project.scholar_ai.service.project;

import dev.project.scholar_ai.dto.project.AddCollaboratorRequest;
import dev.project.scholar_ai.dto.project.CollaboratorDto;
import dev.project.scholar_ai.dto.project.CreateProjectDto;
import dev.project.scholar_ai.dto.project.ProjectDto;
import dev.project.scholar_ai.dto.project.RemoveCollaboratorRequest;
import dev.project.scholar_ai.dto.project.UpdateCollaboratorRequest;
import dev.project.scholar_ai.dto.project.UpdateProjectDto;
import dev.project.scholar_ai.mapping.project.ProjectMapper;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.model.core.project.Project;
import dev.project.scholar_ai.model.core.project.ProjectCollaborator;
import dev.project.scholar_ai.repository.core.account.UserAccountRepository;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.core.project.ProjectCollaboratorRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectCollaboratorRepository projectCollaboratorRepository;
    private final AuthUserRepository authUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final ProjectMapper projectMapper;

    /**
     * Create a new project
     */
    public ProjectDto createProject(CreateProjectDto createProjectDto, UUID userId) {
        log.info("Creating new project: {} for user: {}", createProjectDto.name(), userId);

        Project project = projectMapper.fromCreateDto(createProjectDto, userId);
        Project savedProject = projectRepository.save(project);

        log.info("Project created successfully with ID: {}", savedProject.getId());
        return projectMapper.toDto(savedProject);
    }

    /**
     * Get project by ID and validate user ownership or collaboration
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public ProjectDto getProjectById(UUID projectId, UUID userId) {
        log.info("Fetching project with ID: {} for user: {}", projectId, userId);

        // First try to find as owner
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElse(null);

        // If not found as owner, check if user is a collaborator
        if (project == null) {
            boolean isCollaborator = projectCollaboratorRepository
                    .findByProjectIdAndCollaboratorId(projectId, userId)
                    .isPresent();

            if (!isCollaborator) {
                throw new RuntimeException("Project not found or access denied");
            }

            // If user is collaborator, get the project without user restriction
            project =
                    projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        }

        return projectMapper.toDto(project);
    }

    /**
     * Get all projects for a user (including collaborated projects)
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<ProjectDto> getProjectsByUserId(UUID userId) {
        log.info("Fetching all projects for user: {}", userId);

        List<Project> projects = projectRepository.findProjectsByUserIdOrCollaboratorId(userId);
        return projects.stream()
                .map(project -> {
                    ProjectDto dto = projectMapper.toDto(project);
                    // Add collaborator information
                    List<CollaboratorDto> collaborators = getCollaboratorsForProject(project.getId());
                    return new ProjectDto(
                            dto.id(),
                            dto.name(),
                            dto.description(),
                            dto.domain(),
                            dto.topics(),
                            dto.tags(),
                            dto.userId(),
                            dto.status(),
                            dto.progress(),
                            dto.totalPapers(),
                            dto.activeTasks(),
                            dto.createdAt(),
                            dto.updatedAt(),
                            dto.lastActivity(),
                            dto.isStarred(),
                            collaborators);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get projects by status for a user
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<ProjectDto> getProjectsByUserIdAndStatus(UUID userId, Project.Status status) {
        log.info("Fetching projects with status: {} for user: {}", status, userId);

        List<Project> projects = projectRepository.findByUserIdAndStatus(userId, status);
        return projects.stream().map(projectMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get starred projects for a user
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<ProjectDto> getStarredProjects(UUID userId) {
        log.info("Fetching starred projects for user: {}", userId);

        List<Project> projects = projectRepository.findStarredProjectsByUserId(userId);
        return projects.stream().map(projectMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Update an existing project
     */
    public ProjectDto updateProject(UUID projectId, UpdateProjectDto updateProjectDto, UUID userId) {
        log.info("Updating project with ID: {} for user: {}", projectId, userId);

        // First try to find as owner
        Project existingProject =
                projectRepository.findByIdAndUserId(projectId, userId).orElse(null);

        // If not found as owner, check if user is a collaborator with edit permissions
        if (existingProject == null) {
            ProjectCollaborator collaboration = projectCollaboratorRepository
                    .findByProjectIdAndCollaboratorId(projectId, userId)
                    .orElse(null);

            if (collaboration == null
                    || (collaboration.getRole() != ProjectCollaborator.CollaborationRole.EDITOR
                            && collaboration.getRole() != ProjectCollaborator.CollaborationRole.ADMIN)) {
                throw new RuntimeException("Project not found or access denied");
            }

            // If user is collaborator with edit permissions, get the project without user
            // restriction
            existingProject =
                    projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        }

        // Update fields from DTO
        if (updateProjectDto.name() != null) {
            existingProject.setName(updateProjectDto.name());
        }
        if (updateProjectDto.description() != null) {
            existingProject.setDescription(updateProjectDto.description());
        }
        if (updateProjectDto.domain() != null) {
            existingProject.setDomain(updateProjectDto.domain());
        }
        if (updateProjectDto.topics() != null) {
            existingProject.setTopics(projectMapper.listToString(updateProjectDto.topics()));
        }
        if (updateProjectDto.tags() != null) {
            existingProject.setTags(projectMapper.listToString(updateProjectDto.tags()));
        }
        if (updateProjectDto.status() != null) {
            existingProject.setStatus(projectMapper.stringToStatusEnum(updateProjectDto.status()));
        }
        if (updateProjectDto.progress() != null) {
            existingProject.setProgress(updateProjectDto.progress());
        }
        if (updateProjectDto.lastActivity() != null) {
            existingProject.setLastActivity(updateProjectDto.lastActivity());
        }
        if (updateProjectDto.isStarred() != null) {
            existingProject.setIsStarred(updateProjectDto.isStarred());
        }

        Project savedProject = projectRepository.save(existingProject);

        log.info("Project updated successfully with ID: {}", savedProject.getId());
        return projectMapper.toDto(savedProject);
    }

    /**
     * Delete a project
     */
    public void deleteProject(UUID projectId, UUID userId) {
        log.info("Deleting project with ID: {} for user: {}", projectId, userId);

        // Only project owners can delete projects
        Project project = projectRepository
                .findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Project not found or access denied"));

        projectRepository.delete(project);

        log.info("Project deleted successfully with ID: {}", projectId);
    }

    /**
     * Update project paper count
     */
    public void updateProjectPaperCount(UUID projectId, int totalPapers) {
        log.info("Updating paper count for project: {} to: {}", projectId, totalPapers);

        Project project =
                projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        project.setTotalPapers(totalPapers);
        projectRepository.save(project);
    }

    /**
     * Update project active tasks count
     */
    public void updateProjectActiveTasksCount(UUID projectId, int activeTasks) {
        log.info("Updating active tasks count for project: {} to: {}", projectId, activeTasks);

        Project project =
                projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        project.setActiveTasks(activeTasks);
        projectRepository.save(project);
    }

    /**
     * Get project count by status for a user
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public long getProjectCountByStatus(UUID userId, Project.Status status) {
        return projectRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * Toggle project starred status
     */
    public ProjectDto toggleProjectStar(UUID projectId, UUID userId) {
        log.info("Toggling star status for project: {} and user: {}", projectId, userId);

        // First try to find as owner
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElse(null);

        // If not found as owner, check if user is a collaborator
        if (project == null) {
            boolean isCollaborator = projectCollaboratorRepository
                    .findByProjectIdAndCollaboratorId(projectId, userId)
                    .isPresent();

            if (!isCollaborator) {
                throw new RuntimeException("Project not found or access denied");
            }

            // If user is collaborator, get the project without user restriction
            project =
                    projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        }

        project.setIsStarred(!project.getIsStarred());
        Project savedProject = projectRepository.save(project);

        log.info("Project star status toggled successfully for ID: {}", projectId);
        return projectMapper.toDto(savedProject);
    }

    /**
     * Get collaborators for a project
     */
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<CollaboratorDto> getCollaboratorsForProject(UUID projectId) {
        log.info("Fetching collaborators for project: {}", projectId);

        List<ProjectCollaborator> collaborators = projectCollaboratorRepository.findByProjectId(projectId);
        return collaborators.stream()
                .map(collaborator -> {
                    UserAccount userAccount = userAccountRepository
                            .findById(collaborator.getCollaboratorId())
                            .orElse(null);
                    String name = userAccount != null ? userAccount.getFullName() : "";
                    return new CollaboratorDto(
                            collaborator.getId(),
                            collaborator.getProjectId(),
                            collaborator.getCollaboratorId(),
                            collaborator.getCollaboratorEmail(),
                            name,
                            collaborator.getOwnerEmail(),
                            collaborator.getRole().name(),
                            collaborator.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * Add a collaborator to a project
     */
    public CollaboratorDto addCollaborator(UUID projectId, AddCollaboratorRequest request, UUID ownerId) {
        log.info("Adding collaborator {} to project {} by owner {}", request.collaboratorEmail(), projectId, ownerId);

        // Verify project ownership
        Project project = projectRepository
                .findByIdAndUserId(projectId, ownerId)
                .orElseThrow(() -> new RuntimeException("Project not found or access denied"));

        // Find collaborator by email
        AuthUser collaboratorUser = authUserRepository
                .findByEmail(request.collaboratorEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.collaboratorEmail()));

        // Check if collaboration already exists
        if (projectCollaboratorRepository.existsByProjectIdAndCollaboratorId(projectId, collaboratorUser.getId())) {
            throw new RuntimeException("Collaborator already exists for this project");
        }

        // Get owner email
        AuthUser ownerUser =
                authUserRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));

        // Create collaboration
        ProjectCollaborator collaboration = new ProjectCollaborator();
        collaboration.setProjectId(projectId);
        collaboration.setCollaboratorId(collaboratorUser.getId());
        collaboration.setCollaboratorEmail(collaboratorUser.getEmail());
        collaboration.setOwnerEmail(ownerUser.getEmail());
        collaboration.setRole(
                ProjectCollaborator.CollaborationRole.valueOf(request.role().toUpperCase()));

        ProjectCollaborator savedCollaboration = projectCollaboratorRepository.save(collaboration);

        // Get user account details
        UserAccount userAccount =
                userAccountRepository.findById(collaboratorUser.getId()).orElse(null);
        String email = collaboratorUser.getEmail();
        String name = userAccount != null ? userAccount.getFullName() : "";

        log.info("Collaborator added successfully to project: {}", projectId);
        return new CollaboratorDto(
                savedCollaboration.getId(),
                savedCollaboration.getProjectId(),
                savedCollaboration.getCollaboratorId(),
                savedCollaboration.getCollaboratorEmail(),
                name,
                savedCollaboration.getOwnerEmail(),
                savedCollaboration.getRole().name(),
                savedCollaboration.getCreatedAt());
    }

    /**
     * Remove a collaborator from a project
     */
    public void removeCollaborator(UUID projectId, RemoveCollaboratorRequest request, UUID ownerId) {
        log.info(
                "Removing collaborator {} from project {} by owner {}",
                request.collaboratorEmail(),
                projectId,
                ownerId);

        // Verify project ownership
        projectRepository
                .findByIdAndUserId(projectId, ownerId)
                .orElseThrow(() -> new RuntimeException("Project not found or access denied"));

        // Find collaborator by email
        AuthUser collaboratorUser = authUserRepository
                .findByEmail(request.collaboratorEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.collaboratorEmail()));

        // Remove collaboration
        projectCollaboratorRepository.deleteByProjectIdAndCollaboratorId(projectId, collaboratorUser.getId());

        log.info("Collaborator removed successfully from project: {}", projectId);
    }

    /**
     * Update a collaborator's role in a project
     */
    public CollaboratorDto updateCollaborator(UUID projectId, UpdateCollaboratorRequest request, UUID ownerId) {
        log.info(
                "Updating collaborator {} role to {} in project {} by owner {}",
                request.collaboratorEmail(),
                request.role(),
                projectId,
                ownerId);

        // Verify project ownership
        projectRepository
                .findByIdAndUserId(projectId, ownerId)
                .orElseThrow(() -> new RuntimeException("Project not found or access denied"));

        // Find collaborator by email
        AuthUser collaboratorUser = authUserRepository
                .findByEmail(request.collaboratorEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.collaboratorEmail()));

        // Find existing collaboration
        ProjectCollaborator collaboration = projectCollaboratorRepository
                .findByProjectIdAndCollaboratorId(projectId, collaboratorUser.getId())
                .orElseThrow(() -> new RuntimeException("Collaborator not found for this project"));

        // Update role
        try {
            ProjectCollaborator.CollaborationRole newRole =
                    ProjectCollaborator.CollaborationRole.valueOf(request.role().toUpperCase());
            collaboration.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid collaboration role: " + request.role());
        }

        ProjectCollaborator savedCollaboration = projectCollaboratorRepository.save(collaboration);

        // Get user account details
        UserAccount userAccount =
                userAccountRepository.findById(collaboratorUser.getId()).orElse(null);
        String name = userAccount != null ? userAccount.getFullName() : "";

        log.info("Collaborator role updated successfully in project: {}", projectId);
        return new CollaboratorDto(
                savedCollaboration.getId(),
                savedCollaboration.getProjectId(),
                savedCollaboration.getCollaboratorId(),
                savedCollaboration.getCollaboratorEmail(),
                name,
                savedCollaboration.getOwnerEmail(),
                savedCollaboration.getRole().name(),
                savedCollaboration.getCreatedAt());
    }
}
