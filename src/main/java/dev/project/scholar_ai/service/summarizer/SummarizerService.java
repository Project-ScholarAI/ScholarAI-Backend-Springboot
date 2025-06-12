package dev.project.scholar_ai.service.summarizer;

import dev.project.scholar_ai.dto.agent.request.SummarizationRequest;
import dev.project.scholar_ai.messaging.publisher.SummarizationRequestSender;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.service.websearch.PaperPersistenceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummarizerService {

    private final SummarizationRequestSender requestSender;
    private final PaperPersistenceService paperPersistenceService;

    /**
     * Initiate content extraction & summarization for all papers belonging to the given correlation ID.
     *
     * @param correlationId correlation ID for the originating web search
     */
    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public void initiateSummarization(String correlationId) {
        List<Paper> papers = paperPersistenceService.findPapersByCorrelationId(correlationId);
        if (papers.isEmpty()) {
            throw new IllegalArgumentException("No papers found for correlationId " + correlationId);
        }

        List<UUID> paperIds = papers.stream().map(Paper::getId).toList();
        log.info("Sending summarization request for {} papers (correlationId={})", paperIds.size(), correlationId);

        SummarizationRequest request = new SummarizationRequest(papers.get(0).getId() != null ? null : null, correlationId, paperIds);
        requestSender.send(request);
    }
} 