# Weekly Report Management System

A comprehensive Spring Boot backend system for managing weekly reports, user accounts, and organizational hierarchy with role-based access control.

## Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based access control (Admin, Supervisor, Employee)
- Secure password encoding with BCrypt
- Token refresh functionality

### 👥 User Management
- Multi-role user system (Admin, Supervisor, Employee)
- User profile management with inheritance
- User enable/disable functionality
- Supervisor-employee assignment

### 📊 Report Management
- Weekly report creation and submission
- Report approval/rejection workflow
- Report status tracking (Draft, Submitted, Approved, Rejected)
- Supervisor feedback system
- Report filtering and search

### 🛡️ Security Features
- CORS configuration
- Input validation
- Exception handling
- SQL injection protection
- XSS protection

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: H2 (Development), MySQL (Production)
- **Security**: Spring Security with JWT
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Java Version**: 21

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Token refresh

### Reports
- `GET /api/reports` - Get reports (role-based filtering)
- `POST /api/reports` - Create new report
- `PUT /api/reports/{id}` - Update report
- `GET /api/reports/{id}` - Get specific report
- `DELETE /api/reports/{id}` - Delete report
- `POST /api/reports/{id}/submit` - Submit report
- `POST /api/reports/{id}/approve` - Approve report
- `POST /api/reports/{id}/reject` - Reject report

### Admin Management
- `GET /api/admin/users` - Get all users
- `POST /api/admin/users` - Create user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user
- `POST /api/admin/users/{id}/enable` - Enable user
- `POST /api/admin/users/{id}/disable` - Disable user
- `GET /api/admin/dashboard` - Admin dashboard

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd weekly-report
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - H2 Console: `http://localhost:8080/api/h2-console`

### Default Credentials

The system initializes with sample data:

- **Admin**: `admin` / `admin123`
- **Supervisor**: `supervisor` / `super123`
- **Employee**: `employee1` / `emp123`, `employee2` / `emp123`, `employee3` / `emp123`

## Database Schema

### User Management
- `user_accounts` - User authentication data
- `base_profiles` - Common profile information
- `admin_profiles` - Admin-specific data
- `supervisor_profiles` - Supervisor-specific data
- `employee_profiles` - Employee-specific data

### Report Management
- `weekly_reports` - Weekly report data
- Report status workflow: Draft → Submitted → Approved/Rejected

## Configuration

### Database Configuration
The application uses H2 in-memory database for development. To use MySQL in production:

1. Uncomment MySQL configuration in `application.properties`
2. Update database connection details
3. Change Hibernate dialect to `MySQL8Dialect`

### JWT Configuration
- Secret key: Configured in `application.properties`
- Token expiration: 24 hours (86400000 ms)

## API Documentation

The API is fully documented using Swagger/OpenAPI 3. Access the interactive documentation at:
`http://localhost:8080/api/swagger-ui.html`

## Security

### Authentication Flow
1. User provides credentials via `/api/auth/login`
2. System validates credentials and returns JWT token
3. Client includes token in Authorization header: `Bearer <token>`
4. System validates token for protected endpoints

### Role-Based Access Control
- **Admin**: Full system access
- **Supervisor**: Can view and approve reports from assigned employees
- **Employee**: Can create, edit, and submit their own reports

## Error Handling

The system provides comprehensive error handling:
- Validation errors with field-specific messages
- Authentication and authorization errors
- Resource not found errors
- Business logic errors

## Development

### Project Structure
```
src/main/java/com/example/weekly_report/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── exception/      # Exception handling
├── repository/     # Data access layer
├── security/       # Security configuration
└── service/        # Business logic
```

### Adding New Features
1. Create entity classes in `entity/` package
2. Create repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Add DTOs for request/response in `dto/` package

## Testing

Run tests using Maven:
```bash
mvn test
```

## Production Deployment

1. **Database Setup**
   - Configure MySQL database
   - Update connection properties
   - Run database migrations

2. **Security Configuration**
   - Change JWT secret key
   - Configure CORS for production domains
   - Enable HTTPS

3. **Environment Variables**
   - Set production database credentials
   - Configure JWT secret
   - Set server port and context path

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please contact the development team at dev@company.com.
