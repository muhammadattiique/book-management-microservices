# Book Hub System - API Contract (`/api/v1`)

This document freezes the REST API contracts, routes, request/response DTOs, and status codes for the microservices within the Book Hub System.

---

## 1. Auth Service (`/api/v1/auth`)

### Register User
* **Method:** `POST`
* **Path:** `/api/v1/auth/register`
* **Request Body (`RegisterRequestDTO`):**
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securePassword123"
  }