# Milou - Java Backend & Email Management System

A robust, object-oriented Java backend application built with a clean layered architecture, featuring database integration via Hibernate ORM and dependency management using Maven.

## 🚀 Tech Stack
* **Java** (Core Programming Language)
* **Maven** (Dependency Management & Build Tool)
* **Hibernate / ORM** (Object-Relational Mapping & Database Sessions)
* **SQL** (Database Schema & Management)[cite: 1]

## 📁 Project Structure
* `src/main/java/com/milou/model`: Contains core domain entities such as `User`, `Email`, and `EmailRecipient`.
* `src/main/java/com/milou/service`: Implements business logic and application services including `UserService` and `EmailService`.
* `src/main/java/com/milou/framework`: Manages database connectivity and session factories using the Singleton design pattern (`SingletonSessionFactory`).
* `src/main/java/SQL`: Stores database schema and initialization scripts (`schema.sql`).[cite: 1]

## ⚙️ Getting Started

### Prerequisites
* Java Development Kit (JDK)
* Apache Maven
* A relational database management system (e.g., MySQL, PostgreSQL)

### Installation & Configuration
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/milou.git](https://github.com/your-username/milou.git)
