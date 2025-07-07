package dev.project.scholar_ai.mapping.paper;

import dev.project.scholar_ai.dto.paper.metadata.AuthorDto;
import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.model.paper.metadata.Author;
import dev.project.scholar_ai.model.paper.metadata.ExternalId;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.model.paper.metadata.PaperMetrics;
import dev.project.scholar_ai.model.paper.metadata.PublicationVenue;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaperMapper {

    PaperMapper INSTANCE = Mappers.getMapper(PaperMapper.class);

    @Mapping(target = "correlationId", ignore = true) // Will be set separately
    @Mapping(target = "authors", source = "authors", qualifiedByName = "mapAuthors")
    @Mapping(target = "externalIds", source = "externalIds", qualifiedByName = "mapExternalIds")
    @Mapping(target = "venue", source = ".", qualifiedByName = "mapVenue")
    @Mapping(target = "metrics", source = ".", qualifiedByName = "mapMetrics")
    @Mapping(target = "publicationTypes", source = "publicationTypes", qualifiedByName = "listToString")
    @Mapping(target = "fieldsOfStudy", source = "fieldsOfStudy", qualifiedByName = "listToString")
    Paper toEntity(PaperMetadataDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "authors", source = "authors", qualifiedByName = "mapAuthorEntities")
    @Mapping(target = "externalIds", source = "externalIds", qualifiedByName = "mapExternalIdEntities")
    @Mapping(target = "venueName", source = "venue.venueName")
    @Mapping(target = "publisher", source = "venue.publisher")
    @Mapping(target = "volume", source = "venue.volume")
    @Mapping(target = "issue", source = "venue.issue")
    @Mapping(target = "pages", source = "venue.pages")
    @Mapping(target = "citationCount", source = "metrics.citationCount")
    @Mapping(target = "referenceCount", source = "metrics.referenceCount")
    @Mapping(target = "influentialCitationCount", source = "metrics.influentialCitationCount")
    @Mapping(target = "publicationTypes", source = "publicationTypes", qualifiedByName = "stringToList")
    @Mapping(target = "fieldsOfStudy", source = "fieldsOfStudy", qualifiedByName = "stringToList")
    PaperMetadataDto toDto(Paper entity);

    @Named("mapAuthors")
    default List<Author> mapAuthors(List<AuthorDto> authorDtos) {
        if (authorDtos == null) return null;
        return authorDtos.stream()
                .map(dto -> Author.builder()
                        .name(dto.name())
                        .authorId(dto.authorId())
                        .orcid(dto.orcid())
                        .affiliation(dto.affiliation())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("mapAuthorEntities")
    default List<AuthorDto> mapAuthorEntities(List<Author> authors) {
        if (authors == null) return null;
        return authors.stream()
                .map(author -> new AuthorDto(
                        author.getName(), author.getAuthorId(), author.getOrcid(), author.getAffiliation()))
                .collect(Collectors.toList());
    }

    @Named("mapExternalIds")
    default List<ExternalId> mapExternalIds(Map<String, Object> externalIdsMap) {
        if (externalIdsMap == null) return null;
        return externalIdsMap.entrySet().stream()
                .map(entry -> ExternalId.builder()
                        .source(entry.getKey())
                        .value(entry.getValue().toString())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("mapExternalIdEntities")
    default Map<String, Object> mapExternalIdEntities(List<ExternalId> externalIds) {
        if (externalIds == null) return null;
        return externalIds.stream().collect(Collectors.toMap(ExternalId::getSource, ExternalId::getValue));
    }

    @Named("mapVenue")
    default PublicationVenue mapVenue(PaperMetadataDto dto) {
        if (dto.venueName() == null
                && dto.publisher() == null
                && dto.volume() == null
                && dto.issue() == null
                && dto.pages() == null) {
            return null;
        }
        return PublicationVenue.builder()
                .venueName(dto.venueName())
                .publisher(dto.publisher())
                .volume(dto.volume())
                .issue(dto.issue())
                .pages(dto.pages())
                .build();
    }

    @Named("mapMetrics")
    default PaperMetrics mapMetrics(PaperMetadataDto dto) {
        if (dto.citationCount() == null && dto.referenceCount() == null && dto.influentialCitationCount() == null) {
            return null;
        }
        return PaperMetrics.builder()
                .citationCount(dto.citationCount())
                .referenceCount(dto.referenceCount())
                .influentialCitationCount(dto.influentialCitationCount())
                .build();
    }

    @Named("listToString")
    default String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    @Named("stringToList")
    default List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        return List.of(str.split(","));
    }
}
