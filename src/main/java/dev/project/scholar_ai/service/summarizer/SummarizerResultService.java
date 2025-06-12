package dev.project.scholar_ai.service.summarizer;

import dev.project.scholar_ai.dto.event.SummarizationCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SummarizerResultService {

    public void handleCompletion(SummarizationCompletedEvent event) {
        // TODO: Implement persistence of summary/sections once FastAPI returns detailed payloads.
        log.info("Handled summarization completion event for correlationId={} (processed={}, failed={})", event.correlationId(), event.processed(), event.failed());
    }
} 