# ScholarAI Notes API

This document provides comprehensive documentation for the Notes API implementation in the ScholarAI backend.

## Overview

The Notes API allows users to create, manage, and organize notes within their research projects. Notes support markdown content, tagging, and favorite status for better organization.

## Features

- ✅ **CRUD Operations**: Create, read, update, and delete notes
- ✅ **Markdown Support**: Rich text content with markdown formatting
- ✅ **Tagging System**: Organize notes with custom tags
- ✅ **Favorite Status**: Mark important notes as favorites
- ✅ **Search Functionality**: Search notes by content or tags
- ✅ **Access Control**: Project-based access with owner/collaborator permissions
- ✅ **RESTful API**: Standard HTTP methods and status codes

## Database Schema

The notes are stored in the `project_notes` table with the following structure:

```sql
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
```

## API Endpoints

### Base URL
```
/api/v1/projects/{projectId}/notes
```

### 1. Get All Notes
**GET** `/api/v1/projects/{projectId}/notes`

Retrieve all notes for a project.

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Notes retrieved successfully",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Research Ideas",
      "content": "# Research Ideas\n\n## Key Concepts\n- **Machine Learning** approaches",
      "createdAt": "2025-07-25T10:00:00.000Z",
      "updatedAt": "2025-07-25T10:30:00.000Z",
      "isFavorite": true,
      "tags": ["ideas", "planning"]
    }
  ]
}
```

### 2. Create Note
**POST** `/api/v1/projects/{projectId}/notes`

Create a new note.

**Request Body:**
```json
{
  "title": "New Note Title",
  "content": "# Note Content\n\nMarkdown content here...",
  "tags": ["tag1", "tag2"]
}
```

**Response (201 Created):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 201,
  "message": "Note created successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "New Note Title",
    "content": "# Note Content\n\nMarkdown content here...",
    "createdAt": "2025-07-25T10:00:00.000Z",
    "updatedAt": "2025-07-25T10:00:00.000Z",
    "isFavorite": false,
    "tags": ["tag1", "tag2"]
  }
}
```

### 3. Update Note
**PUT** `/api/v1/projects/{projectId}/notes/{noteId}`

Update an existing note.

**Request Body:**
```json
{
  "title": "Updated Note Title",
  "content": "# Updated Content\n\nNew markdown content...",
  "tags": ["updated", "tags"]
}
```

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Note updated successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Updated Note Title",
    "content": "# Updated Content\n\nNew markdown content...",
    "createdAt": "2025-07-25T10:00:00.000Z",
    "updatedAt": "2025-07-25T10:30:00.000Z",
    "isFavorite": true,
    "tags": ["updated", "tags"]
  }
}
```

### 4. Delete Note
**DELETE** `/api/v1/projects/{projectId}/notes/{noteId}`

Delete a note.

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Note deleted successfully",
  "data": "Note deleted successfully"
}
```

### 5. Toggle Favorite Status
**PUT** `/api/v1/projects/{projectId}/notes/{noteId}/favorite`

Toggle the favorite status of a note.

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Note favorite status updated",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Note Title",
    "content": "# Note Content",
    "createdAt": "2025-07-25T10:00:00.000Z",
    "updatedAt": "2025-07-25T10:30:00.000Z",
    "isFavorite": true,
    "tags": ["tag1", "tag2"]
  }
}
```

### 6. Get Favorite Notes
**GET** `/api/v1/projects/{projectId}/notes/favorites`

Get all favorite notes for a project.

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Favorite notes retrieved successfully",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Important Research Notes",
      "content": "# Important Notes\n\nKey findings...",
      "createdAt": "2025-07-25T10:00:00.000Z",
      "updatedAt": "2025-07-25T10:30:00.000Z",
      "isFavorite": true,
      "tags": ["important", "findings"]
    }
  ]
}
```

### 7. Search Notes by Tag
**GET** `/api/v1/projects/{projectId}/notes/search/tag?tag={tagName}`

Search notes by tag.

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Notes search completed successfully",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "ML Research Notes",
      "content": "# Machine Learning Notes",
      "createdAt": "2025-07-25T10:00:00.000Z",
      "updatedAt": "2025-07-25T10:30:00.000Z",
      "isFavorite": false,
      "tags": ["ml", "research"]
    }
  ]
}
```

### 8. Search Notes by Content
**GET** `/api/v1/projects/{projectId}/notes/search/content?q={searchTerm}`

Search notes by content (title and content fields).

**Response (200 OK):**
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 200,
  "message": "Notes search completed successfully",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Neural Network Research",
      "content": "# Neural Networks\n\nDeep learning approaches...",
      "createdAt": "2025-07-25T10:00:00.000Z",
      "updatedAt": "2025-07-25T10:30:00.000Z",
      "isFavorite": false,
      "tags": ["neural-networks", "deep-learning"]
    }
  ]
}
```

## Authentication

All endpoints require authentication via JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Access Control

- **Project Owners**: Full access to all notes in their projects
- **Project Collaborators**: Full access to all notes in projects they collaborate on
- **Unauthorized Users**: No access to notes

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 401,
  "message": "Authentication required",
  "data": null
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 404,
  "message": "Note not found",
  "data": null
}
```

### 400 Bad Request
```json
{
  "timestamp": "2025-07-25T10:00:00.000Z",
  "status": 400,
  "message": "Project not found or access denied",
  "data": null
}
```

## Implementation Details

### Project Structure

```
src/main/java/dev/project/scholar_ai/
├── controller/project/
│   └── ProjectNoteController.java          # REST API endpoints
├── dto/project/
│   ├── NoteDto.java                        # Note data transfer object
│   ├── CreateNoteDto.java                  # Create note request DTO
│   └── UpdateNoteDto.java                  # Update note request DTO
├── model/core/project/
│   └── ProjectNote.java                    # JPA entity
├── repository/core/project/
│   └── ProjectNoteRepository.java          # Data access layer
├── service/project/
│   └── ProjectNoteService.java             # Business logic
└── mapping/project/
    └── ProjectNoteMapper.java              # Entity-DTO mapping
```

### Key Components

1. **ProjectNote Entity**: JPA entity mapping to the `project_notes` table
2. **ProjectNoteRepository**: Spring Data JPA repository with custom query methods
3. **ProjectNoteService**: Business logic layer with access control and validation
4. **ProjectNoteController**: REST API endpoints with proper error handling
5. **DTOs**: Data transfer objects for API requests and responses
6. **ProjectNoteMapper**: MapStruct mapper for entity-DTO conversion

### Database Migration

The database migration is located at:
```
src/main/resources/db/migration/V20250125_001__create_project_notes_table.sql
```

## Testing

### Postman Collection

A Postman collection is available at:
```
postman/scholarai-notes.postman_collection.json
```

### Manual Testing

1. **Setup**: Ensure the application is running and database migration is applied
2. **Authentication**: Get a valid JWT token by logging in
3. **Create Project**: Create a project first (if not exists)
4. **Test Endpoints**: Use the Postman collection or curl commands

### Example curl Commands

```bash
# Get all notes
curl -X GET "http://localhost:8080/api/v1/projects/{projectId}/notes" \
  -H "Authorization: Bearer {token}"

# Create note
curl -X POST "http://localhost:8080/api/v1/projects/{projectId}/notes" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Note",
    "content": "# Test\n\nThis is a test note.",
    "tags": ["test", "example"]
  }'

# Update note
curl -X PUT "http://localhost:8080/api/v1/projects/{projectId}/notes/{noteId}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Test Note",
    "content": "# Updated Test\n\nThis is an updated test note.",
    "tags": ["test", "updated"]
  }'

# Toggle favorite
curl -X PUT "http://localhost:8080/api/v1/projects/{projectId}/notes/{noteId}/favorite" \
  -H "Authorization: Bearer {token}"

# Delete note
curl -X DELETE "http://localhost:8080/api/v1/projects/{projectId}/notes/{noteId}" \
  -H "Authorization: Bearer {token}"
```

## Performance Considerations

1. **Indexes**: Database indexes are created for `project_id`, `is_favorite`, and `updated_at` columns
2. **Pagination**: Consider implementing pagination for large note collections
3. **Caching**: Consider Redis caching for frequently accessed notes
4. **Search Optimization**: Full-text search indexes for content search

## Future Enhancements

1. **Rich Text Editor**: Integration with rich text editors
2. **File Attachments**: Support for file uploads in notes
3. **Note Templates**: Predefined note templates
4. **Note Sharing**: Share individual notes with collaborators
5. **Note Versioning**: Track changes and version history
6. **Export/Import**: Export notes to various formats (PDF, Markdown, etc.)
7. **Note Categories**: Hierarchical note organization
8. **Advanced Search**: Full-text search with filters

## Troubleshooting

### Common Issues

1. **Authentication Errors**: Ensure JWT token is valid and not expired
2. **Project Access**: Verify user has access to the project (owner or collaborator)
3. **Database Connection**: Check database connectivity and migration status
4. **Validation Errors**: Ensure request body meets validation requirements

### Logs

Check application logs for detailed error information:
```bash
tail -f logs/application.log
```

## Support

For issues or questions regarding the Notes API, please refer to:
- API Documentation: This README
- Postman Collection: `postman/scholarai-notes.postman_collection.json`
- Database Schema: Migration file in `src/main/resources/db/migration/` 