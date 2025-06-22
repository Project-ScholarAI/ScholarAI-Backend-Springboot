-- Migration to complete the transition from project_id to correlation_id in papers table
-- This migration makes project_id nullable since we now only use correlation_id

-- 1. Make project_id column nullable to support the new correlation_id approach
ALTER TABLE papers ALTER COLUMN project_id DROP NOT NULL;

-- 2. Make correlation_id column NOT NULL since it's now the primary reference
ALTER TABLE papers ALTER COLUMN correlation_id SET NOT NULL;

-- 3. Add comments for documentation
COMMENT ON COLUMN papers.project_id IS 'Legacy project reference - kept for backward compatibility (nullable)';
COMMENT ON COLUMN papers.correlation_id IS 'Primary reference to web search operation (required)'; 