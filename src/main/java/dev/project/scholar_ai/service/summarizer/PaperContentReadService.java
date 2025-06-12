package dev.project.scholar_ai.service.summarizer;

import dev.project.scholar_ai.dto.summarizer.PaperCitationDto;
import dev.project.scholar_ai.dto.summarizer.PaperSectionDto;
import dev.project.scholar_ai.model.paper.content.PaperSummary;
import dev.project.scholar_ai.repository.paper.PaperCitationRepository;
import dev.project.scholar_ai.repository.paper.PaperSectionRepository;
import dev.project.scholar_ai.repository.paper.PaperSummaryRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaperContentReadService {

    private final PaperSectionRepository sectionRepository;
    private final PaperCitationRepository citationRepository;
    private final PaperSummaryRepository summaryRepository;

    public List<PaperSectionDto> getSections(UUID paperId) {
        return sectionRepository.findByPaperId(paperId).stream()
                .map(sec -> new PaperSectionDto(sec.getHeading(), sec.getContent()))
                .collect(Collectors.toList());
    }

    public List<PaperCitationDto> getCitations(UUID paperId) {
        return citationRepository.findByCitingPaperId(paperId).stream()
                .map(cit -> new PaperCitationDto(cit.getCitedDoi(), cit.getRawCitation()))
                .collect(Collectors.toList());
    }

    public String getSummaryJson(UUID paperId) {
        return summaryRepository.findByPaperId(paperId)
                .map(PaperSummary::getSummaryJson)
                .orElse(null);
    }
} 