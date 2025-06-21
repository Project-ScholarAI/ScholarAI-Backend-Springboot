-- Migration to add unique constraints for paper deduplication
-- This ensures database-level uniqueness for DOI and other key identifiers

-- 1. Add unique constraint on DOI (most important for deduplication)
-- Note: We need to handle NULL values properly since DOI might not always be available
CREATE UNIQUE INDEX CONCURRENTLY idx_papers_doi_unique 
ON papers (doi) 
WHERE doi IS NOT NULL AND TRIM(doi) != '';

-- 2. Add unique constraint on Semantic Scholar ID
CREATE UNIQUE INDEX CONCURRENTLY idx_papers_semantic_scholar_id_unique 
ON papers (semantic_scholar_id) 
WHERE semantic_scholar_id IS NOT NULL AND TRIM(semantic_scholar_id) != '';

-- 3. Add unique constraint on external IDs (source + value combination)
-- This prevents the same external ID from being stored multiple times
CREATE UNIQUE INDEX CONCURRENTLY idx_external_ids_source_value_unique 
ON external_ids (source, value) 
WHERE source IS NOT NULL AND value IS NOT NULL AND TRIM(source) != '' AND TRIM(value) != '';

-- 4. Add indexes to improve deduplication query performance
CREATE INDEX CONCURRENTLY idx_papers_doi_lookup 
ON papers (doi) 
WHERE doi IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_papers_semantic_scholar_id_lookup 
ON papers (semantic_scholar_id) 
WHERE semantic_scholar_id IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_external_ids_source_lookup 
ON external_ids (source) 
WHERE source IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_external_ids_value_lookup 
ON external_ids (value) 
WHERE value IS NOT NULL;

-- 5. Add comments for documentation
COMMENT ON INDEX idx_papers_doi_unique IS 'Ensures uniqueness of DOI across all papers (excluding NULL/empty values)';
COMMENT ON INDEX idx_papers_semantic_scholar_id_unique IS 'Ensures uniqueness of Semantic Scholar ID across all papers (excluding NULL/empty values)';
COMMENT ON INDEX idx_external_ids_source_value_unique IS 'Ensures uniqueness of external ID source-value pairs (excluding NULL/empty values)'; 