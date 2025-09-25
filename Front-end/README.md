# Weekly Report Frontend

A modern React frontend application for managing weekly reports with role-based access control.

## Features

- **Authentication**: Login and registration with JWT tokens
- **Role-based Access**: Different dashboards for Admin, Supervisor, and Employee roles
- **Report Management**: Create, edit, submit, approve, and reject weekly reports
- **User Management**: Admin can manage users and assign supervisors
- **Responsive Design**: Built with Tailwind CSS for modern, mobile-friendly UI
- **Real-time Updates**: Automatic token refresh and error handling

## User Roles

### Employee
- Create and edit weekly reports
- Submit reports for review
- View personal report history
- Dashboard with personal statistics

### Supervisor
- Review submitted reports
- Approve or reject reports with feedback
- View team reports
- Dashboard with team statistics

### Admin
- Full system access
- Manage users (create, edit, delete, enable/disable)
- Assign supervisors to employees
- View all reports and system statistics
- Dashboard with system overview

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- Backend API running on `http://localhost:8080/api`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser and navigate to `http://localhost:5173`

### Backend Connection

The frontend is configured to connect to the Spring Boot backend running on:
- Base URL: `http://localhost:8080/api`
- Authentication: JWT tokens
- CORS: Enabled for all origins

## Project Structure

```
src/
├── components/
│   └── Layout/
│       ├── Header.jsx          # Navigation header
│       ├── Sidebar.jsx         # Role-based sidebar navigation
│       └── Layout.jsx          # Main layout wrapper
├── contexts/
│   └── AuthContext.jsx         # Authentication context
├── pages/
│   ├── auth/
│   │   ├── Login.jsx           # Login page
│   │   └── Register.jsx        # Registration page
│   ├── dashboard/
│   │   ├── AdminDashboard.jsx  # Admin dashboard
│   │   ├── SupervisorDashboard.jsx # Supervisor dashboard
│   │   └── EmployeeDashboard.jsx   # Employee dashboard
│   ├── reports/
│   │   ├── ReportList.jsx      # Reports listing
│   │   ├── CreateReport.jsx    # Create new report
│   │   └── ReportDetail.jsx    # Report details and actions
│   └── admin/
│       └── UserManagement.jsx  # User management (Admin only)
├── services/
│   └── api.js                  # API service layer
├── App.jsx                     # Main app component with routing
└── main.jsx                    # App entry point
```

## API Endpoints

The frontend communicates with these backend endpoints:

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Token refresh

### Reports
- `GET /api/reports` - Get reports (role-based filtering)
- `POST /api/reports` - Create new report
- `GET /api/reports/:id` - Get report by ID
- `PUT /api/reports/:id` - Update report
- `DELETE /api/reports/:id` - Delete report
- `POST /api/reports/:id/submit` - Submit report
- `POST /api/reports/:id/approve` - Approve report
- `POST /api/reports/:id/reject` - Reject report

### Admin
- `GET /api/admin/users` - Get all users
- `POST /api/admin/users` - Create user
- `PUT /api/admin/users/:id` - Update user
- `DELETE /api/admin/users/:id` - Delete user
- `POST /api/admin/users/:id/enable` - Enable user
- `POST /api/admin/users/:id/disable` - Disable user
- `GET /api/admin/dashboard` - Admin dashboard data

## Styling

The application uses Tailwind CSS for styling with:
- Responsive design
- Modern UI components
- Consistent color scheme
- Hover effects and transitions
- Loading states and error handling

## Security Features

- JWT token authentication
- Automatic token refresh
- Role-based route protection
- API request/response interceptors
- Secure token storage in localStorage

## Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

### Environment Variables

No environment variables are required for basic functionality. The API base URL is hardcoded to `http://localhost:8080/api`.

## Deployment

1. Build the application:
```bash
npm run build
```

2. Deploy the `dist` folder to your web server

3. Ensure the backend API is accessible from your frontend domain

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.