
# University ERP System

A comprehensive University Enterprise Resource Planning (ERP) system built with Java Swing and SQLite. This application manages Students, Instructors, Courses, Sections, and Grades, featuring role-based access for Admins, Instructors, and Students.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [How to Run](#how-to-run)
- [Default Credentials](#default-credentials)
- [Features & Usage](#features--usage)
    - [Admin Dashboard](#admin-dashboard)
    - [Instructor Dashboard](#instructor-dashboard)
    - [Student Dashboard](#student-dashboard)
- [Backup & Restore (Admin)](#backup--restore-admin)

## Prerequisites

* **Java Development Kit (JDK):** Version 17 or higher.
* **IntelliJ IDEA:** Recommended IDE.
* **Maven:** For dependency management.

## Installation & Setup

1.  **Clone/Download Project:** Extract the project files to a local directory.
2.  **Open in IntelliJ:**
    * File > Open > Select project folder.
3.  **Add Dependencies:**
    * This project uses `sqlite-jdbc` and `openpdf`.
    * If using Maven, ensure your `pom.xml` contains:
        ```xml
        <dependencies>
            <dependency>
                <groupId>org.xerial</groupId>
                <artifactId>sqlite-jdbc</artifactId>
                <version>3.42.0.0</version>
            </dependency>
            <dependency>
                <groupId>com.github.librepdf</groupId>
                <artifactId>openpdf</artifactId>
                <version>1.3.30</version>
            </dependency>
        </dependencies>
        ```
    * Reload the Maven project to download the JARs.

## How to Run

1.  **Locate the Main File:** Open `src/PopulateData.java`.
2.  **Populate Database:**
    * Run `PopulateData.java` **once**.
    * This script creates the database files (`erp.db`, `credentials.db`, etc.) and populates them with sample data (50 students, 5 instructors, courses, sections, and grades).
    * *Note: If you want to reset the system, simply delete all `.db` files in the project root and run `PopulateData.java` again.*
3.  **Launch Application:**
    * Open `src/Main.java` (or create one that launches `new LoginView().setVisible(true)`).
    * Run the application.

## Default Credentials

The `PopulateData.java` script creates the following default users. All passwords default to **`123`**.

| Role | User ID | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `ADMIN` | `123` | Full system access. |
| **Instructor** | `INST_001` | `123` | Manage assigned sections & grades. |
| **Instructor** | `INST_002` | `123` | ... up to `INST_005`. |
| **Student** | `STU_001` | `123` | View courses, timetable, transcripts. |
| **Student** | `STU_002` | `123` | ... up to `STU_050`. |

## Features & Usage

### Admin Dashboard
* **Login:** Use `ADMIN` / `123`.
* **Manage Users:** Create, Edit, or Delete Students and Instructors.
* **Manage Curriculum:** Create Courses and Sections.
    * **Assign Instructors:** Assign sections to specific instructors via the "Edit Section" dialog.
    * **Manage Timetables:** Set weekly schedules for sections.
* **System Rules:**
    * **Active Semester:** Set the current semester (e.g., `SUMMER_2025`) at the bottom of the "Manage Sections" view.
    * **Add/Drop Deadline:** Set the cutoff date for student enrollment changes.
* **Maintenance Mode:** Toggle "Maintenance Mode" in the sidebar. This restricts student actions (e.g., enrolling/dropping courses) but allows viewing data.

### Instructor Dashboard
* **Login:** Use an ID like `INST_001`.
* **View Sections:** See assigned courses.
* **Grade Students:** Enter marks for Labs, Quizzes, Mids, Finals, etc.
* **View Stats:** See class averages and performance summaries.

### Student Dashboard
* **Login:** Use an ID like `STU_001`.
* **My Courses:** View enrolled courses and detailed grade breakdowns.
    * **Export Transcript:** Click "Export PDF" to generate a PDF report of academic history.
* **Manage Courses:** Browse the course catalog.
    * **Enroll/Drop:** Register for classes (if within deadline and maintenance is OFF).
    * **Search:** Filter courses by name or code.
* **Timetable:** View weekly class schedule in a visual grid.

## Backup & Restore (Admin)

The Admin Dashboard includes dedicated buttons in the sidebar for database management:

1.  **Export DB:**
    * Click "Export DB".
    * Select a destination folder.
    * Enter a filename prefix (e.g., `backup_v1`).
    * The system creates `backup_v1_erp.db` and `backup_v1_credentials.db`.

2.  **Import DB:**
    * Click "Import DB".
    * Select the **ERP** backup file (`.db`) when prompted.
    * Select the **Credentials** backup file (`.db`) when prompted.
    * Confirm the overwrite. The application will close, and you must restart it to load the restored data.

# About Domain

following is diagram of domain of this project.
![domain_diagram.svg](domain_diagram.svg)