-- Migration script to add text extraction fields to papers table
-- Version: V2
-- Description: Add extraction fields for text extraction agent

-- Add extraction fields to papers table
ALTER TABLE papers 
ADD COLUMN IF NOT EXISTS extracted_text TEXT,
ADD COLUMN IF NOT EXISTS extraction_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS extracted_at TIMESTAMP;

-- Add index for extraction status queries
CREATE INDEX IF NOT EXISTS idx_papers_extraction_status 
ON papers(extraction_status);

-- Add index for extraction timestamp queries  
CREATE INDEX IF NOT EXISTS idx_papers_extracted_at 
ON papers(extracted_at);

-- Update any existing papers to have PENDING status
UPDATE papers 
SET extraction_status = 'PENDING' 
WHERE extraction_status IS NULL;