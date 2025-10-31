# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.4 backend application for a career planning platform ("bc-backend") built with Java 17. The application provides AI-powered career services including resume parsing, job matching, and chat-based career guidance using RAG (Retrieval-Augmented Generation).

## Development Commands

### Build and Run
- **Build**: `mvn clean compile`
- **Run tests**: `mvn test`
- **Package**: `mvn package`
- **Run application**: `mvn spring-boot:run`
- **Run with Docker**: `docker compose up -d` (starts all dependent services)

### Development Workflow
- The project uses Maven Wrapper (`mvnw`/`mvnw.cmd`)
- Lombok is used for code generation - ensure IDE has Lombok plugin installed
- DevTools is included for automatic restart during development

## Architecture

### Core Technologies
- **Spring Boot 3.5.4** with Spring Security for authentication
- **Spring AI** for AI/ML capabilities (DeepSeek, OpenAI, Ollama models)
- **JPA/Hibernate** with MySQL for relational data
- **MongoDB** for document storage (resume content)
- **Redis** for caching and vector storage
- **RabbitMQ** for asynchronous message processing
- **JWT** for stateless authentication

### Key Components

#### Security & Authentication
- JWT-based authentication with Spring Security
- Role-based access control (Student, Recruiter roles)
- CORS configured for development (localhost, 10.150.*.* networks)
- Public endpoints: registration, login, AI endpoints, jobs, skills

#### AI & RAG System
- **ChatController**: Provides RAG endpoints with vector store and search engine retrieval
- **VectorStoreConfiguration**: Configures Redis-based vector stores for jobs and resumes
- **EmbeddingService**: Handles document embedding and similarity search
- **SearchEngineDocumentRetriever**: Combines vector search with traditional search

#### Data Models
- **User**: Base entity with role-based authentication, manages resumes and skills
- **Resume**: PDF/document storage with parsing status tracking
- **Job**: Job postings with salary, requirements, and skill associations
- **Skill**: Technical/professional skills that connect users and jobs
- **StudentProfile** & **RecruiterProfile**: Role-specific user profiles

#### Message Queue Processing
- **RabbitMQ** for async processing:
  - Resume parsing pipeline
  - Document embedding generation
  - Content extraction workflows

### Database Architecture
- **MySQL**: Primary relational database for users, jobs, skills, resumes metadata
- **MongoDB**: Stores parsed resume content and unstructured data
- **Redis**: Caching, session storage, and vector embeddings

### Service Dependencies
The application requires these services (configured in `compose.yaml`):
- MySQL (3306) - primary database
- MongoDB (27017) - document storage
- Redis (6379) - caching and vector store
- RabbitMQ (5672) - message queue

## Configuration

### Application Properties
Key configuration areas:
- Database connections (MySQL, MongoDB)
- Redis vector store configuration
- AI model endpoints (DeepSeek, OpenAI, Ollama)
- JWT secret and expiration
- OSS (Object Storage Service) for file storage

### Security Configuration
- JWT token validation via `JwtTokenFilter`
- Password encoding with Spring Security's delegating encoder
- CORS configured for development environments
- Public routes defined for authentication endpoints

## Testing
- Unit tests located in `src/test/java/`
- Integration tests for controllers and services
- OSS utility tests for file storage operations
- PDF document reader tests for resume parsing

## Deployment
- Docker containerization with multi-stage build
- GitHub Actions workflow for CI/CD to GitHub Packages
- Health checks via Spring Boot Actuator endpoints
- Service dependencies managed via Docker Compose

## Key Development Notes

- The project uses a hybrid database approach: MySQL for structured data, MongoDB for unstructured content
- AI capabilities are modular and support multiple model providers
- Resume processing is asynchronous via message queues
- Vector embeddings enable semantic search for job matching
- File uploads use OSS (Object Storage Service) for cloud storage