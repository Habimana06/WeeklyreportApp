📊 WeeklyReportApp

WeeklyReportApp is a full-stack weekly report management system that helps organizations streamline how employees submit reports, supervisors review them, and admins manage the overall process.

🚀 Features
👨‍💼 Admin

Create and manage user accounts (employees & supervisors)

Assign supervisors to employees

Manage system roles and permissions

🧑‍🏫 Supervisor

Review weekly reports submitted by employees

Provide structured feedback and approval/rejection

Track employee progress over time

👷 Employee

Submit weekly reports with key updates and progress

View feedback from supervisors

Edit and resubmit reports if needed

🛠 Tech Stack

Frontend: React.js (Vite/CRA, Tailwind CSS or Material UI)

Backend: Java Spring Boot (REST API)

Database: MySQL / PostgreSQL (configurable)

Authentication: JWT-based authentication (secure login for all roles)

Version Control: Git & GitHub

📂 Project Structure
WeeklyreportApp/
│── frontend/    # React.js frontend
│── backend/     # Java Spring Boot backend
│── README.md    # Documentation

⚙️ Installation & Setup
1️⃣ Clone the repository
git clone https://github.com/Habimana06/WeeklyreportApp.git
cd WeeklyreportApp

2️⃣ Backend Setup (Java Spring Boot)
cd backend
# Import into IntelliJ/Eclipse or build via Maven
mvn spring-boot:run


The backend will start at: http://localhost:8080

3️⃣ Frontend Setup (React.js)
cd frontend
npm install
npm start


The frontend will run at: http://localhost:3000

📌 Usage Flow

Admin logs in → creates users (employees, supervisors) → assigns supervisors.

Employee logs in → submits a weekly report.

Supervisor logs in → reviews reports → submits feedback (approve/reject).

Employee views feedback and makes improvements.

Admin monitors progress across the organization.

🔮 Future Improvements

Add email/notification system for report submissions and feedback.

Dashboard with charts/graphs for progress tracking.

Export reports to PDF/Excel.

Role-based analytics.

👨‍💻 Author

Ntaganira Habimana Happy
🎓 Adventist University of Central Africa (AUCA)
💡 Passionate about full-stack development & teamwork