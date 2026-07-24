# 🥗 QuickEats — AI Food Delivery & Recommendation Platform

[![QuickEats CI Pipeline](https://github.com/shivam-shukla888/QuickEats-Ordering-System/actions/workflows/ci.yml/badge.svg)](https://github.com/shivam-shukla888/QuickEats-Ordering-System/actions/workflows/ci.yml)

**QuickEats** is a modern food ordering and delivery backend built with **Spring Boot 3**, **MySQL/H2**, **Spring Security JWT**, and **Groq Llama 3 AI Integration**.

---

## 🤖 AI-Powered Features (Groq Llama 3)

QuickEats integrates direct LLM capabilities via **Groq's OpenAI-compatible Chat Completions API** using a lightweight, resilient REST client (`GroqClientService`) with custom system prompts, contextual database grounding, and graceful fallbacks.

### 1. AI Order Support Chatbot (`POST /api/chat/support`)
- **Purpose**: Provides real-time natural language customer assistance for order status inquiries, order history questions, and general food/menu queries.
- **Model**: Groq Llama 3 (`llama-3.3-70b-versatile` / `llama-3.1-8b-instant`)
- **How It Works**:
  1. Fetches the user's name and recent order history (last 5 orders with status, total amount, order date, and item details) from the database.
  2. Constructs a rich system prompt containing user identity and order context.
  3. Sends user message + context to Groq API via `GroqClientService`.
  4. Returns natural language response. If the Groq API times out or fails, returns a graceful fallback message with latest order status rather than a stack trace.

#### Example Request:
```http
POST /api/chat/support HTTP/1.1
Content-Type: application/json

{
  "userId": 1,
  "message": "Where is my order?"
}
```

#### Example Response:
```json
{
  "response": "Hello Rahul! Your latest order #100 for Butter Chicken & Garlic Naan from Punjab Dhaba is currently IN_TRANSIT and on its way to you."
}
```

---

### 2. AI Personalized Recommendation Engine (`GET /api/recommendations/user/{userId}`)
- **Purpose**: Generates 3 personalized dish recommendations tailored to the user's past ordering habits, preferred cuisines, and average spend.
- **Model**: Groq Llama 3 (`llama-3.3-70b-versatile` / `llama-3.1-8b-instant`)
- **How It Works**:
  1. Analyzes the user's past orders from the database to compute top cuisine preferences, average order value, and past items.
  2. If the user has **no order history**, automatically falls back to top-rated popular menu items directly from the database (non-AI fallback).
  3. If order history exists, fetches all available menu items and prompts Groq Llama 3 to pick 3 menu recommendations with 1-2 sentence personalized explanations.
  4. Parses LLM output into a clean JSON array structure (`[{ "restaurantName": ..., "itemName": ..., "reason": ... }]`).
  5. Fallback strategy ensures a valid 200 OK response even if the LLM call times out or returns malformed data.

#### Example Request:
```http
GET /api/recommendations/user/1 HTTP/1.1
Accept: application/json
```

#### Example Response:
```json
[
  {
    "restaurantName": "Punjab Dhaba",
    "itemName": "Dal Makhani",
    "reason": "Pairs perfectly with your previous North Indian orders of Butter Chicken with rich creamy lentil flavors."
  },
  {
    "restaurantName": "Pind Balluchi",
    "itemName": "Malai Kofta",
    "reason": "Matches your preference for mild creamy North Indian gravies."
  },
  {
    "restaurantName": "Haldiram's Express",
    "itemName": "Chole Bhature",
    "reason": "A top-rated favorite under your average order price point."
  }
]
```

---

## 🔐 Persistent Authentication Architecture

QuickEats uses an enterprise-grade **Access Token (Short-Lived JWT) + Refresh Token (Long-Lived Database-Backed UUID)** authentication architecture:

- **Security Token Rotation**: Access tokens are kept short-lived (15–30 minutes) in-memory to mitigate XSS attacks. Long-lived refresh tokens (30 days) are stored securely in database table `refresh_tokens` and rotated on every use.
- **Silent Re-Authentication**: The React frontend (`AuthContext.jsx` & `axiosInstance.js`) automatically intercepts `401 Unauthorized` responses and performs silent token refresh (`POST /api/auth/refresh`) without disrupting the user session.

---

## 🛠️ Tech Stack & Dependencies

| Layer | Technology | Usage / Purpose |
|---|---|---|
| **Language & Framework** | Java 17, Spring Boot 3.2.3 / 3.3.4 | Core application framework |
| **AI LLM Provider** | Groq API (`llama-3.3-70b-versatile` / `llama-3.1-8b-instant`) | Fast inference LLM via `GroqClientService` |
| **Security & Auth** | Spring Security + JJWT 0.12.5 | Stateless JWT authentication & refresh tokens |
| **Real-Time WebSockets** | Spring WebSocket, STOMP, SockJS | Order status updates & live rider location tracking |
| **Database** | H2 (In-Memory Dev/Test), MySQL 8 | Relational database persistence |
| **Frontend** | React 18, Vite 8, Tailwind CSS, Axios | Responsive single-page web application |

---

## 🧪 How to Test the Endpoints

Run these `curl` commands against the running backend (`http://localhost:8080`):

### 1. Test AI Support Chatbot (`POST /api/chat/support`)
```bash
curl -X POST http://localhost:8080/api/chat/support \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "message": "What did I order last time?"}'
```

### 2. Test AI Recommendations (`GET /api/recommendations/user/{userId}`)
```bash
curl -X GET http://localhost:8080/api/recommendations/user/1 \
  -H "Accept: application/json"
```

---

## ⚙️ Environment Setup & Local Execution

### Prerequisites
- JDK 17 or 21
- Node.js 18+ and npm

### Configuration (`application.properties`)
Set your Groq API key in your environment or `application.properties`:
```properties
groq.api.key=${GROQ_API_KEY:your_groq_api_key_here}
groq.base-url=https://api.groq.com/openai/v1
groq.model-name=llama-3.3-70b-versatile
```

### Running locally:
```cmd
.\run_app.bat
```

---

## 🧪 Automated Testing

Execute all unit & integration tests:

```cmd
"C:\Users\thesh\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd" test
```

Includes unit tests for:
- `ChatSupportControllerTest.java`: Validates `POST /api/chat/support` and fallback on Groq error.
- `AiRecommendationControllerTest.java`: Validates `GET /api/recommendations/user/{userId}` for history-based AI recommendations, empty-history fallbacks, and API failure fallbacks.

---

## 📜 License

MIT License. Free to adapt and use.
