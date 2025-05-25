# 🚀 ScholarAI WebSearch Swagger UI Test Guide

## 🎯 What's New

The WebSearch functionality now has a **beautiful Swagger UI** where you can:

- ✅ **Input domain and batch size** directly in the UI
- ✅ **See all returned paper details** in the response
- ✅ **Track search progress** with correlation IDs
- ✅ **View search history** and results

## 🔧 New API Endpoints

### 1. **🔍 POST /api/demo/websearch** - Search Papers

**Input Parameters:**

```json
{
  "queryTerms": ["machine learning", "neural networks"],
  "domain": "Computer Science",
  "batchSize": 10
}
```

**Response:**

```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "correlationId": "corr-123-456",
  "queryTerms": ["machine learning", "neural networks"],
  "domain": "Computer Science",
  "batchSize": 10,
  "status": "SUBMITTED",
  "submittedAt": "2024-01-15T10:30:00",
  "message": "Web search job submitted successfully. Results will be available shortly.",
  "papers": []
}
```

### 2. **📄 GET /api/demo/websearch/{correlationId}** - Get Results

**Response (when completed):**

```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "correlationId": "corr-123-456",
  "status": "COMPLETED",
  "message": "Web search completed successfully! Found 8 papers.",
  "papers": [
    {
      "title": "Deep Learning for Natural Language Processing",
      "doi": "10.1000/182",
      "publicationDate": "2023-05-15",
      "venueName": "Nature Machine Intelligence",
      "publisher": "Nature Publishing Group",
      "peerReviewed": true,
      "authors": [
        {
          "name": "Dr. Jane Smith",
          "affiliation": "MIT Computer Science Department",
          "email": "jane.smith@mit.edu"
        }
      ],
      "citationCount": 142,
      "paperUrl": "https://arxiv.org/abs/2301.12345",
      "source": "Multi-Source"
    }
  ]
}
```

### 3. **📚 GET /api/demo/websearch** - Get All Results

Returns array of all search results.

## 🧪 Testing Steps

### **Step 1: Start Services**

```bash
# Terminal 1: Start FastAPI
cd ScholarAI-Backend-FastAPI
uvicorn app.main:app --reload

# Terminal 2: Start Spring Boot
cd ScholarAI-Backend-Springboot
mvn spring-boot:run
```

### **Step 2: Open Swagger UI**

Navigate to: `http://localhost:8080/docs`

### **Step 3: Test WebSearch**

1. **Click on "🔍 Search Academic Papers"**
2. **Click "Try it out"**
3. **Modify the request body:**
   ```json
   {
     "queryTerms": ["artificial intelligence", "machine learning"],
     "domain": "Computer Science",
     "batchSize": 5
   }
   ```
4. **Click "Execute"**
5. **Copy the `correlationId` from response**

### **Step 4: Check Results**

1. **Click on "📄 Get Search Results"**
2. **Click "Try it out"**
3. **Paste the `correlationId`**
4. **Click "Execute"**
5. **See all paper details!**

## 🎉 Expected Results

You should see:

- ✅ **Detailed paper information** including titles, DOIs, authors, citations
- ✅ **Publication dates and venues**
- ✅ **Source information** (Semantic Scholar, arXiv, etc.)
- ✅ **Real-time status updates** (SUBMITTED → COMPLETED)
- ✅ **Beautiful Swagger UI** with proper documentation

## 🔧 Available Domains

- Computer Science
- Mathematics
- Physics
- Biology
- Chemistry
- Medicine
- Engineering
- Psychology
- Economics

## 📊 Batch Size Limits

- **Minimum:** 1 paper
- **Maximum:** 50 papers
- **Recommended:** 5-15 papers for testing

## 🎯 Features

- **Multi-source search** (4 academic databases)
- **AI-enhanced refinement** (Gemini integration)
- **Smart deduplication**
- **Real-time progress tracking**
- **Comprehensive paper metadata**
- **Beautiful Swagger documentation**
