package dev.project.scholar_ai.service;

import dev.project.scholar_ai.model.paper.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public Optional<Paper> getPaperById(Long id) {
        return paperRepository.findById(id);
    }

    public Paper savePaper(Paper paper) {
        return paperRepository.save(paper);
    }

    public Paper updatePaper(Long id, Paper paperDetails) {
        Paper paper =
                paperRepository.findById(id).orElseThrow(() -> new RuntimeException("Paper not found with id: " + id));

        paper.setTitle(paperDetails.getTitle());
        paper.setAuthors(paperDetails.getAuthors());
        paper.setAbstractText(paperDetails.getAbstractText());
        paper.setPublicationDate(paperDetails.getPublicationDate());
        paper.setJournal(paperDetails.getJournal());
        paper.setDoi(paperDetails.getDoi());

        return paperRepository.save(paper);
    }

    public void deletePaper(Long id) {
        paperRepository.deleteById(id);
    }

    public List<Paper> searchPapersByTitle(String title) {
        return paperRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Paper> searchPapersByAuthor(String author) {
        return paperRepository.findByAuthorsContainingIgnoreCase(author);
    }

    public List<Paper> searchPapersByKeyword(String keyword) {
        return paperRepository.searchByKeyword(keyword);
    }

    public List<Paper> getPapersByDateRange(LocalDate startDate, LocalDate endDate) {
        return paperRepository.findByPublicationDateBetween(startDate, endDate);
    }

    public List<Paper> getPapersByJournal(String journal) {
        return paperRepository.findByJournalIgnoreCase(journal);
    }
}
