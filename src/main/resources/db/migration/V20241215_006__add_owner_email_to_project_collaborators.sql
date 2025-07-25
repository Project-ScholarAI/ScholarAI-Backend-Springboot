-- Migration to add owner_email field to project_collaborators table
-- This allows storing the project owner's email directly in the collaboration record

-- Add owner_email column to project_collaborators table
ALTER TABLE project_collaborators
ADD COLUMN owner_email VARCHAR(255) NOT NULL DEFAULT '';

-- Add index for owner email lookups to improve performance
CREATE INDEX CONCURRENTLY idx_project_collaborators_owner_email
ON project_collaborators (owner_email);

-- Add comment for documentation
COMMENT ON COLUMN project_collaborators.owner_email IS 'Email address of the project owner for easy reference and lookup';

-- Update existing records to populate the owner email field
-- This will be handled by the application logic when the service methods are updated 