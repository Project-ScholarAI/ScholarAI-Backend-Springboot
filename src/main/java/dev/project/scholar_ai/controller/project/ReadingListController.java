package dev.project.scholar_ai.controller.project;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.project.AddReadingListItemDto;
import dev.project.scholar_ai.dto.project.ReadingListItemDto;
import dev.project.scholar_ai.dto.project.UpdateReadingListItemDto;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.service.project.ReadingListItemService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/projects/{projectId}/reading-list")
@RequiredArgsConstructor
public class ReadingListController {

    private final ReadingListItemService readingListItemService;
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
     * Get all reading list items for a project with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<APIResponse<Map<String, Object>>> getReadingList(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String relevance,
            @RequestParam(required = false) Boolean isBookmarked,
            @RequestParam(required = false) Boolean isRecommended,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "addedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get reading list for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);

            // Validate pagination parameters
            if (page < 1) page = 1;
            if (limit < 1 || limit > 100) limit = 20;

            // Build filters map
            Map<String, String> filters = new HashMap<>();
            if (status != null) filters.put("status", status);
            if (priority != null) filters.put("priority", priority);
            if (difficulty != null) filters.put("difficulty", difficulty);
            if (relevance != null) filters.put("relevance", relevance);
            if (isBookmarked != null) filters.put("isBookmarked", isBookmarked.toString());
            if (isRecommended != null) filters.put("isRecommended", isRecommended.toString());
            if (search != null) filters.put("search", search);
            if (sortBy != null) filters.put("sortBy", sortBy);
            if (sortOrder != null) filters.put("sortOrder", sortOrder);

            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<ReadingListItemDto> itemsPage =
                    readingListItemService.getReadingListItems(projectId, userId, filters, pageable);

            // Build response with pagination info
            Map<String, Object> response = new HashMap<>();
            response.put("items", itemsPage.getContent());
            response.put(
                    "pagination",
                    Map.of(
                            "page", page,
                            "limit", limit,
                            "total", itemsPage.getTotalElements(),
                            "totalPages", itemsPage.getTotalPages(),
                            "hasNext", itemsPage.hasNext(),
                            "hasPrev", itemsPage.hasPrevious()));

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Reading list retrieved successfully", response));
        } catch (RuntimeException e) {
            log.error("Error retrieving reading list for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving reading list for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve reading list", null));
        }
    }

    /**
     * Add a new paper to the reading list
     */
    @PostMapping
    public ResponseEntity<APIResponse<ReadingListItemDto>> addReadingListItem(
            @PathVariable UUID projectId, @Valid @RequestBody AddReadingListItemDto dto, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Add reading list item for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto createdItem = readingListItemService.addReadingListItem(projectId, userId, dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(
                            HttpStatus.CREATED.value(), "Paper added to reading list successfully", createdItem));
        } catch (RuntimeException e) {
            log.error("Error adding reading list item for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error adding reading list item for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to add reading list item", null));
        }
    }

    /**
     * Update a reading list item
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<APIResponse<ReadingListItemDto>> updateReadingListItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateReadingListItemDto dto,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Update reading list item {} for project {} endpoint hit by user: {}",
                    itemId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem =
                    readingListItemService.updateReadingListItem(projectId, itemId, userId, dto);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Reading list item updated successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error("Error updating reading list item {} for project {}: {}", itemId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error updating reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update reading list item", null));
        }
    }

    /**
     * Update reading list item status
     */
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<APIResponse<ReadingListItemDto>> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, String> request,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            String status = request.get("status");
            if (status == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Status is required", null));
            }

            log.info(
                    "Update status of reading list item {} to {} for project {} endpoint hit by user: {}",
                    itemId,
                    status,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem = readingListItemService.updateStatus(projectId, itemId, userId, status);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Status updated successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error(
                    "Error updating status of reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error updating status of reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update status", null));
        }
    }

    /**
     * Update reading progress
     */
    @PatchMapping("/{itemId}/progress")
    public ResponseEntity<APIResponse<ReadingListItemDto>> updateProgress(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Integer> request,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            Integer progress = request.get("readingProgress");
            if (progress == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Reading progress is required", null));
            }

            log.info(
                    "Update progress of reading list item {} to {}% for project {} endpoint hit by user: {}",
                    itemId, progress, projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem = readingListItemService.updateProgress(projectId, itemId, userId, progress);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Reading progress updated successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error(
                    "Error updating progress of reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error updating progress of reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update progress", null));
        }
    }

    /**
     * Delete a reading list item
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<APIResponse<String>> deleteReadingListItem(
            @PathVariable UUID projectId, @PathVariable UUID itemId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Delete reading list item {} from project {} endpoint hit by user: {}",
                    itemId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            readingListItemService.deleteReadingListItem(projectId, itemId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Paper removed from reading list successfully", null));
        } catch (RuntimeException e) {
            log.error("Error deleting reading list item {} from project {}: {}", itemId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error deleting reading list item {} from project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete reading list item", null));
        }
    }

    /**
     * Get reading list statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<APIResponse<Map<String, Object>>> getReadingListStats(
            @PathVariable UUID projectId, @RequestParam(required = false) String timeRange, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info("Get reading list stats for project {} endpoint hit by user: {}", projectId, principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            Map<String, Object> stats = readingListItemService.getReadingListStats(projectId, userId);

            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Reading list statistics retrieved successfully", stats));
        } catch (RuntimeException e) {
            log.error("Error retrieving reading list stats for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving reading list stats for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve reading list statistics",
                            null));
        }
    }

    /**
     * Get recommended items
     */
    @GetMapping("/recommendations")
    public ResponseEntity<APIResponse<List<ReadingListItemDto>>> getRecommendations(
            @PathVariable UUID projectId,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false, defaultValue = "true") boolean excludeRead,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Get reading list recommendations for project {} endpoint hit by user: {}",
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            List<ReadingListItemDto> recommendations =
                    readingListItemService.getRecommendedItems(projectId, userId, limit);

            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Recommendations retrieved successfully", recommendations));
        } catch (RuntimeException e) {
            log.error("Error retrieving recommendations for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving recommendations for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve recommendations", null));
        }
    }

    /**
     * Add or update notes for a reading list item
     */
    @PostMapping("/{itemId}/notes")
    public ResponseEntity<APIResponse<ReadingListItemDto>> updateNotes(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, String> request,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            String notes = request.get("note");
            if (notes == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Note is required", null));
            }

            log.info(
                    "Update notes for reading list item {} in project {} endpoint hit by user: {}",
                    itemId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem = readingListItemService.updateNotes(projectId, itemId, userId, notes);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Note added successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error(
                    "Error updating notes for reading list item {} in project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error updating notes for reading list item {} in project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update notes", null));
        }
    }

    /**
     * Rate a completed reading list item
     */
    @PatchMapping("/{itemId}/rating")
    public ResponseEntity<APIResponse<ReadingListItemDto>> rateItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Integer> request,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            Integer rating = request.get("rating");
            if (rating == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Rating is required", null));
            }

            log.info(
                    "Rate reading list item {} with {} stars for project {} endpoint hit by user: {}",
                    itemId,
                    rating,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem = readingListItemService.rateItem(projectId, itemId, userId, rating);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Rating updated successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error("Error rating reading list item {} for project {}: {}", itemId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error rating reading list item {} for project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update rating", null));
        }
    }

    /**
     * Toggle bookmark status
     */
    @PutMapping("/{itemId}/bookmark")
    public ResponseEntity<APIResponse<ReadingListItemDto>> toggleBookmark(
            @PathVariable UUID projectId, @PathVariable UUID itemId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null));
            }

            log.info(
                    "Toggle bookmark for reading list item {} in project {} endpoint hit by user: {}",
                    itemId,
                    projectId,
                    principal.getName());

            UUID userId = getUserIdFromPrincipal(principal);
            ReadingListItemDto updatedItem = readingListItemService.toggleBookmark(projectId, itemId, userId);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Bookmark toggled successfully", updatedItem));
        } catch (RuntimeException e) {
            log.error(
                    "Error toggling bookmark for reading list item {} in project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error(
                    "Unexpected error toggling bookmark for reading list item {} in project {}: {}",
                    itemId,
                    projectId,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to toggle bookmark", null));
        }
    }
}
