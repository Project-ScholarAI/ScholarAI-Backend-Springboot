package dev.project.scholar_ai.controller;

import dev.project.scholar_ai.model.paper.Paper;
import dev.project.scholar_ai.service.PaperService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private final PaperService paperService;

    @Autowired
    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @GetMapping
    public ResponseEntity<List<Paper>> getAllPapers() {
        List<Paper> papers = paperService.getAllPapers();
        return ResponseEntity.ok(papers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paper> getPaperById(@PathVariable Long id) {
        Optional<Paper> paper = paperService.getPaperById(id);
        return paper.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Paper> createPaper(@RequestBody Paper paper) {
        Paper savedPaper = paperService.savePaper(paper);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPaper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paper> updatePaper(@PathVariable Long id, @RequestBody Paper paperDetails) {
        try {
            Paper updatedPaper = paperService.updatePaper(id, paperDetails);
            return ResponseEntity.ok(updatedPaper);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) {
        try {
            paperService.deletePaper(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Paper>> searchByTitle(@RequestParam String title) {
        List<Paper> papers = paperService.searchPapersByTitle(title);
        return ResponseEntity.ok(papers);
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<Paper>> searchByAuthor(@RequestParam String author) {
        List<Paper> papers = paperService.searchPapersByAuthor(author);
        return ResponseEntity.ok(papers);
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<Paper>> searchByKeyword(@RequestParam String keyword) {
        List<Paper> papers = paperService.searchPapersByKeyword(keyword);
        return ResponseEntity.ok(papers);
    }

    @GetMapping("/search/journal")
    public ResponseEntity<List<Paper>> searchByJournal(@RequestParam String journal) {
        List<Paper> papers = paperService.getPapersByJournal(journal);
        return ResponseEntity.ok(papers);
    }

    @GetMapping("/search/date-range")
    public ResponseEntity<List<Paper>> searchByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<Paper> papers = paperService.getPapersByDateRange(start, end);
            return ResponseEntity.ok(papers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
