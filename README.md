# CampusMart

CampusMart is a student marketplace platform built as a monolithic full-stack application with a Spring Boot backend and React frontend.

## Project Structure

```text
campusmart/
  campusmart-backend/
  campusmart-frontend/
  docker-compose.yml
```

## Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- Lombok
- Maven
- Swagger/OpenAPI

## Frontend

- React 19
- Vite
- React Router
- Axios
- Bootstrap 5
- React Toastify
- Context API

## Local Setup

1. Copy environment examples:

```bash
cp campusmart-backend/.env.example campusmart-backend/.env
cp campusmart-frontend/.env.example campusmart-frontend/.env
```

2. Start MySQL:

```bash
docker compose up -d
```

3. Start the backend:

```bash
cd campusmart-backend
mvn spring-boot:run
```

4. Start the frontend:

```bash
cd campusmart-frontend
npm install
npm run dev
```

