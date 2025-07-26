package dev.project.scholar_ai.service;

import dev.project.scholar_ai.dto.papercall.PaperCallResponse;
import dev.project.scholar_ai.dto.papercall.PaperCallStatisticsResponse;
import dev.project.scholar_ai.model.core.papercall.PaperCall;
import dev.project.scholar_ai.repository.core.papercall.PaperCallRepository;
import dev.project.scholar_ai.service.papercall.PaperCallService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaperCallServiceTest {

    @Mock
    private PaperCallRepository paperCallRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaperCallService paperCallService;

    private UUID testUserId;
    private String testDomain;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testDomain = "machine learning";
    }

    @Test
    void testGetPaperCallsForUser() {
        // Given
        List<PaperCall> mockPaperCalls = Arrays.asList(
            createMockPaperCall("Conference 1", "conference", "WikiCFP"),
            createMockPaperCall("Journal 1", "journal", "MDPI")
        );
        Page<PaperCall> mockPage = new PageImpl<>(mockPaperCalls);
        
        when(paperCallRepository.findByUserIdWithFilters(
            eq(testUserId), eq(testDomain), eq("conference"), eq("WikiCFP"), 
            eq("search"), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        Page<PaperCallResponse> result = paperCallService.getPaperCallsForUser(
            testUserId, testDomain, "conference", "WikiCFP", "search", 0, 20, "createdAt", "desc"
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Conference 1", result.getContent().get(0).getTitle());
        assertEquals("conference", result.getContent().get(0).getType());
    }

    @Test
    void testGetStatistics() {
        // Given
        when(paperCallRepository.countByUserIdAndDomain(testUserId, testDomain)).thenReturn(5L);
        when(paperCallRepository.countByTypeForUser(testUserId, testDomain))
            .thenReturn(Arrays.asList(new Object[]{"conference", 3}, new Object[]{"journal", 2}));
        when(paperCallRepository.countBySourceForUser(testUserId, testDomain))
            .thenReturn(Arrays.asList(new Object[]{"WikiCFP", 3}, new Object[]{"MDPI", 2}));

        // When
        PaperCallStatisticsResponse result = paperCallService.getStatistics(testUserId, testDomain);

        // Then
        assertNotNull(result);
        assertEquals(testDomain, result.getDomain());
        assertEquals(5, result.getTotalCalls());
        assertEquals(3, result.getConferences());
        assertEquals(2, result.getJournals());
        assertEquals(2, result.getSources().size());
        assertEquals(3, result.getSources().get("WikiCFP"));
        assertEquals(2, result.getSources().get("MDPI"));
    }

    @Test
    void testRefreshPaperCalls() {
        // Given
        PaperCallResponse[] mockResponses = {
            createMockPaperCallResponse("Conference 1", "conference", "WikiCFP"),
            createMockPaperCallResponse("Journal 1", "journal", "MDPI")
        };
        
        ResponseEntity<PaperCallResponse[]> mockResponseEntity = 
            new ResponseEntity<>(mockResponses, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(PaperCallResponse[].class)))
            .thenReturn(mockResponseEntity);

        // When
        assertDoesNotThrow(() -> paperCallService.refreshPaperCalls(testUserId, testDomain));

        // Then
        verify(paperCallRepository).deleteByUserIdAndDomain(testUserId, testDomain);
        verify(paperCallRepository).saveAll(anyList());
    }

    private PaperCall createMockPaperCall(String title, String type, String source) {
        return PaperCall.builder()
            .id(UUID.randomUUID())
            .userId(testUserId)
            .title(title)
            .link("http://example.com")
            .type(type)
            .source(source)
            .domain(testDomain)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private PaperCallResponse createMockPaperCallResponse(String title, String type, String source) {
        return PaperCallResponse.builder()
            .title(title)
            .link("http://example.com")
            .type(type)
            .source(source)
            .domain(testDomain)
            .createdAt(LocalDateTime.now())
            .build();
    }
} 