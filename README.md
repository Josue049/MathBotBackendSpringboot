# MathBot Spring Boot Backend

Spring Boot backend for user registration, login, JWT auth, and chat proxying to the FastAPI AI service.

## Requirements

- Java 17+
- PostgreSQL
- FastAPI service running on `http://127.0.0.1:8000`

## Environment variables

- `SPRING_DATASOURCE_URL` = `jdbc:postgresql://localhost:5432/mathbot`
- `SPRING_DATASOURCE_USERNAME` = your PostgreSQL user
- `SPRING_DATASOURCE_PASSWORD` = your PostgreSQL password
- `JWT_SECRET` = a long secret key
- `FASTAPI_CHAT_URL` = `http://127.0.0.1:8000/api/chat`
- `CORS_ALLOWED_ORIGINS` = `http://localhost:5173,http://127.0.0.1:5173`

## Run

```bash
mvn spring-boot:run
```

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `POST /api/chat`

## Swagger

Open the API docs at:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`
