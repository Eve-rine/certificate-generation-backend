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

## PostgreSQL Setup

1. **Install PostgreSQL:**
    - Follow the instructions for your OS from the [official PostgreSQL website](https://www.postgresql.org/download/).
2. **Create a database and user:**
   - Open `psql` and run:
     ```
     CREATE DATABASE cert_generation_db;
     CREATE USER cert_user WITH PASSWORD 'yourpassword';
     GRANT ALL PRIVILEGES ON DATABASE cert_generation_db TO cert_user;
     ```

3. **Configure application properties:**
   - In `src/main/resources/application.properties`, add:
     ```
     spring.datasource.url=jdbc:postgresql://localhost:5432/cert_generation_db
     spring.datasource.username=cert_user
     spring.datasource.password=yourpassword
     spring.jpa.hibernate.ddl-auto=update
     spring.jpa.show-sql=true
     spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
     ```

Replace `yourpassword` with a secure password.