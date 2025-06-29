# CLAUDE.md

# システムデザイン面接システム

## 概要

- 候補者はあるシステムのデザインについて、自分ならどのように設計するか問われる
- 面接官が課題を出し、候補者は回答していく
- 面接官は候補者の回答をある程度深堀りしていく
- どちらかといえば機能要件よりも非機能要件を重視する。スケーリング戦略（オートスケール、キューイング、パーティション、シャーディング…）、キャッシュ戦略、可用性など
- 最後にフィードバックが行われる

## 機能

- 面接を開始する機能
- 面接で回答する機能
- 面接を終了する機能（ある程度で自動で終了もする）

## 希望

- 面接官役は、LLMで実現する。AWS Bedrockを使いたい。
- 質問の骨子はどっかにデータとしてもっておく。深堀りはLLMに勝手に考えてもらう

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a todo application built with a polyglot microservices architecture:

- **Frontend**: Web UI (planned - directory currently empty)
- **Java Backend**: RESTful API service (planned - directory currently empty) 
- **Rust Backend**: High-performance service (planned - directory currently empty)
- **Docker Compose**: Service orchestration (file exists but empty)

## Current State

The project has been implemented with:
- ✅ React frontend with TypeScript (runs on host OS)
- ✅ Rust backend API with CRUD operations (runs in Docker)
- ✅ Docker configuration for backend only

## Architecture Design

The intended architecture follows a microservices pattern:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   Frontend  │───▶│ Java Backend │───▶│   Database  │
│  (Web UI)   │    │  (REST API)  │    │             │
└─────────────┘    └──────────────┘    └─────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │Rust Backend │
                   │(Performance)│
                   └─────────────┘
```

## Development Setup

### Initial Project Setup
Since services are not yet implemented, initialize each component:

**Frontend:**
```bash
cd frontend/
# Initialize with your preferred framework:
npx create-react-app . --template typescript
# or
npm create vue@latest .
# or
npx create-next-app@latest .
```

**Java Backend:**
```bash
cd java/
# Initialize Spring Boot project:
curl https://start.spring.io/starter.tgz \
  -d dependencies=web,data-jpa,h2 \
  -d javaVersion=17 \
  -d type=maven-project \
  -d groupId=com.example \
  -d artifactId=todo-api | tar -xzf -
```

**Rust Backend:**
```bash
cd rust/
cargo init --name todo-rust
```

### Service Development Commands

**Frontend** (React with TypeScript):
```bash
cd frontend/
npm install          # Install dependencies
npm start            # Development server (http://localhost:2009)
npm run build        # Production build
npm test             # Run tests
npm run lint         # Lint code (if configured)
```

**Rust Backend** (via Docker):
```bash
# Build and run via Docker Compose
docker-compose up --build    # Build and start backend
docker-compose down          # Stop backend

# For development with local Rust installation:
cd rust/
cargo run                    # Run development server (http://localhost:8000)
cargo test                  # Run tests
cargo build --release       # Production build
cargo fmt                   # Format code
cargo clippy                # Lint code
```

### Docker Commands

```bash
docker-compose up -d              # Start all services
docker-compose down               # Stop all services
docker-compose logs <service>     # View service logs
docker-compose build              # Rebuild containers
```

## Service Communication

Services should communicate via:
- **Frontend ↔ Java Backend**: HTTP REST API
- **Java Backend ↔ Rust Backend**: HTTP API or message queue
- **Backends ↔ Database**: Database drivers/ORMs

## Testing Strategy

Each service should implement:
- **Frontend**: Component tests (Jest/Vitest), E2E tests (Cypress/Playwright)
- **Java**: Unit tests (JUnit), Integration tests (TestContainers)
- **Rust**: Unit tests (`cargo test`), Integration tests

## Configuration

Environment-specific configuration should be managed via:
- Frontend: `.env` files
- Java: `application-{profile}.yml`
- Rust: Environment variables or config files
- Docker: `docker-compose.override.yml` for local development