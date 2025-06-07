package dev.project.scholar_ai.service;

import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.mapping.paper.PaperMapper;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperPersistenceService {

    private final PaperRepository paperRepository;
    private final PaperMapper paperMapper;

    @Transactional(transactionManager = "paperTransactionManager")
    public List<Paper> savePapers(List<PaperMetadataDto> paperDtos, UUID projectId) {
        log.info("Persisting {} papers for project {}", paperDtos.size(), projectId);

        return paperDtos.stream()
                .map(dto -> {
                    try {
                        Paper paper = paperMapper.toEntity(dto);
                        paper.setProjectId(projectId);

                        // Set bidirectional relationships
                        if (paper.getAuthors() != null) {
                            paper.getAuthors().forEach(author -> author.setPaper(paper));
                        }

                        if (paper.getExternalIds() != null) {
                            paper.getExternalIds().forEach(externalId -> externalId.setPaper(paper));
                        }

                        if (paper.getVenue() != null) {
                            paper.getVenue().setPaper(paper);
                        }

                        if (paper.getMetrics() != null) {
                            paper.getMetrics().setPaper(paper);
                        }

                        Paper savedPaper = paperRepository.save(paper);
                        log.debug("Successfully saved paper: {} (DOI: {})", savedPaper.getTitle(), savedPaper.getDoi());
                        return savedPaper;

                    } catch (Exception e) {
                        log.error(
                                "Failed to save paper: {} (DOI: {}). Error: {}",
                                dto.title(),
                                dto.doi(),
                                e.getMessage(),
                                e);
                        throw new RuntimeException("Failed to save paper: " + dto.title(), e);
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<Paper> findPapersByProjectId(UUID projectId) {
        log.debug("Finding papers for project {}", projectId);
        return paperRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<PaperMetadataDto> findPaperDtosByProjectId(UUID projectId) {
        log.debug("Finding paper DTOs for project {}", projectId);
        List<Paper> papers = paperRepository.findByProjectId(projectId);
        return papers.stream().map(paperMapper::toDto).toList();
    }
}
