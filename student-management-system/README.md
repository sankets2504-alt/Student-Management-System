# Student Management System (SMS)

A full-featured **Desktop Application** built using **Java Swing** and **MySQL**, designed with a modern GUI powered by **FlatLaf**. It features distinct **Admin** and **User (Student)** Dashboards with role-based access control.

---

## 👥 Roles & Dashboards Overview

### 1. 🛡️ Administrator Dashboard
Provides complete administrative control over the university or school system:
- **Admin Analytics Dashboard**: High-level KPI metrics (Total Students, Active Students, Total Courses, Total Grade Records, Class Average Score) and real-time visual progress bars by Department and Academic Year.
- **Student Directory**: Full CRUD (Add, Edit, Delete, View) with input validation (duplicate roll number and email checks), live search, multi-criteria filtering, and CSV export.
- **Course Catalog Management**: Create, edit, and delete courses, assign credits (1–10), instructors, and department affiliations.
- **Grades & Enrollment Book**: Assign marks (0–100) per course and semester with automatic Letter Grade (`A+` to `F`) and GPA point calculation. Color-coded grade badges and CSV export.
- **User Accounts Management**: View registered system accounts, assign `ADMIN` or `STUDENT` roles, and link login accounts to student profiles.
- **Database Tools**: Test active connections, adjust MySQL credentials on the fly, or re-initialize tables and seed data.

---

### 2. 🎓 User / Student Dashboard
Tailored for individual students to access and manage their academic journey:
- **Personalized Header**: Displays student's name, roll number, department, academic year, and status in real-time.
- **Academic KPI Cards**:
  - **Cumulative GPA**: Calculated based on completed credit hours and grade points.
  - **Enrolled Courses**: Count of currently registered courses.
  - **Credits Completed**: Total credit hours earned.
  - **Average Score**: Overall percentage score across courses.
- **My Grades & Transcript**:
  - Personal grade sheet showing Course Code, Course Title, Semester, Credits, Marks, Letter Grade, and Remarks.
  - **Download Transcript (CSV)**: One-click export of the student's personal report card.
- **Course Catalog**: Read-only browser to explore available courses across departments with descriptions and instructor info.
- **My Profile & Account Management**:
  - **View Profile**: View full registration details (contact info, date of birth, enrollment date, email, status).
  - **Edit My Profile**: Students can directly update their First/Last Name, Email Address, Phone Number, Gender, and Date of Birth!
  - **Change Password**: Self-service password updates with current password verification.
- **Self-Service Registration**: New students can register themselves directly from the Sign In window by clicking **"New Student? Create Account"**!
- **Logout & Switch User**: Easily switch between accounts.

---

## 🔑 Default Login Credentials

The application includes pre-configured demo credentials:

| Role | Username | Password | Linked Student / Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full access to all administrative management tools |
| **Student (User)** | `student` | `student123` | Logged in as student **Aarav Sharma** (`STU-1001`) |
| **Student (User)** | `priya` | `priya123` | Logged in as student **Priya Patel** (`STU-1002`) |

> **Tip:** The sign-in window also includes **"Quick Demo Access"** buttons: click **"Admin Portal"** or **"Student Portal"** to log in instantly without typing.

---

## 🚀 How to Run the Application

### Method 1: One-Click Windows Script (Easiest)
Navigate to the project folder and double-click:
- **`run.bat`**

*(If the `.class` files are not found, `run.bat` automatically calls `build.bat` to compile first, then launches the application).*

---

### Method 2: From PowerShell or Terminal
Open a terminal in `C:\Users\shubh\.gemini\antigravity\scratch\student-management-system`:

1. **Compile the code:**
   ```powershell
   cmd /c "dir /s /b src\*.java > sources.txt && javac -encoding UTF-8 -cp lib/* -d bin @sources.txt && del sources.txt"
   ```
   *(Or simply run `.\build.bat`)*

2. **Run the application:**
   ```powershell
   java -cp "bin;lib/*" com.sms.Main
   ```

---

## ⚙️ MySQL Database Setup & First Launch

Your Windows machine already has the `MySQL80` service running.

1. Launch the application using `run.bat`.
2. If your MySQL root password is not yet configured, the **Database Configuration Dialog** will pop up automatically.
3. Enter your MySQL credentials:
   - **Host**: `localhost`
   - **Port**: `3306`
   - **Database Name**: `student_management_db`
   - **Username**: `root`
   - **Password**: *(your MySQL root password)*
4. Click **"Test Connection"** to verify.
5. Click **"Save & Connect"**. The application will automatically create the database, all required tables (`students`, `courses`, `grades`, `users`), and seed initial sample data.
6. The **Sign In Dialog** will appear — choose **Admin** or **Student** to enter!

---

## 📁 Project Structure

```
student-management-system/
├── bin/                             # Compiled Java bytecode (.class files)
├── config/
│   └── db.properties                # MySQL connection configuration
├── lib/
│   ├── flatlaf-3.5.4.jar            # Modern Flat Look and Feel GUI
│   └── mysql-connector-j-8.4.0.jar  # MySQL JDBC 8.4 Driver
├── src/com/sms/
│   ├── Main.java                    # App entry point & startup router
│   ├── dao/                         # StudentDAO, CourseDAO, GradeDAO, UserDAO
│   ├── database/                    # DatabaseConfig, DatabaseManager
│   ├── model/                       # Student, Course, Grade, User, DashboardStats
│   ├── ui/
│   │   ├── LoginDialog.java         # Role-based login with quick demo access
│   │   ├── MainFrame.java           # Administrator Dashboard
│   │   ├── UserDashboardFrame.java  # Student / User Dashboard
│   │   ├── UserAccountsPanel.java   # User accounts administration tab
│   │   ├── DashboardPanel.java      # Admin analytics dashboard
│   │   ├── StudentPanel.java        # Student CRUD & filter panel
│   │   ├── CoursePanel.java         # Course catalog management panel
│   │   ├── GradePanel.java          # Gradebook & enrollment panel
│   │   ├── DatabaseSettingsDialog.java # MySQL connection settings modal
│   │   ├── StudentDialog.java       # Add/Edit Student modal
│   │   ├── CourseDialog.java        # Add/Edit Course modal
│   │   └── GradeDialog.java         # Assign/Edit Grade modal
│   └── util/
│       └── CsvExporter.java         # CSV exporter for data and transcripts
├── build.bat                        # Batch compilation script
├── run.bat                          # Batch execution script
├── schema.sql                       # Full MySQL schema & seed script
└── README.md                        # Documentation
```
