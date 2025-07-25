package dev.project.scholar_ai.service.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.project.scholar_ai.dto.project.UpdateCollaboratorRequest;
import dev.project.scholar_ai.mapping.project.ProjectMapper;
import dev.project.scholar_ai.model.core.project.ProjectCollaborator;
import dev.project.scholar_ai.repository.core.account.UserAccountRepository;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.core.project.ProjectCollaboratorRepository;
import dev.project.scholar_ai.repository.core.project.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectCollaboratorRepository projectCollaboratorRepository;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void updateCollaborator_ValidRequest_ShouldUpdateRole() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID collaboratorId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UpdateCollaboratorRequest request = new UpdateCollaboratorRequest("test@example.com", "ADMIN");

        // Mock project ownership verification
        when(projectRepository.findByIdAndUserId(projectId, ownerId))
                .thenReturn(Optional.of(mock(dev.project.scholar_ai.model.core.project.Project.class)));

        // Mock AuthUser for collaborator lookup
        dev.project.scholar_ai.model.core.auth.AuthUser mockAuthUser =
                mock(dev.project.scholar_ai.model.core.auth.AuthUser.class);
        when(mockAuthUser.getId()).thenReturn(collaboratorId);
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockAuthUser));

        // Mock existing collaboration
        ProjectCollaborator existingCollaboration = new ProjectCollaborator();
        existingCollaboration.setId(UUID.randomUUID());
        existingCollaboration.setProjectId(projectId);
        existingCollaboration.setCollaboratorId(collaboratorId);
        existingCollaboration.setCollaboratorEmail("test@example.com");
        existingCollaboration.setOwnerEmail("owner@example.com");
        existingCollaboration.setRole(ProjectCollaborator.CollaborationRole.VIEWER);

        when(projectCollaboratorRepository.findByProjectIdAndCollaboratorId(projectId, collaboratorId))
                .thenReturn(Optional.of(existingCollaboration));

        when(projectCollaboratorRepository.save(any(ProjectCollaborator.class))).thenReturn(existingCollaboration);

        // Act
        var result = projectService.updateCollaborator(projectId, request, ownerId);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.role());
        verify(projectCollaboratorRepository).save(any(ProjectCollaborator.class));
    }

    @Test
    void updateCollaborator_InvalidRole_ShouldThrowException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID collaboratorId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UpdateCollaboratorRequest request = new UpdateCollaboratorRequest("test@example.com", "INVALID_ROLE");

        // Mock project ownership verification
        when(projectRepository.findByIdAndUserId(projectId, ownerId))
                .thenReturn(Optional.of(mock(dev.project.scholar_ai.model.core.project.Project.class)));

        // Mock AuthUser for collaborator lookup
        dev.project.scholar_ai.model.core.auth.AuthUser mockAuthUser =
                mock(dev.project.scholar_ai.model.core.auth.AuthUser.class);
        when(mockAuthUser.getId()).thenReturn(collaboratorId);
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockAuthUser));

        // Mock existing collaboration
        ProjectCollaborator existingCollaboration = new ProjectCollaborator();
        when(projectCollaboratorRepository.findByProjectIdAndCollaboratorId(projectId, collaboratorId))
                .thenReturn(Optional.of(existingCollaboration));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.updateCollaborator(projectId, request, ownerId));
    }
}
