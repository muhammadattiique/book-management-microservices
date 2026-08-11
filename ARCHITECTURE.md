# Book Hub System - System Architecture Documentation

This document outlines the architectural patterns, service boundaries, and communication flows implemented within the Book Hub System.

---

## 1. Microservices vs. Monolith Architecture
* **The Monolith Approach:** A traditional monolithic application bundles all business logic (Auth, Books, Loans, Notifications) into a single deployable artifact. While simpler to start, it suffers from tight coupling, difficult scaling, and high blast radius during failures.
* **The Microservices Approach (Chosen Architecture):** The Book Hub System is decomposed into independent, loosely coupled services. Each service owns its specific business domain, can be scaled independently, and fails in isolation without bringing down the entire ecosystem.

---

## 2. Service Boundaries & Domain Decomposition
The system is divided into clear functional boundaries:
* **API Gateway Service (`gateway-service`):** Acts as the single entry point, handling routing and security cross-cutting concerns.
* **Discovery Service (`eureka-server`):** Acts as the service registry for dynamic service discovery.
* **Auth Service (`auth-service`):** Manages user identity, registration, login, and JWT issuance.
* **Book Service (`book-service`):** Manages inventory, book metadata, authors, and categories.
* **Loans Service (`loans-service`):** Handles book checkout, borrowing states, and returns.
* **Notifications Service (`notifications-service`):** Listens to events and handles user notifications.

---

## 3. Database-Per-Service Pattern
To ensure true loose coupling and prevent database-level tight coupling:
* Each microservice strictly maintains its own isolated database schema or database instance.
* Services **never** share or directly access another service's database.
* Inter-service data sharing is handled strictly via API calls or asynchronous messaging events.

---

## 4. Communication Patterns: Synchronous REST vs. Asynchronous Kafka
* **Synchronous Communication (REST / HTTP):**
    * Used for direct client-to-server requests where an immediate response is required.
    * Example: A user querying book details via `GET /api/v1/books` or logging in via `POST /api/v1/auth/login`. Routed through the API Gateway.
* **Asynchronous Communication (Apache Kafka):**
    * Used for decoupled, event-driven communication where services don't need to block and wait for a response.
    * Example: When a book is successfully borrowed in the `loans-service`, an event is published to a Kafka topic (`book-borrowed-topic`), which the `notifications-service` consumes to trigger a user alert asynchronously.

---

## 5. API Gateway Request Flow
The request lifecycle follows this path:
1. **Client Request:** The client sends an HTTP request to the entry point (e.g., `http://localhost:8080/api/v1/books`).
2. **Gateway Routing:** The **API Gateway (`gateway-service`)** intercepts the request.
3. **Service Discovery Lookup:** The Gateway queries the **Eureka Server (`eureka-server`)** to resolve the physical network address of `book-service`.
4. **Load Balancing & Forwarding:** The Gateway load-balances and forwards the request to the target `book-service` instance using Spring Cloud LoadBalancer (`lb://book-service`).
5. **Response Return:** The Book Service processes the request using its defined `/api/v1` contract and returns the DTO response back through the Gateway to the client.