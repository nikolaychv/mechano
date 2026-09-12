## About the Project

Mechano is a backend platform for managing vehicle repair services, built with Java 21 and Spring Boot.

The application allows users to browse repair shops, create bookings, leave reviews, manage favorites, and upload related images. Repair shop owners can manage their own shops and bookings, while administrators have broader management permissions.

The project uses PostgreSQL with Liquibase for database migrations and JWT-based authentication through a separate authentication service. The backend follows a layered architecture with controllers, services, repositories, DTOs, and role-based authorization.

### Main Features

- User profiles linked to a separate authentication service
- JWT-based authentication and role-based authorization
- Repair shop management
- Booking management
- Reviews and ratings
- Favorites
- Image upload and media storage
- Soft delete support
- PostgreSQL database
- Liquibase database migrations
- Swagger / OpenAPI documentation
- JUnit and Mockito tests
