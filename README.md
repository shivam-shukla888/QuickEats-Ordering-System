# QuickEats Food Ordering System

A RESTful backend API for a food ordering platform built with **Spring Boot 3.2**, **Java 17+**, **Spring Security**, and **JPA/Hibernate**. 

It handles user authentication, restaurant and menu management, order processing with item snapshots, and includes full test coverage with Mockito and `@SpringBootTest`.

---

## 🚀 Key Features & Highlights

- **JWT Authentication**: Token-based login (`/api/users/login`) issuing 24-hour Bearer tokens. Protected routes require `Authorization: Bearer <token>`.
- **Clean DTO Architecture**: Request and response payloads are strictly decoupled from JPA entities (`UserResponseDTO`, `RestaurantRequestDTO/ResponseDTO`, `MenuRequestDTO/ResponseDTO`, `OrderResponseDTO`), avoiding password leaks and circular dependencies.
- **Global Exception Handling**: Centralized `@RestControllerAdvice` returning standard JSON error responses (`{ timestamp, status, error, message, path }`) for 404s, validation errors, and bad credentials.
- **Pagination & Sorting**: Endpoints returning lists support Spring Data `Pageable` query parameters (`page`, `size`, `sort`).
- **OpenAPI / Swagger UI**: Interactive API documentation generated automatically via SpringDoc.
- **CORS Support**: Configurable allowed origins (`cors.allowed-origins`) for easy integration with web or mobile frontends (React, Flutter, etc.).
- **In-Memory & Production DB**: Runs out of the box with H2 in-memory DB (auto-seeded with sample data) and easily switches to MySQL.

---

## 🛠️ Tech Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.3 (Spring Web, Spring Data JPA, Spring Security, Spring Validation)
- **Security**: Spring Security + JJWT 0.12.5 (Stateless session handling, BCrypt hashing)
- **Database**: H2 (dev/test) / MySQL 8 (production)
- **API Docs**: SpringDoc OpenAPI 2.5.0
- **Build Tool**: Maven

---

## ⚙️ Running Locally

### Prerequisites
- JDK 17 or higher
- Maven 3.6 or higher

### Step-by-Step
1. **Clone the repository:**
   ```bash
   git clone https://github.com/shivam-shukla888/QuickEats-Ordering-System.git
   cd QuickEats-Ordering-System
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The app starts on `http://localhost:8080`. Sample restaurants and menu items are automatically seeded into the database on startup.

3. **Explore Interactive Docs & H2 Console:**
   - **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
     *(JDBC URL: `jdbc:h2:mem:quickeatsdb`, Username: `sa`, Password: `password`)*

---

## 🧪 Running Tests

Execute unit and integration tests with Maven:

```bash
mvn clean test
```

Tests include:
- **Unit tests** (`UserServiceTest`, `RestaurantServiceTest`, `OrderServiceTest`) using Mockito.
- **Integration tests** (`UserControllerTest`) using `@SpringBootTest` & `MockMvc` covering registration, JWT login, and duplicate email constraints.

---

## 📡 API Overview

### 🔑 Authentication & Users
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/users/register` | Public | Register a new user |
| `POST` | `/api/users/login` | Public | Authenticate & receive JWT token |
| `GET` | `/api/users?page=0&size=10` | Authenticated | Paginated list of users |
| `GET` | `/api/users/{id}` | Authenticated | Get user by ID |
| `PUT` | `/api/users/{id}` | Authenticated | Update user profile |
| `DELETE` | `/api/users/{id}` | Authenticated | Delete user account |

### 🍕 Restaurants & Menus
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/restaurants?page=0&size=10` | Public | Paginated list of restaurants |
| `GET` | `/api/restaurants/{id}` | Public | Get restaurant details |
| `GET` | `/api/restaurants/cuisine/{cuisineType}` | Public | Filter restaurants by cuisine |
| `GET` | `/api/restaurants/search?name={name}` | Public | Search restaurants by name |
| `POST` | `/api/restaurants` | Authenticated | Add a new restaurant |
| `PUT` | `/api/restaurants/{id}` | Authenticated | Update restaurant details |
| `DELETE` | `/api/restaurants/{id}` | Authenticated | Remove restaurant |
| `GET` | `/api/restaurants/{id}/menu` | Public | Get restaurant menu items |
| `POST` | `/api/restaurants/{id}/menu` | Authenticated | Add menu item |
| `PUT` | `/api/restaurants/menu/{menuId}` | Authenticated | Update menu item |
| `DELETE` | `/api/restaurants/menu/{menuId}` | Authenticated | Delete menu item |

### 🛒 Orders
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/orders` | Authenticated | Place an order |
| `GET` | `/api/orders?page=0&size=10` | Authenticated | Paginated list of all orders |
| `GET` | `/api/orders/{id}` | Authenticated | Get order details |
| `GET` | `/api/orders/user/{userId}` | Authenticated | Get orders placed by a user |
| `GET` | `/api/orders/restaurant/{restaurantId}` | Authenticated | Get orders for a restaurant |
| `PUT` | `/api/orders/{id}/status` | Authenticated | Update order status |

---

## 🔒 Configuration

You can customize JWT settings and CORS origins in `src/main/resources/application.properties`:

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=86400000

# CORS Allowed Origins
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,http://localhost:8080,http://localhost:8081}
```

---

## 📜 License

MIT License. Feel free to use and modify for learning or production applications.
