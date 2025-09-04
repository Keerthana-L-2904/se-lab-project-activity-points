This is a Spring Boot application for managing student activity points in an educational institution.
## Key Features
- **User Management**: Separate roles for Students, Faculty Advisors (FAs), and Admins
- **Activity Tracking**: Record student participation in institutional/departmental activities
- **Points System**: Automatic calculation of department and institutional points
- **Approval Workflow**: FA approval process for student activity submissions
- **Dashboard**: Admin dashboard with system statistics

## Database Schema
Key entities:
- **Student**: Tracks student information and points
- **Activity**: Stores activity details (points, type, dates)
- **StudentActivity**: Junction table for student participation
- **Requests**: Student activity approval requests
- **Validation**: Attendance validation records
- **Fa**: Faculty advisor information
- **Admin**: Administrator accounts

## Setup Instructions

### Prerequisites
- Java 21
- Maven 3.8.11
- MySQL database-8.0.32
- springboot-3.2.2

### Installation
1. Clone the repository
2. Configure database connection in `application.properties`
3. Run:
   ```
   mvn spring-boot:run
   ```
## Testing
The application includes unit tests for services and integration tests for controllers. Run tests with:
```
mvn test
```
## Technologies Used
- **Backend**: Spring Boot, Spring Data JPA
- **Database**: MySQL
- Frontend-React
- **Build Tool**: Maven
