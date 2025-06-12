package dev.project.scholar_ai.dto.event;

import java.util.List;
import java.util.UUID;

public record ContentExtractionCompletedEvent(
        UUID projectId,
        String correlationId,
        String sourceCorrelationId,
        int totalPapers,
        int papersProcessed,
        int papersExtracted,
        int papersSummarized,
        int citationsExtracted,
        List<PaperExtractionResult> results) {
    
    public record PaperExtractionResult(
            UUID paperId,
            String doi,
            String title,
            ExtractionStatus extractionStatus,
            SummaryStatus summaryStatus,
            String errorMessage,
            
            // Extracted content fields
            String fullText,
            String titleExtracted,
            String abstractExtracted,
            String introduction,
            String methodology,
            String results,
            String discussion,
            String conclusion,
            String references,
            String acknowledgments,
            String extractedKeywords,
            String keyPhrases,
            Integer figuresCount,
            Integer tablesCount,
            Integer equationsCount,
            Integer wordCount,
            Integer sectionCount,
            Integer referenceCount,
            
            // Summary fields
            String problemMotivation,
            String keyContributions,
            String methodOverview,
            String dataExperimentalSetup,
            String headlineResults,
            String limitationsFailures,
            String practicalImplications,
            String structuredFacts,
            
            // Citation information
            List<CitationInfo> citationsFound) {}
    
    public record CitationInfo(
            String citedTitle,
            String citedDoi,
            String citationContext,
            String citationPurpose,
            String sectionMentioned,
            Double citationStrength) {}
    
    public enum ExtractionStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
    }
    
    public enum SummaryStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, OUTDATED
    }
} 