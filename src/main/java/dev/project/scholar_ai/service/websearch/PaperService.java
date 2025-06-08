package dev.project.scholar_ai.service.websearch;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("paperTransactionManager")
public class PaperService {

    private final PaperRepository paperRepository;

    @Autowired
    public PaperService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    public List<Paper> getAllPapers() {
        return paperRepository.findAll();
    }

    public Optional<Paper> getPaperById(UUID id) {
        return paperRepository.findById(id);
    }

    public Paper savePaper(Paper paper) {
        return paperRepository.save(paper);
    }

    public Paper updatePaper(UUID id, Paper paperDetails) {
        Paper paper =
                paperRepository.findById(id).orElseThrow(() -> new RuntimeException("Paper not found with id: " + id));

        paper.setTitle(paperDetails.getTitle());
        paper.setAuthors(paperDetails.getAuthors());
        paper.setAbstractText(paperDetails.getAbstractText());
        paper.setPublicationDate(paperDetails.getPublicationDate());
        paper.setDoi(paperDetails.getDoi());

        return paperRepository.save(paper);
    }

    public void deletePaper(UUID id) {
        paperRepository.deleteById(id);
    }

    public List<Paper> searchPapersByTitle(String title) {
        return paperRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Paper> searchPapersByAuthor(String author) {
        return paperRepository.findByAuthorNameContainingIgnoreCase(author);
    }

    public List<Paper> searchPapersByKeyword(String keyword) {
        return paperRepository.searchByKeyword(keyword);
    }

    public List<Paper> getPapersByDateRange(LocalDate startDate, LocalDate endDate) {
        return paperRepository.findByPublicationDateBetween(startDate, endDate);
    }

    public List<Paper> getPapersByVenue(String venue) {
        return paperRepository.findByVenueNameContainingIgnoreCase(venue);
    }
}
