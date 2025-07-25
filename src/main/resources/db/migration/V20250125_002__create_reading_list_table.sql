-- Migration to create reading_list table
-- This table stores reading list items associated with projects

CREATE TABLE reading_list (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    paper_id UUID NOT NULL REFERENCES papers(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'in-progress', 'completed', 'skipped')),
    priority VARCHAR(20) NOT NULL DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'critical')),
    difficulty VARCHAR(20) NOT NULL DEFAULT 'medium' CHECK (difficulty IN ('easy', 'medium', 'hard', 'expert')),
    relevance VARCHAR(20) NOT NULL DEFAULT 'medium' CHECK (relevance IN ('low', 'medium', 'high', 'critical')),
    estimated_time INTEGER CHECK (estimated_time > 0),
    actual_time INTEGER CHECK (actual_time > 0),
    notes TEXT,
    tags TEXT[],
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    reading_progress INTEGER NOT NULL DEFAULT 0 CHECK (reading_progress >= 0 AND reading_progress <= 100),
    read_count INTEGER NOT NULL DEFAULT 0 CHECK (read_count >= 0),
    is_bookmarked BOOLEAN NOT NULL DEFAULT FALSE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    recommended_by VARCHAR(100),
    recommended_reason TEXT,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_read_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Ensure unique paper per project
    UNIQUE(project_id, paper_id)
);

-- Indexes for better performance
CREATE INDEX idx_reading_list_project_id ON reading_list(project_id);
CREATE INDEX idx_reading_list_status ON reading_list(status);
CREATE INDEX idx_reading_list_priority ON reading_list(priority);
CREATE INDEX idx_reading_list_difficulty ON reading_list(difficulty);
CREATE INDEX idx_reading_list_relevance ON reading_list(relevance);
CREATE INDEX idx_reading_list_is_bookmarked ON reading_list(is_bookmarked);
CREATE INDEX idx_reading_list_is_recommended ON reading_list(is_recommended);
CREATE INDEX idx_reading_list_added_at ON reading_list(added_at DESC);
CREATE INDEX idx_reading_list_last_read_at ON reading_list(last_read_at DESC);
CREATE INDEX idx_reading_list_reading_progress ON reading_list(reading_progress);
CREATE INDEX idx_reading_list_rating ON reading_list(rating);

-- Composite indexes for common query patterns
CREATE INDEX idx_reading_list_project_status ON reading_list(project_id, status);
CREATE INDEX idx_reading_list_project_priority ON reading_list(project_id, priority);
CREATE INDEX idx_reading_list_project_bookmarked ON reading_list(project_id, is_bookmarked);
CREATE INDEX idx_reading_list_project_recommended ON reading_list(project_id, is_recommended);

-- Add comments for documentation
COMMENT ON TABLE reading_list IS 'Stores reading list items associated with projects';
COMMENT ON COLUMN reading_list.id IS 'Unique identifier for the reading list item';
COMMENT ON COLUMN reading_list.project_id IS 'Reference to the project this item belongs to';
COMMENT ON COLUMN reading_list.paper_id IS 'Reference to the paper in the reading list';
COMMENT ON COLUMN reading_list.status IS 'Current reading status: pending, in-progress, completed, skipped';
COMMENT ON COLUMN reading_list.priority IS 'Priority level: low, medium, high, critical';
COMMENT ON COLUMN reading_list.difficulty IS 'Perceived difficulty: easy, medium, hard, expert';
COMMENT ON COLUMN reading_list.relevance IS 'Relevance to project: low, medium, high, critical';
COMMENT ON COLUMN reading_list.estimated_time IS 'Estimated reading time in minutes';
COMMENT ON COLUMN reading_list.actual_time IS 'Actual time spent reading in minutes';
COMMENT ON COLUMN reading_list.notes IS 'User notes about the paper';
COMMENT ON COLUMN reading_list.tags IS 'Array of tags for categorization';
COMMENT ON COLUMN reading_list.rating IS 'User rating (1-5 stars) for completed items';
COMMENT ON COLUMN reading_list.reading_progress IS 'Reading progress percentage (0-100)';
COMMENT ON COLUMN reading_list.read_count IS 'Number of times the paper has been read';
COMMENT ON COLUMN reading_list.is_bookmarked IS 'Whether the item is bookmarked';
COMMENT ON COLUMN reading_list.is_recommended IS 'Whether the item was recommended';
COMMENT ON COLUMN reading_list.recommended_by IS 'Source of recommendation (ai-system, user, etc.)';
COMMENT ON COLUMN reading_list.recommended_reason IS 'Reason for recommendation';
COMMENT ON COLUMN reading_list.added_at IS 'When the item was added to reading list';
COMMENT ON COLUMN reading_list.started_at IS 'When reading was started';
COMMENT ON COLUMN reading_list.completed_at IS 'When reading was completed';
COMMENT ON COLUMN reading_list.last_read_at IS 'Last time the paper was read';
COMMENT ON COLUMN reading_list.updated_at IS 'Last modification timestamp'; 