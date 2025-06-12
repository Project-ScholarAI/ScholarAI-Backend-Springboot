package dev.project.scholar_ai.service.summarizer;

import dev.project.scholar_ai.dto.summarizer.PaperContentRequest;
import dev.project.scholar_ai.dto.summarizer.PaperSectionDto;
import dev.project.scholar_ai.dto.summarizer.PaperCitationDto;
import dev.project.scholar_ai.model.paper.content.PaperCitation;
import dev.project.scholar_ai.model.paper.content.PaperSection;
import dev.project.scholar_ai.model.paper.content.PaperSummary;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperCitationRepository;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.repository.paper.PaperSectionRepository;
import dev.project.scholar_ai.repository.paper.PaperSummaryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperContentPersistenceService {

    private final PaperRepository paperRepository;
    private final PaperSectionRepository sectionRepository;
    private final PaperCitationRepository citationRepository;
    private final PaperSummaryRepository summaryRepository;

    @Transactional(transactionManager = "paperTransactionManager")
    public void persistContent(PaperContentRequest request) {
        UUID paperId = request.paperId();
        Paper paper = paperRepository
                .findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));

        // Remove old data
        sectionRepository.deleteAll(paper.getSections());
        citationRepository.deleteAll(paper.getCitations());
        paper.getSections().clear();
        paper.getCitations().clear();

        // Sections
        if (request.sections() != null) {
            for (PaperSectionDto dto : request.sections()) {
                PaperSection entity = PaperSection.builder()
                        .paper(paper)
                        .heading(dto.heading())
                        .content(dto.content())
                        .build();
                paper.addSection(entity);
            }
        }

        // Citations
        if (request.citations() != null) {
            for (PaperCitationDto dto : request.citations()) {
                PaperCitation citation = PaperCitation.builder()
                        .citingPaper(paper)
                        .citedDoi(dto.citedDoi())
                        .rawCitation(dto.rawCitation())
                        .build();
                paper.addCitation(citation);
            }
        }

        // Summary
        if (request.summaryJson() != null) {
            PaperSummary summary = paper.getSummary();
            if (summary == null) {
                summary = PaperSummary.builder().paper(paper).summaryJson(request.summaryJson()).build();
                paper.setSummary(summary);
            } else {
                summary.setSummaryJson(request.summaryJson());
            }
        }

        paperRepository.save(paper);
        log.info("Persisted extracted content for paper {}", paperId);
    }
} 