# 🥗 QuickEats — AI Food Delivery & Recommendation Platform

[![QuickEats CI Pipeline](https://github.com/shivam-shukla888/QuickEats-Ordering-System/actions/workflows/ci.yml/badge.svg)](https://github.com/shivam-shukla888/QuickEats-Ordering-System/actions/workflows/ci.yml)

**QuickEats** is a full-stack, enterprise-grade food ordering and delivery platform built with **Spring Boot 3**, **React 18**, **PostgreSQL / MySQL / H2**, **Spring Security JWT**, and **Groq Llama 3 AI Integration**.

---

## 🚀 Key Platform Features & Architecture

### 1. 🛡️ Enterprise Security & Governance
- **Price Tampering Prevention**: `OrderService.placeOrder()` ignores client-side item prices, performing server-side database price lookup via `MenuRepository` and recalculating `totalAmount`.
- **Privilege Escalation Defense**: `UserService.createUser()` forces `role="CUSTOMER"` on registration. Role updates are strictly isolated to admin-only endpoints protected by `@PreAuthorize("hasRole('ADMIN')")`.
- **IDOR Protection**: Strict ownership checks in `OrderController` and `UserController` enforce that customers can only view, modify, or cancel their own resources.
- **Fail-Fast Startup Security**: `JwtUtil` verifies 256-bit secret length in production profiles, logging clear diagnostic error messages before failing fast.

### 2. ⚡ Real-Time Tracking & Order Metadata
- **End-to-End Order Metadata**: Fully persists and renders structured delivery addresses, landmark details, payment methods, delivery instructions, and rider tips.
- **Live WebSockets & Location Simulation**: Real-time STOMP/SockJS topic broadcast for live rider tracking and status progression (`PENDING` ➔ `PREPARING` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED`).

### 3. 🤖 AI-Powered Features (Groq Llama 3 & RAG)
- **AI Order Support Chatbot (`POST /api/chat/support`)**: Contextually grounded customer support bot using user history and live database state.
- **AI Smart Upsells & Recommendations (`GET /api/recommendations/user/{userId}` & `GET /api/restaurants/{id}/menu`)**: Dynamic AI menu recommendation pipeline with fallbacks to popular menu items.

### 4. 🔑 Silent Re-Authentication & Refresh Token Rotation
- **Token Rotation**: Short-lived in-memory JWT access tokens (15-30 mins) paired with long-lived database-backed refresh tokens (30 days).
- **Session Continuity**: `AuthContext` and `ProtectedRoute` handle silent background token refreshes on page reloads without flashing login redirects or wiping cart state.

---

## 🛠️ Tech Stack

| Layer | Technology | Usage / Purpose |
|---|---|---|
| **Backend Framework** | Java 17, Spring Boot 3.2.3 | Core REST APIs & business logic |
| **Database** | PostgreSQL, MySQL 8, H2 | Relational database persistence |
| **Security & Auth** | Spring Security 6, JJWT 0.12.5 | JWT stateless security & role checks |
| **AI LLM Provider** | Groq API (`llama-3.3-70b-versatile`) | Fast LLM inference via `GroqClientService` |
| **WebSockets** | Spring WebSocket, STOMP, SockJS | Real-time order status & rider tracking |
| **Frontend** | React 18, Vite 8, Tailwind CSS, Axios | Responsive single-page web app |

---

## 🧪 How to Run Automated Tests

### Backend Unit & Integration Tests (71 Tests)
```bash
mvn clean test
```
Includes test suites for:
- `OrderSecurityTest.java`: Ownership checks, 403 Forbidden enforcement, and order cancellation rules.
- `UserSecurityTest.java`: Privilege escalation defenses and IDOR ownership checks.
- `OrderServiceTest.java`: Price tampering prevention, DB price calculation, and order metadata persistence.
- `HealthControllerTest.java` & `DatasourceHealthCheckTest.java`: Health probe and DB fallback checks.

### Frontend Production Build
```bash
cd QuickEats-Frontend
npm run build
```

---

## ⚙️ Local Development Setup

### 1. Environment Configuration (`application.properties`)
```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}
jwt.secret=${JWT_SECRET:your_256_bit_production_secret_here}
groq.api.key=${GROQ_API_KEY:your_groq_api_key}
```

### 2. Launch Backend:
```bash
./run_app.bat
```

### 3. Launch Frontend:
```bash
cd QuickEats-Frontend
npm run dev
```

---

## 📜 License

MIT License. Free to adapt and use.
