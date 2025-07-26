# PaperCall Implementation in Spring Backend

## Overview

The PaperCall functionality in the Spring backend acts as a bridge between the frontend and the FastAPI backend. It calls the FastAPI `/calls` endpoint to fetch paper call data and stores it in the local database for efficient filtering and querying.

## Architecture

```
Frontend → Spring Backend → FastAPI Backend → External Sources
                ↓
            Database (paper_call table)
```

## Database Schema

### paper_call Table

```sql
CREATE TABLE paper_call (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    link TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('conference', 'journal')),
    source VARCHAR(100) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    when_info VARCHAR(255),
    where_info VARCHAR(255),
    deadline VARCHAR(255),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## API Endpoints

### 1. Get All Paper Calls
```
GET /api/papercall/calls
```

**Query Parameters:**
- `domain` (optional): Research domain to filter by
- `type` (optional): Filter by type ('conference' or 'journal')
- `source` (optional): Filter by source ('WikiCFP', 'MDPI', 'Taylor & Francis', 'Springer')
- `searchTerm` (optional): Search in titles
- `page` (default: 0): Page number
- `size` (default: 20): Page size
- `sortBy` (default: 'createdAt'): Sort field
- `sortDir` (default: 'desc'): Sort direction

### 2. Get Conferences Only
```
GET /api/papercall/conferences?domain={domain}
```

### 3. Get Journals Only
```
GET /api/papercall/journals?domain={domain}
```

### 4. Get by Source
```
GET /api/papercall/source/{source}?domain={domain}
```

### 5. Get Statistics
```
GET /api/papercall/statistics?domain={domain}
```

### 6. Refresh Paper Calls
```
POST /api/papercall/refresh?domain={domain}
```

### 7. Health Check
```
GET /api/papercall/health
```

## Configuration

Add to `application-dev.yml`:

```yaml
app:
  fastapi:
    base-url: ${FASTAPI_BASE_URL:http://localhost:8000}
```

Environment variable:
```bash
FASTAPI_BASE_URL=http://localhost:8000
```

## Key Features

### 1. User-Specific Data
- All paper calls are associated with a user ID
- Users can only see their own paper calls
- Data is filtered by user ID in all queries

### 2. Comprehensive Filtering
- **Domain**: Filter by research domain
- **Type**: Filter by 'conference' or 'journal'
- **Source**: Filter by source (WikiCFP, MDPI, etc.)
- **Search**: Full-text search in titles
- **Pagination**: Page-based results
- **Sorting**: Sort by any field

### 3. Statistics
- Total number of paper calls
- Breakdown by type (conferences vs journals)
- Breakdown by source
- Timestamp of the search

### 4. Data Refresh
- Calls FastAPI endpoint to get fresh data
- Deletes old entries for the user and domain
- Saves new entries with user association

## Implementation Details

### Service Layer
- `PaperCallService`: Main business logic
- Calls FastAPI endpoint using RestTemplate
- Manages database operations
- Handles data mapping between DTOs and entities

### Repository Layer
- `PaperCallRepository`: Data access layer
- Custom queries for filtering
- Statistics aggregation queries
- User-specific data access

### Controller Layer
- `PaperCallController`: REST API endpoints
- Authentication integration
- Error handling
- Response formatting

## Usage Examples

### 1. Get all paper calls for a domain
```bash
curl -X GET "http://localhost:8080/api/papercall/calls?domain=machine%20learning" \
  -H "Authorization: Bearer {token}"
```

### 2. Get only conferences
```bash
curl -X GET "http://localhost:8080/api/papercall/conferences?domain=artificial%20intelligence" \
  -H "Authorization: Bearer {token}"
```

### 3. Get statistics
```bash
curl -X GET "http://localhost:8080/api/papercall/statistics?domain=deep%20learning" \
  -H "Authorization: Bearer {token}"
```

### 4. Refresh data
```bash
curl -X POST "http://localhost:8080/api/papercall/refresh?domain=computer%20vision" \
  -H "Authorization: Bearer {token}"
```

## Error Handling

- FastAPI connection errors are caught and logged
- Database errors are handled gracefully
- Authentication errors return appropriate HTTP status codes
- Invalid parameters return 400 Bad Request

## Testing

Run the tests with:
```bash
mvn test -Dtest=PaperCallServiceTest
```

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Security
- RestTemplate (for FastAPI calls)
- PostgreSQL (for data storage)

## Migration

The database migration is automatically applied when the application starts:
- File: `V20250125_003__create_paper_call_table.sql`
- Creates the paper_call table with indexes
- Sets up triggers for updated_at field 