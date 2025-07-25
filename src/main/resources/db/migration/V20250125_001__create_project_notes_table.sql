-- Migration to create project_notes table
-- This table stores notes associated with projects

CREATE TABLE project_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_favorite BOOLEAN DEFAULT FALSE,
    tags TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for better performance
CREATE INDEX idx_project_notes_project_id ON project_notes(project_id);
CREATE INDEX idx_project_notes_favorite ON project_notes(is_favorite);
CREATE INDEX idx_project_notes_updated_at ON project_notes(updated_at DESC);

-- Add comments for documentation
COMMENT ON TABLE project_notes IS 'Stores notes associated with projects';
COMMENT ON COLUMN project_notes.id IS 'Unique identifier for the note';
COMMENT ON COLUMN project_notes.project_id IS 'Reference to the project this note belongs to';
COMMENT ON COLUMN project_notes.title IS 'Title of the note';
COMMENT ON COLUMN project_notes.content IS 'Markdown content of the note';
COMMENT ON COLUMN project_notes.is_favorite IS 'Whether the note is marked as favorite';
COMMENT ON COLUMN project_notes.tags IS 'Array of tags associated with the note';
COMMENT ON COLUMN project_notes.created_at IS 'Timestamp when the note was created';
COMMENT ON COLUMN project_notes.updated_at IS 'Timestamp when the note was last updated'; 