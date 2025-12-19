# University ERP System

A comprehensive **Enterprise Resource Planning (ERP)** system designed for educational institutions. This Java-based application provides integrated management of students, instructors, courses, grades, announcements, and administrative operations.

## 📋 Features

### Core Functionality
- **User Authentication & Authorization** - Role-based access control for Students, Instructors, and Admins
- **Student Management** - Enrollment tracking, course registration, and academic records
- **Course Management** - Course creation, section management, and course offerings
- **Grading System** - Assessment creation, grade assignment, and transcript generation
- **Timetable Management** - Interactive timetable creation and viewing
- **Announcements** - System-wide announcements for students and instructors
- **Admin Dashboard** - Comprehensive administrative controls and reports
- **Audit Logging** - Complete audit trail of all system actions
- **Backup & Restore** - Database backup and recovery functionality
- **Maintenance Mode** - System maintenance controls for admins

### User Roles
- **Student** - View courses, register for courses, check grades, view announcements, manage timetable
- **Instructor** - Manage gradebook, assign grades, create assessments, view statistics
- **Administrator** - Manage users, courses, sections, system settings, backups, and audit logs

## Technology Stack

### Backend
- **Language**: Java
- **Database**: MySQL
- **JDBC Driver**: MySQL Connector/J 9.5.0

### Frontend
- **UI Framework**: Swing (Java AWT/Swing)
- **Look & Feel**: FlatLAF 3.2.5 (Modern flat UI design)
- **Layout Management**: MigLayout 11.0

### Libraries & Dependencies
- **Security**: jBCrypt 0.4.1 (Password hashing)
- **CSV Processing**: OpenCSV 5.7.1
- **PDF Generation**: PDFBox 2.0.31
- **Utilities**: Apache Commons Lang3 3.12.0, Apache Commons Text 1.10.0, Apache Commons Logging 1.2

## 📁 Project Structure

```
ERP_website_3/
├── src/
│   ├── edu/univ/erp/
│   │   ├── data/                 # Database connectivity & initialization
│   │   │   ├── DatabaseConnection.java
│   │   │   └── DatabaseInitializer.java
│   │   ├── auth/                 # Authentication & authorization
│   │   │   ├── AuthAPI.java
│   │   │   ├── LoginFrame.java
│   │   │   ├── ChangePasswordDialog.java
│   │   │   └── UserSession.java
│   │   ├── access/               # Access control
│   │   │   └── AccessControl.java
│   │   ├── dao/                  # Data Access Objects
│   │   │   ├── StudentDAO.java
│   │   │   ├── InstructorDAO.java
│   │   │   ├── AdminDAO.java
│   │   │   ├── CourseDAO.java
│   │   │   ├── GradeDAO.java
│   │   │   ├── EnrollmentDAO.java
│   │   │   ├── SectionDAO.java
│   │   │   ├── AssessmentDAO.java
│   │   │   ├── AnnouncementDAO.java
│   │   │   ├── CourseOfferingDAO.java
│   │   │   └── BranchDAO.java
│   │   ├── domain/               # Domain models & entities
│   │   │   ├── Student.java
│   │   │   ├── Instructor.java
│   │   │   ├── Course.java
│   │   │   ├── Grade.java
│   │   │   ├── Enrollment.java
│   │   │   ├── Section.java
│   │   │   ├── Assessment.java
│   │   │   ├── Announcement.java
│   │   │   ├── AuditLog.java
│   │   │   ├── Settings.java
│   │   │   └── Branch.java
│   │   ├── service/              # Business logic & services
│   │   │   ├── StudentService.java
│   │   │   ├── AdminService.java
│   │   │   ├── CourseRegistrationService.java
│   │   │   └── SettingsService.java
│   │   ├── ui/                   # User Interface
│   │   │   ├── admin/            # Admin panel screens
│   │   │   │   ├── AdminPanel.java
│   │   │   │   ├── StudentManagementPanel.java
│   │   │   │   ├── InstructorManagementPanel.java
│   │   │   │   ├── CourseManagementPanel.java
│   │   │   │   ├── TimetableEditorPanel.java
│   │   │   │   ├── BackupRestorePanel.java
│   │   │   │   └── MaintenanceModePanel.java
│   │   │   ├── student/          # Student panel screens
│   │   │   │   ├── StudentPanel.java
│   │   │   │   ├── CourseRegistrationPanel.java
│   │   │   │   ├── MyCoursesPanel.java
│   │   │   │   ├── TimetablePanel.java
│   │   │   │   └── AnnouncementViewDialog.java
│   │   │   └── instructor/       # Instructor panel screens
│   │   │       ├── InstructorPanel.java
│   │   │       ├── GradebookPanel.java
│   │   │       ├── FinalGradeDialog.java
│   │   │       └── StatisticsPanel.java
│   │   └── util/                 # Utility classes
│   │       └── TranscriptGenerator.java
│   └── edu.univ.erp.test/
│       └── TestDatabaseConnection.java
├── database/                     # Database files
├── lib/                          # External JAR libraries
├── resources/
│   └── database.properties       # Database configuration
├── DATABASE_SCHEMA.sql           # SQL schema & table definitions
└── README.md                     # This file
```

## 🚀 Getting Started

### Prerequisites
- **Java**: JDK 11 or higher
- **MySQL**: MySQL Server 5.7 or higher
- **IDE**: IntelliJ IDEA, Eclipse, or any Java IDE (optional)

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/erp-website-3.git
   cd erp-website-3
   ```

2. **Setup MySQL Database**
   ```bash
   mysql -u root -p < DATABASE_SCHEMA.sql
   ```
   This will create two databases:
   - `authdb` - Authentication database
   - `erpdb` - Main ERP database

3. **Configure Database Connection**
   Edit `resources/database.properties`:
   ```ini
   jdbc.erp.url=jdbc:mysql://localhost:3306/erpdb?serverTimezone=UTC
   jdbc.auth.url=jdbc:mysql://localhost:3306/authdb?serverTimezone=UTC
   jdbc.user=root
   jdbc.password=YOUR_PASSWORD_HERE
   jdbc.driver=com.mysql.cj.jdbc.Driver
   ```

4. **Compile the Project**
   ```bash
   javac -cp "lib/*:src" -d build src/edu/univ/erp/**/*.java
   ```

5. **Run the Application**
   ```bash
   java -cp "lib/*:build" edu.univ.erp.auth.LoginFrame
   ```

## 📊 Database Architecture

The system uses two separate MySQL databases:

### authdb (Authentication Database)
- `users_auth` - User credentials and role information
- `settings` - System settings

### erpdb (ERP Database)
- `students` - Student information
- `instructors` - Instructor information
- `courses` - Course catalog
- `sections` - Course sections
- `enrollments` - Student enrollments
- `grades` - Grade records
- `assessments` - Assessment information
- `announcements` - System announcements
- `audit_log` - Activity audit trail

See `DATABASE_SCHEMA.sql` for complete schema definitions.

## 🔐 Security Features

- **Password Hashing**: bcrypt algorithm for secure password storage
- **Role-Based Access Control (RBAC)**: Granular permissions based on user roles
- **Session Management**: Secure user session handling
- **Audit Logging**: Complete audit trail of all system actions
- **Prepared Statements**: SQL injection prevention through parameterized queries

## 📝 Usage Examples

### Student User
1. Login with student credentials
2. Register for available courses
3. View enrolled courses and timetable
4. Check grades and announcements
5. Download academic transcript

### Instructor User
1. Login with instructor credentials
2. View assigned sections and students
3. Create and manage assessments
4. Grade student work
5. View class statistics

### Administrator User
1. Login with admin credentials
2. Manage student and instructor accounts
3. Create and manage courses and sections
4. Configure system settings
5. Create system announcements
6. Generate backups and manage audit logs

## 🔧 Configuration

### Application Settings
- Database connection parameters in `resources/database.properties`
- System settings stored in database and manageable through admin panel
- Maintenance mode for scheduled system maintenance

### Timetable
- Interactive timetable editor for schedule management
- PDF export functionality available

## 📄 File Descriptions

- **DATABASE_SCHEMA.sql** - Complete database schema with all table definitions
- **database.properties** - Database connection configuration file
- **ERP.iml** - IntelliJ IDEA project configuration
- **PROJECT_DOCUMENTATION.pdf** - Detailed project documentation
- **relations.png** - Database relationship diagram

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests with improvements or bug fixes.

## 📜 License

This project is provided as-is for educational purposes.

## 📞 Support & Contact

For questions or issues, please open an issue in the repository or contact the project maintainer.

## 🎯 Future Enhancements

- [ ] Web-based interface using Spring Boot/Thymeleaf
- [ ] Mobile application for students and instructors
- [ ] Advanced analytics and reporting features
- [ ] Integration with external systems (email, SMS notifications)
- [ ] Multi-institution support
- [ ] Real-time notifications and messaging

## 📚 Additional Resources

- See `PROJECT_DOCUMENTATION.pdf` for comprehensive system documentation
- Watch `erp_video.mp4` for a system walkthrough and feature demonstration
- Check `relations.png` for database relationship diagram

---

**Last Updated**: December 2025

**Version**: 1.0.0

