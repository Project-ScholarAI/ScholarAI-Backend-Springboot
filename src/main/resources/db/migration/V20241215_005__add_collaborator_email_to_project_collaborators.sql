-- Migration to add collaborator_email field to project_collaborators table
-- This allows storing the collaborator's email directly in the collaboration record

-- Add collaborator_email column to project_collaborators table
ALTER TABLE project_collaborators 
ADD COLUMN collaborator_email VARCHAR(255) NOT NULL DEFAULT '';

-- Add index for email lookups to improve performance
CREATE INDEX CONCURRENTLY idx_project_collaborators_email 
ON project_collaborators (collaborator_email);

-- Add comment for documentation
COMMENT ON COLUMN project_collaborators.collaborator_email IS 'Email address of the collaborator for easy reference and lookup';

-- Update existing records to populate the email field
-- This will be handled by the application logic when the service methods are updated 