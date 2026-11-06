# Whalet

Controle financeiro pessoal. Backend em Java + Spring Boot, frontend em Angular, banco PostgreSQL (Neon).

## Como rodar

### Backend
Defina as variáveis de ambiente `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` e `GOOGLE_CLIENT_ID`, depois:

    cd backend
    ./mvnw spring-boot:run

### Frontend

    cd frontend
    npm install
    npm start

Acesse http://localhost:4200.

## Branches
- `main`: produção
- `develop`: integração
- `feature/*`: novas funcionalidades