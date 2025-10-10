# 🔌 EV-ChargeHub
-------------------------------

## 🚗 Overview
EV ChargeHub is a full-stack solution designed to streamline the EV charging experience in Sri Lanka. The platform offers role-based dashboards, real-time station availability, booking management, and comprehensive analytics.

-------------------------------

## 🎯 Key Features
- Multi-Platform Support: Web application and Android mobile app
- Role-Based Access Control: Separate interfaces for EV owners, station operators, and administrators
- Real-Time Availability: Live tracking of charging station slots
- Smart Booking System: Advanced reservation and scheduling capabilities
- Comprehensive Analytics: Detailed reporting and insights
- Secure Authentication: JWT-based authentication with role management
- Responsive Design: Mobile-first, responsive user interface
  
-------------------------------

## 🚀 Getting Started
Prerequisites
- Frontend: Node.js 18+ and npm/yarn
- Backend: .NET 9 SDK
- Mobile: Android Studio with Kotlin support
- Database: MongoDB & SQLite

### 🌐 Web Application Frontend

```bash
cd EV-WebApp-Frontend
npm i
npm run dev
```

### ⚙️ Web Application Backend

```bash
cd EV-WebApp-Backend
dotnet run
```

### 📱 Mobile Application

1. Open the EV-MobileApp folder in Android Studio
2. Sync the project with Gradle files
3. Update the API base URL
4. Build and run on emulator or android device

-------------------------------

## 🔧 Technology Stack
Frontend
- Framework: React 18 with TypeScript
- Styling: Tailwind CSS
- State Management: React Context API
  
Backend
- Framework: .NET 9 Web API
- Database: MongoDB with official .NET driver
- Authentication: JWT Bearer tokens

Mobile
- Platform: Android (Kotlin)
- Local Storage: SQLite
- UI Components: Material Design

------------------------------

## 🚀 Deployment

Frontend Deployement
```bash
npm run build
```

Backend Deployment
```bash
dotnet publish -c Release -o ./publish
```

-------------------------------
