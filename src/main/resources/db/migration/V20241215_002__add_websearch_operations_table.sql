-- Migration to add web_search_operations table and update papers table
-- This migration supports the new correlation ID-based search tracking

-- 1. Create web_search_operations table
CREATE TABLE web_search_operations (
    correlation_id VARCHAR(100) PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    query_terms TEXT NOT NULL,
    domain VARCHAR(100) NOT NULL,
    batch_size INTEGER NOT NULL,
    total_papers_found INTEGER,
    error_message TEXT,
    search_duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add indexes for common queries
CREATE INDEX idx_web_search_operations_project_id ON web_search_operations(project_id);
CREATE INDEX idx_web_search_operations_status ON web_search_operations(status);
CREATE INDEX idx_web_search_operations_submitted_at ON web_search_operations(submitted_at);
CREATE INDEX idx_web_search_operations_project_status ON web_search_operations(project_id, status);

-- 3. Add correlation_id column to papers table
ALTER TABLE papers ADD COLUMN correlation_id VARCHAR(100);

-- 4. Create index on correlation_id for papers
CREATE INDEX idx_papers_correlation_id ON papers(correlation_id);

-- 5. Data migration: For existing papers, we'll need to create dummy web search operations
-- This is for backward compatibility only - new papers will have proper correlation IDs

-- Note: In a real migration, you would need to handle existing data carefully.
-- For this example, we'll assume existing papers can remain with project_id for now
-- and new searches will use the correlation_id approach.

-- 6. Add constraint to ensure papers have either project_id OR correlation_id
-- ALTER TABLE papers ADD CONSTRAINT chk_papers_id_reference 
--   CHECK (
--     (project_id IS NOT NULL AND correlation_id IS NULL) OR 
--     (project_id IS NULL AND correlation_id IS NOT NULL)
--   );

-- Note: The above constraint is commented out to allow for gradual migration
-- You may want to enable it after ensuring all papers have been migrated to use correlation_id

-- 7. Update trigger for web_search_operations updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_web_search_operations_updated_at 
    BEFORE UPDATE ON web_search_operations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 8. Comments for documentation
COMMENT ON TABLE web_search_operations IS 'Tracks web search operations with correlation IDs mapping to projects';
COMMENT ON COLUMN web_search_operations.correlation_id IS 'Unique identifier for tracking search operations';
COMMENT ON COLUMN web_search_operations.project_id IS 'Reference to the project this search belongs to';
COMMENT ON COLUMN web_search_operations.query_terms IS 'JSON string of search terms used';
COMMENT ON COLUMN web_search_operations.search_duration_ms IS 'Time taken for search completion in milliseconds';

COMMENT ON COLUMN papers.correlation_id IS 'Reference to web search operation that found this paper'; 