# MathBot Spring Boot Backend

Backend para MathBot: autenticación JWT, usuarios y chat con Groq.

## Estructura de paquetes

```
src/main/java/com/mathbot/backend/
├── MathBotApplication.java
├── controllers/      # REST (Auth, User, Chat)
├── models/
│   ├── entity/       # Entidades JPA (User, Conversation, Message…)
│   └── dto/          # DTOs de request/response
├── services/         # Lógica de negocio
├── repositories/     # Spring Data JPA
├── middleware/       # Security, JWT, CORS, OpenAPI, excepciones
└── integrations/     # Cliente Groq API
```

## Requisitos

- Java 17+ (desarrollo local)
- Docker (despliegue)
- PostgreSQL
- API key de Groq

## Desarrollo local

Copia `.env.example` a `.env` y ajusta los valores:

- `SPRING_DATASOURCE_*` — PostgreSQL
- `JWT_SECRET` — secreto para tokens
- `GROQ_API_KEY` — clave de la API de Groq
- `CORS_ALLOWED_ORIGINS` — orígenes del frontend

```bash
mvn spring-boot:run
```

## Docker (local)

```bash
docker build -t mathbot-backend .
docker run --rm -p 8080:8080 --env-file .env mathbot-backend
```

## Despliegue en Render (Docker)

1. Sube este repositorio a GitHub.
2. En Render: **New → Blueprint** (si usas `render.yaml`) o **New → Web Service**.
3. Conecta el repo y define **Root Directory**: `backend` (monorepo).
4. Elige **Runtime: Docker**.
5. Configura las variables de entorno:

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario DB |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña DB |
| `JWT_SECRET` | Secreto largo para JWT |
| `GROQ_API_KEY` | API key de Groq |
| `GROQ_MODEL` | Modelo (opcional, default `openai/gpt-oss-20b`) |
| `CORS_ALLOWED_ORIGINS` | URL del frontend desplegado |

6. **Health Check Path**: `/actuator/health`

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/teachers?institution=...`
- `GET /api/users/me`
- `PUT /api/users/me`
- `GET /api/users/dashboard/teacher`
- `POST /api/chat/start`
- `POST /api/chat`
- `GET /api/history/{userId}`
- `GET /api/history/{userId}/{conversationId}`
- `GET /actuator/health`

## Swagger

- Local: `http://localhost:8080/swagger-ui.html`
