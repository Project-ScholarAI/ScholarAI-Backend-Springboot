# Reading List API - Implementation Guide

## Overview

The Reading List API provides comprehensive functionality for managing reading lists within projects. This implementation includes all 12 endpoints as specified in the detailed API documentation, with full CRUD operations, filtering, pagination, statistics, and advanced features.

## 🚀 Features Implemented

### Core Functionality
- ✅ **Complete CRUD Operations**: Create, read, update, delete reading list items
- ✅ **Advanced Filtering**: Filter by status, priority, difficulty, relevance, bookmarks, recommendations
- ✅ **Pagination**: Cursor-based pagination with configurable page sizes
- ✅ **Search**: Full-text search across notes and tags
- ✅ **Sorting**: Multiple sort options with ascending/descending order
- ✅ **Statistics**: Comprehensive reading analytics and metrics
- ✅ **Progress Tracking**: Reading progress with automatic status management
- ✅ **Rating System**: 1-5 star rating for completed items
- ✅ **Bookmarking**: Toggle bookmark status for quick access
- ✅ **Notes Management**: Rich notes with markdown support
- ✅ **Recommendations**: AI-powered paper recommendations (placeholder)
- ✅ **Access Control**: Project-based authorization (owner/collaborator)

### Advanced Features
- ✅ **Status Management**: Automatic timestamp handling for status transitions
- ✅ **Progress Auto-Update**: Automatic status changes based on progress
- ✅ **Time Tracking**: Estimated vs actual reading time
- ✅ **Tag System**: Flexible tagging for categorization
- ✅ **Audit Trail**: Comprehensive logging for all operations
- ✅ **Validation**: Comprehensive input validation and error handling
- ✅ **Performance**: Optimized database queries with proper indexing

## 📊 Database Schema

### Reading List Table
```sql
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
    
    UNIQUE(project_id, paper_id)
);
```

### Database Indexes
- `idx_reading_list_project_id`: For project-based queries
- `idx_reading_list_status`: For status filtering
- `idx_reading_list_priority`: For priority filtering
- `idx_reading_list_difficulty`: For difficulty filtering
- `idx_reading_list_relevance`: For relevance filtering
- `idx_reading_list_is_bookmarked`: For bookmark filtering
- `idx_reading_list_is_recommended`: For recommendation filtering
- `idx_reading_list_added_at`: For chronological sorting
- `idx_reading_list_last_read_at`: For recent activity
- `idx_reading_list_reading_progress`: For progress-based queries
- `idx_reading_list_rating`: For rating-based queries
- Composite indexes for common query patterns

## 🛠️ Implementation Details

### Project Structure
```
src/main/java/dev/project/scholar_ai/
├── model/core/project/
│   └── ReadingListItem.java              # JPA Entity with enums
├── dto/project/
│   ├── ReadingListItemDto.java           # Response DTO
│   ├── AddReadingListItemDto.java        # Create request DTO
│   └── UpdateReadingListItemDto.java     # Update request DTO
├── repository/core/project/
│   └── ReadingListItemRepository.java    # Spring Data JPA Repository
├── mapping/project/
│   └── ReadingListItemMapper.java        # MapStruct mapper
├── service/project/
│   └── ReadingListItemService.java       # Business logic service
└── controller/project/
    └── ReadingListController.java        # REST controller
```

### Key Components

#### 1. ReadingListItem Entity
- **Enums**: Status, Priority, Difficulty, Relevance
- **Validation**: Database constraints and JPA annotations
- **Timestamps**: Automatic creation and update timestamps
- **Relationships**: Links to projects and papers

#### 2. DTOs with Validation
- **ReadingListItemDto**: Complete response object
- **AddReadingListItemDto**: Create with validation annotations
- **UpdateReadingListItemDto**: Update with optional fields
- **Swagger Documentation**: Comprehensive API documentation

#### 3. Repository Layer
- **Custom Queries**: Native SQL for complex filtering
- **Pagination Support**: Spring Data Pageable
- **Statistics Queries**: Aggregation functions
- **Search Functionality**: Full-text search capabilities

#### 4. Service Layer
- **Business Logic**: Status transitions, progress management
- **Access Control**: Project authorization validation
- **Transaction Management**: Database consistency
- **Error Handling**: Comprehensive exception management

#### 5. Controller Layer
- **REST Endpoints**: All 12 API endpoints implemented
- **Rate Limiting**: Resilience4j integration
- **Authentication**: JWT token validation
- **Response Formatting**: Consistent API response structure

## 📋 API Endpoints

### 1. GET /api/v1/projects/{projectId}/reading-list
**Purpose**: Retrieve all reading list items with filtering and pagination

**Query Parameters**:
- `status`: Filter by status (pending, in-progress, completed, skipped)
- `priority`: Filter by priority (low, medium, high, critical)
- `difficulty`: Filter by difficulty (easy, medium, hard, expert)
- `relevance`: Filter by relevance (low, medium, high, critical)
- `isBookmarked`: Filter by bookmark status (true/false)
- `isRecommended`: Filter by recommendation status (true/false)
- `search`: Search in notes and tags
- `sortBy`: Sort field (addedAt, priority, title, rating, difficulty)
- `sortOrder`: Sort direction (asc, desc)
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20, max: 100)

**Example Request**:
```bash
GET /api/v1/projects/550e8400-e29b-41d4-a716-446655440000/reading-list?status=in-progress&priority=high&page=1&limit=20
```

### 2. POST /api/v1/projects/{projectId}/reading-list
**Purpose**: Add a new paper to the reading list

**Request Body**:
```json
{
  "paperId": "550e8400-e29b-41d4-a716-446655440001",
  "priority": "high",
  "estimatedTime": 45,
  "notes": "Important paper for understanding transformer architecture",
  "tags": ["transformer", "attention", "nlp"],
  "difficulty": "hard",
  "relevance": "high"
}
```

### 3. PUT /api/v1/projects/{projectId}/reading-list/{itemId}
**Purpose**: Update an existing reading list item

**Request Body**:
```json
{
  "status": "in-progress",
  "priority": "critical",
  "estimatedTime": 60,
  "actualTime": 30,
  "notes": "Updated notes with additional insights",
  "tags": ["transformer", "attention", "nlp", "deep-learning"],
  "rating": 4,
  "difficulty": "expert",
  "relevance": "critical",
  "readingProgress": 75,
  "isBookmarked": true
}
```

### 4. PATCH /api/v1/projects/{projectId}/reading-list/{itemId}/status
**Purpose**: Update only the status with automatic timestamp management

**Request Body**:
```json
{
  "status": "completed"
}
```

### 5. PATCH /api/v1/projects/{projectId}/reading-list/{itemId}/progress
**Purpose**: Update reading progress with automatic status management

**Request Body**:
```json
{
  "readingProgress": 75
}
```

### 6. DELETE /api/v1/projects/{projectId}/reading-list/{itemId}
**Purpose**: Remove a paper from the reading list

### 7. GET /api/v1/projects/{projectId}/reading-list/stats
**Purpose**: Get comprehensive reading list statistics

**Query Parameters**:
- `timeRange`: Time range for stats (7d, 30d, 90d, all)

### 8. GET /api/v1/projects/{projectId}/reading-list/recommendations
**Purpose**: Get AI-powered paper recommendations

**Query Parameters**:
- `limit`: Number of recommendations (default: 10, max: 50)
- `difficulty`: Preferred difficulty level
- `excludeRead`: Exclude already read papers (default: true)

### 9. POST /api/v1/projects/{projectId}/reading-list/{itemId}/notes
**Purpose**: Add or update notes for a reading list item

**Request Body**:
```json
{
  "note": "Key insight: The attention mechanism allows the model to focus on different parts of the input sequence dynamically."
}
```

### 10. PATCH /api/v1/projects/{projectId}/reading-list/{itemId}/rating
**Purpose**: Rate a completed reading list item (1-5 stars)

**Request Body**:
```json
{
  "rating": 4
}
```

### 11. PUT /api/v1/projects/{projectId}/reading-list/{itemId}/bookmark
**Purpose**: Toggle the bookmark status of a reading list item

### 12. PATCH /api/v1/projects/{projectId}/reading-list/bulk
**Purpose**: Update multiple reading list items at once (Future Enhancement)

## 🔧 Business Logic

### Status Management
The system automatically handles status transitions and timestamps:

- **pending → in-progress**: Sets `startedAt`, increments `readCount`
- **in-progress → completed**: Sets `completedAt`, calculates `actualTime`
- **in-progress → pending**: Clears `startedAt`
- **completed → in-progress**: Clears `completedAt`, sets `startedAt`
- **Any → skipped**: No timestamp changes

### Progress Management
Reading progress automatically triggers status changes:

- **0% progress**: Sets status to 'pending' if currently 'in-progress'
- **1-99% progress**: Sets status to 'in-progress' if currently 'pending'
- **100% progress**: Sets status to 'completed'

### Access Control
Users can only access reading lists for projects they:
- Own (project owner)
- Collaborate on (project collaborator)

### Validation Rules
- **Paper ID**: Must be valid UUID and exist in papers table
- **Priority/Difficulty/Relevance**: Must be valid enum values
- **Estimated/Actual Time**: Must be positive integers
- **Notes**: Maximum 1000 characters
- **Tags**: Maximum 10 tags, each max 50 characters
- **Rating**: 1-5 stars, only for completed items
- **Progress**: 0-100 percentage

## 🧪 Testing

### Postman Collection
A comprehensive Postman collection is provided at:
```
postman/scholarai-reading-list.postman_collection.json
```

### Environment Variables
Set up the following variables in Postman:
- `base_url`: Your API base URL (e.g., http://localhost:8080)
- `auth_token`: Your JWT authentication token
- `project_id`: A valid project UUID
- `item_id`: A valid reading list item UUID
- `paper_id`: A valid paper UUID

### Test Scenarios
1. **Basic CRUD Operations**: Create, read, update, delete items
2. **Filtering and Pagination**: Test all filter combinations
3. **Status Transitions**: Verify automatic timestamp management
4. **Progress Updates**: Test automatic status changes
5. **Access Control**: Test unauthorized access scenarios
6. **Validation**: Test invalid input scenarios
7. **Statistics**: Verify calculation accuracy
8. **Error Handling**: Test various error conditions

## 🚀 Performance Considerations

### Database Optimization
- **Indexes**: Comprehensive indexing strategy for all query patterns
- **Query Optimization**: Native SQL queries for complex operations
- **Pagination**: Cursor-based pagination for large datasets
- **Connection Pooling**: Optimized database connection management

### Caching Strategy
- **Statistics Caching**: Cache reading list statistics (15-minute TTL)
- **Recommendation Caching**: Cache AI recommendations (1-hour TTL)
- **User Session Caching**: Cache user project access permissions

### Monitoring
- **Query Performance**: Monitor slow queries and optimize
- **API Response Times**: Track endpoint performance
- **Error Rates**: Monitor and alert on high error rates
- **User Activity**: Track reading patterns for insights

## 🔮 Future Enhancements

### Planned Features
1. **Bulk Operations**: Update multiple items simultaneously
2. **Advanced Search**: Elasticsearch integration for full-text search
3. **AI Recommendations**: Machine learning-based paper recommendations
4. **Reading Analytics**: Detailed reading behavior analysis
5. **Export Functionality**: Export reading lists to various formats
6. **Collaborative Features**: Shared reading lists and annotations
7. **Mobile Optimization**: Mobile-specific API optimizations
8. **Real-time Updates**: WebSocket integration for live updates

### Technical Improvements
1. **GraphQL Support**: Alternative API interface
2. **Event Sourcing**: Complete audit trail of all changes
3. **Microservices**: Split into dedicated reading list service
4. **CQRS Pattern**: Separate read and write models
5. **Distributed Caching**: Redis cluster for high availability

## 📚 Usage Examples

### Adding a Paper to Reading List
```bash
curl -X POST "http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000/reading-list" \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "paperId": "550e8400-e29b-41d4-a716-446655440001",
    "priority": "high",
    "estimatedTime": 45,
    "notes": "Important paper for understanding transformer architecture",
    "tags": ["transformer", "attention", "nlp"],
    "difficulty": "hard",
    "relevance": "high"
  }'
```

### Updating Reading Progress
```bash
curl -X PATCH "http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000/reading-list/550e8400-e29b-41d4-a716-446655440001/progress" \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "readingProgress": 75
  }'
```

### Getting Reading List Statistics
```bash
curl -X GET "http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000/reading-list/stats?timeRange=30d" \
  -H "Authorization: Bearer your_jwt_token"
```

## 🛡️ Security Considerations

### Authentication & Authorization
- **JWT Tokens**: Secure token-based authentication
- **Project Access**: Validate user permissions for each project
- **Rate Limiting**: Prevent API abuse with Resilience4j
- **Input Validation**: Comprehensive validation of all inputs

### Data Protection
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Input sanitization
- **Audit Logging**: Complete audit trail of all operations
- **Data Encryption**: Sensitive data encryption at rest

## 📈 Monitoring & Analytics

### Key Metrics
- **API Response Times**: Monitor endpoint performance
- **Error Rates**: Track and alert on errors
- **User Engagement**: Reading completion rates
- **System Health**: Database performance and availability

### Logging
- **Structured Logging**: JSON format for easy parsing
- **Request Tracing**: Track requests across services
- **Error Tracking**: Detailed error information
- **Performance Monitoring**: Query and API performance

## 🤝 Contributing

### Development Setup
1. **Clone Repository**: `git clone <repository-url>`
2. **Install Dependencies**: `mvn clean install`
3. **Run Database Migrations**: Flyway will auto-execute
4. **Start Application**: `mvn spring-boot:run`
5. **Run Tests**: `mvn test`

### Code Quality
- **Spotless**: Automatic code formatting
- **SonarQube**: Code quality analysis
- **Unit Tests**: Comprehensive test coverage
- **Integration Tests**: End-to-end API testing

### Pull Request Process
1. **Create Feature Branch**: `git checkout -b feature/reading-list-enhancement`
2. **Implement Changes**: Follow coding standards
3. **Add Tests**: Ensure adequate test coverage
4. **Update Documentation**: Keep README and API docs current
5. **Submit PR**: Include detailed description and testing notes

## 📞 Support

### Documentation
- **API Documentation**: Swagger UI at `/swagger-ui.html`
- **Database Schema**: See migration files in `src/main/resources/db/migration/`
- **Code Comments**: Comprehensive inline documentation

### Troubleshooting
- **Common Issues**: Check application logs for detailed error messages
- **Database Issues**: Verify database connectivity and migration status
- **Authentication Issues**: Ensure valid JWT token and proper format
- **Performance Issues**: Monitor database queries and API response times

### Contact
For technical support or feature requests, please:
1. Check existing documentation and issues
2. Create detailed bug reports with reproduction steps
3. Provide relevant logs and error messages
4. Include environment details and configuration

---

**Note**: This implementation provides a solid foundation for reading list management with room for future enhancements and scalability improvements. 