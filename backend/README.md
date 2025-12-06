# Certificate Generation Service

A Spring Boot application for managing customers and certificate generation.

## Features

- User authentication and authorization
- User role management (Admin, User)
- Customer CRUD operations
- Certificate generation and management
- Database integration (PostgreSQL)
- RESTful API endpoints

## Technologies

- Java 17+
- Spring Boot
- Maven
- JUnit 5 & Mockito (for testing)

## Setup

1. **Install prerequisites:**
   - Java JDK 17+
   - Maven
   - IntelliJ IDEA
   - Git

2. **Clone the repository:**
    git clone
   https://github.com/Eve-rine/certificate-generation-backend/tree/main/backend
    cd certificate-generation-backend/backend or your preferred directory
3. **Build the project:**
   `mvn clean install`
4. **Run the application:**
   `mvn spring-boot:run`
5. **Access the API:**
   Use tools like Postman or Swagger UI to explore the API endpoints.
    - Base URL: `http://localhost:8080`
- Example endpoints:
    - `GET /customers`
    - `POST /customers`
    - `DELETE /customers/{id}`
    - `POST /certificates/generate`
  
## Testing
**Run Unit Tests:**
   `mvn test`