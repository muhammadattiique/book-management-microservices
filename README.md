# 📚 Book Hub System - Event-Driven Microservices Architecture

A robust, production-ready microservices-based backend for an online bookstore and library management system. Built using **Spring Boot**, **Spring Cloud (Eureka & API Gateway)**, **Apache Kafka** for asynchronous event-driven messaging, **Spring Security (JWT)**, and **MySQL**.

---

## 🏛️ Architecture Overview

The system is split into decoupled, containerized microservices communicating via REST APIs and asynchronous Kafka message events:

              ┌───────────────────────┐
              │      API Gateway      │ (Port 8080)
              └──────────┬────────────┘
                         │
┌─────────────────────┼─────────────────────┐
│                     │                     │
▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Auth Service │      │ Book Service │      │ Loan Service │
│  (Port 8081) │      │  (Port 8083) │      │  (Port 8082) │
└──────────────┘      └──────────────┘      └──────┬───────┘
│
(Kafka Events)
│
┌─────────────────────┘
▼
┌──────────────┐      ┌──────────────────────┐
│  Inventory   │      │     Notification     │
│   Service    │      │       Service        │
└──────────────┘      └──────────────────────┘
▲
└────── [Eureka Discovery Server: 8761]
└────── [Apache Kafka Broker: 9092]


### Core Services
1. **Eureka Server (Port 8761):** Centralized service discovery registry.
2. **API Gateway (Port 8080):** Single entry point routing requests to downstream microservices and handling security filtering.
3. **Auth Service (Port 8081):** Manages user registration, authentication, and JWT token issuance with role-based access control (`ROLE_MEMBER`, `ROLE_STUDENT`, `ROLE_ADMIN`, `ROLE_LIBRARIAN`).
4. **Book Service (Port 8083):** Manages the book catalog, details, and inventory lookups.
5. **Loan Service (Port 8082):** Handles book borrowings, validation rules, and publishes `LoanCreatedEvent` messages to Kafka.
6. **Inventory Service:** Tracks stock levels and reacts asynchronously to Kafka event streams.
7. **Notification Service:** Listens to event streams and generates user notifications upon loan activities.

---

## 🛠️ Prerequisites

Ensure the following tools are installed on your system:
* **Docker & Docker Compose** (Required for containerized infrastructure and services)
* **Java 17+** (Optional, if running services locally)
* **Maven 3.8+** (Optional, for building project artifacts)

---

## 🚀 Setup & Run Instructions

### 1. Clone the Repository
```bash
git clone [https://github.com/your-username/book-hub-system.git](https://github.com/your-username/book-hub-system.git)
cd book-hub-system