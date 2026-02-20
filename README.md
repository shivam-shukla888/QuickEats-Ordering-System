# QuickEats Food Ordering System

A comprehensive backend-driven food ordering platform built with Java, Spring Boot, Hibernate, and MySQL.

## Features

- **User Management**: User registration, authentication, and profile management
- **Restaurant Management**: Browse restaurants by cuisine type, search by name
- **Menu Management**: View restaurant menus with detailed item information
- **Order Management**: Place orders, track order status, view order history
- **Admin Features**: Restaurant and menu management for restaurant owners

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.3
- **Database**: MySQL (with H2 for development/testing)
- **ORM**: Hibernate/JPA
- **Security**: Spring Security with BCrypt password encoding
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Maven

## API Endpoints

### User Management
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - User login
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Restaurant Management
- `POST /api/restaurants` - Create a new restaurant
- `GET /api/restaurants` - Get all restaurants
- `GET /api/restaurants/{id}` - Get restaurant by ID
- `GET /api/restaurants/cuisine/{cuisineType}` - Get restaurants by cuisine type
- `GET /api/restaurants/search?name={name}` - Search restaurants by name
- `GET /api/restaurants/cuisines` - Get all cuisine types
- `PUT /api/restaurants/{id}` - Update restaurant
- `DELETE /api/restaurants/{id}` - Delete restaurant

### Menu Management
- `POST /api/restaurants/{id}/menu` - Add menu item to restaurant
- `GET /api/restaurants/{id}/menu` - Get restaurant menu
- `PUT /api/restaurants/menu/{menuId}` - Update menu item
- `DELETE /api/restaurants/menu/{menuId}` - Delete menu item

### Order Management
- `POST /api/orders` - Place a new order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get orders by user
- `GET /api/orders/restaurant/{restaurantId}` - Get orders by restaurant
- `GET /api/orders/user/{userId}/status/{status}` - Get user orders by status
- `GET /api/orders/restaurant/{restaurantId}/status/{status}` - Get restaurant orders by status
- `GET /api/orders/between-dates?startDate={}&endDate={}` - Get orders between dates
- `GET /api/orders/status/{status}/count` - Get order count by status
- `PUT /api/orders/{id}/status` - Update order status

## Database Schema

### Users Table
- id (PK, Auto-increment)
- name (VARCHAR, NOT NULL)
- email (VARCHAR, UNIQUE, NOT NULL)
- password (VARCHAR, NOT NULL, encrypted)
- role (VARCHAR, NOT NULL, DEFAULT 'CUSTOMER')

### Restaurants Table
- id (PK, Auto-increment)
- name (VARCHAR, NOT NULL)
- address (VARCHAR, NOT NULL)
- cuisine_type (VARCHAR, NOT NULL)

### Menus Table
- id (PK, Auto-increment)
- item_name (VARCHAR, NOT NULL)
- price (DOUBLE, NOT NULL)
- description (TEXT)
- restaurant_id (FK to restaurants)

### Orders Table
- id (PK, Auto-increment)
- user_id (FK to users, NOT NULL)
- restaurant_id (FK to restaurants, NOT NULL)
- status (VARCHAR, NOT NULL)
- total_amount (DOUBLE, NOT NULL)
- order_time (DATETIME, NOT NULL)
- order_items (JSON)

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Database Setup
1. Create a MySQL database named `quickeats_db`
2. Update database credentials in `application.properties` if needed
3. The application will automatically create tables on startup

### Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. The application will start on `http://localhost:8080`

### Using H2 Database (for development)
To use H2 instead of MySQL, comment out the MySQL configuration and uncomment the H2 configuration in `application.properties`.

## Sample Data

The application automatically seeds sample data on first startup:
- 5 restaurants with different cuisine types (American, Italian, Indian, Chinese, Japanese)
- Menu items for each restaurant

## Security

- Passwords are encrypted using BCrypt
- Spring Security handles authentication
- API endpoints are configured for public access (can be customized)

## Error Handling

The application provides comprehensive error handling with appropriate HTTP status codes and error messages.

## Future Enhancements

- JWT-based authentication
- Role-based access control
- Payment integration
- Real-time order tracking
- Email notifications
- Image upload for menu items
