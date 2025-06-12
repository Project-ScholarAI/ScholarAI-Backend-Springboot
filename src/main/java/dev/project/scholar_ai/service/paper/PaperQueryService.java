package dev.project.scholar_ai.service.paper;

import dev.project.scholar_ai.dto.paper.metadata.PaperPdfDto;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaperQueryService {

    private final PaperRepository paperRepository;

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public PaperPdfDto getPdfInfo(UUID paperId) {
        Paper paper = paperRepository
                .findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));
        return new PaperPdfDto(paper.getId(), paper.getPdfContentUrl(), paper.getPdfUrl());
    }
} 