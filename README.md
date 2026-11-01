# SimuLab — Interactive Distributed System Design & Simulation Platform

[![Java 21/25](https://img.shields.io/badge/Java-21%2B%20%7C%2025-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8.x-purple.svg)](https://vitejs.dev/)
[![React Flow](https://img.shields.io/badge/@xyflow/react-12.x-pink.svg)](https://reactflow.dev/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-4.x-cyan.svg)](https://tailwindcss.com/)

> **SimuLab** is an interactive distributed system design and simulation platform where software architects, system designers, and backend engineers visually design distributed architectures and run a genuine discrete-event simulation engine in Java to observe bottlenecks, analyze latency/throughput, inject failures, and compare architectures under identical workloads.

---

## 🌟 Key Features

1. **Visual Drag-and-Drop Architecture Canvas**
   - Built on React Flow (`@xyflow/react`) with custom dark-themed developer tool cards.
   - Interactive handles on all four sides for flexible topology routing.
   - Animated traffic pulse edges that visualize data flowing across services.

2. **Genuine Discrete-Event Simulation Engine (Java 21/25)**
   - Priority queue event loop sorting events by fractional-millisecond timestamps.
   - Deterministic execution: identical workloads and seeds yield 100% reproducible results.
   - Realistic stochastic distributions: Constant intervals, Poisson process, and bursty traffic peaks.

3. **8 Modeled Infrastructure Components**
   - **Client Workload Generator**: Configurable RPS, Poisson/Constant/Burst distribution, timeouts.
   - **Load Balancer**: Round-Robin, Least-Connections, and Random balancing algorithms.
   - **API Server**: Horizontal replicas, thread pool limits, processing time, concurrency tracking.
   - **In-Memory Cache (Redis)**: Configurable hit/miss ratio, sub-millisecond lookups, miss forwarding.
   - **Relational/NoSQL Database**: Connection pool sizing, query execution latencies, read/write split ratios.
   - **Message Queue (Kafka/RabbitMQ)**: Buffer capacities, enqueue latencies, backpressure handling.
   - **Worker Pool**: Asynchronous consumers with configurable concurrency draining message queues.
   - **Object Storage (S3)**: Upload/download latency and bandwidth constraints.

4. **Automated Bottleneck Detector & Architectural Advisory**
   - Evaluates component utilization, queue contention, connection pool saturation, and dropped requests.
   - Generates actionable, prescriptive recommendations (e.g., *"Database connection pool saturated at 100% causing 420ms queue wait. Add a Redis Cache in front of the database with an estimated 80% hit ratio to reduce load"*).

5. **Head-to-Head Architecture Comparison Engine**
   - Runs two architectures under identical workloads and seeds.
   - Computes throughput delta (`+X%`), P95 latency delta (`-X% faster`), and error rate delta.
   - Determines the winning architecture and outputs a structured tradeoff analysis (resilience, scalability, performance).

6. **Chaos Engineering & Failure Injection**
   - Node crashes & outages.
   - Latency spikes (e.g., +350ms network degradation).
   - Network partitions & link cuts.
   - Capacity reductions.

---

## 🏗️ Architecture & Component Flow

```
                      ┌──────────────────────────────────────────┐
                      │            SimuLab Frontend              │
                      │  (React 19 + TypeScript + React Flow)    │
                      └────────────────────┬─────────────────────┘
                                           │ REST API / JSON
                                           ▼
                      ┌──────────────────────────────────────────┐
                      │          Spring Boot 3 Backend           │
                      │ ┌──────────────────────────────────────┐ │
                      │ │   REST Controllers & Repositories    │ │
                      │ └──────────────────┬───────────────────┘ │
                      │                    │                     │
                      │ ┌──────────────────▼───────────────────┐ │
                      │ │    Discrete-Event Simulation Engine  │ │
                      │ │  • Event Priority Queue (Timestamp)  │ │
                      │ │  • Request State Machine             │ │
                      │ │  • 8 Component Simulators            │ │
                      │ │  • Metrics & Percentile Calculator   │ │
                      │ │  • Bottleneck & Advisory Engine      │ │
                      │ └──────────────────────────────────────┘ │
                      └────────────────────┬─────────────────────┘
                                           │
                                           ▼
                                 ┌──────────────────┐
                                 │ PostgreSQL / H2  │
                                 └──────────────────┘
```

---

## 🚀 Quickstart Guide

### Prerequisites
- **Java 21** or higher (tested with Java 21 & Java 25)
- **Node.js 20+** and `npm`
- **Docker & Docker Compose** (optional, for containerized run)

---

### Option 1: Run Locally (Fastest Development Mode)

#### 1. Start the Spring Boot Backend
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run

# Linux / macOS:
./mvnw spring-boot:run
```
The backend starts on `http://localhost:8080`.
- Swagger UI / OpenAPI docs: `http://localhost:8080/swagger-ui.html`
- H2 In-Memory Console: `http://localhost:8080/h2-console`

#### 2. Start the Frontend
In a new terminal:
```bash
cd frontend
npm install
npm run dev
```
The application opens at `http://localhost:5173`.

---

### Option 2: Run with Docker Compose

Deploy the entire stack (PostgreSQL, Backend, Frontend) with a single command:
```bash
docker-compose up --build
```
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

---

## 🧪 Running Automated Tests

The repository includes a comprehensive JUnit 5 test suite verifying simulation correctness, bottleneck identification, failure injection, and comparison accuracy:

```bash
cd backend
.\mvnw.cmd test
```

Frontend compilation and bundle verification:
```bash
cd frontend
npm run build
```

---

## 🎯 Pre-Seeded Architecture Templates

SimuLab comes with three battle-tested architecture templates ready to simulate with a single click:

1. **Classic Monolith Architecture**
   - `Web Client` ➔ `API Gateway` ➔ `Monolith App Server` ➔ `PostgreSQL Primary DB`.
   - Demonstrates connection pool exhaustion and single-point-of-failure bottlenecks under high RPS.

2. **Microservices with Redis Cache**
   - `Clients` ➔ `HAProxy Load Balancer` ➔ `2x API Services` ➔ `Redis Cache (85% Hit)` ➔ `Postgres DB`.
   - Demonstrates how horizontal scaling and in-memory caching reduce database load by 80%+ and lower P95 latency.

3. **Event-Driven Async Queue & Workers**
   - `Ingestion Traffic` ➔ `Ingest API` ➔ `Apache Kafka Broker` ➔ `Async Worker Pool` ➔ `S3 Storage & TimescaleDB`.
   - Demonstrates absorbing spiky burst traffic through message queue buffers without dropping incoming client requests.

---

## 📄 License
MIT License. Built for distributed systems engineers and system design practitioners.
