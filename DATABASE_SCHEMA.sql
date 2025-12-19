-- ============================
-- AUTH DATABASE
-- ============================
CREATE DATABASE IF NOT EXISTS authdb;
USE authdb;

CREATE TABLE IF NOT EXISTS settings (
    setting_key   VARCHAR(100) NOT NULL PRIMARY KEY,
    setting_value VARCHAR(255) NULL
    );

CREATE TABLE IF NOT EXISTS users_auth (
    user_id       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    role          ENUM('Student','Instructor','Admin') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(20) DEFAULT 'active',
    last_login    TIMESTAMP NULL
    );

-- ============================
-- ERP DATABASE
-- ============================
CREATE DATABASE IF NOT EXISTS erpdb;
USE erpdb;

CREATE TABLE IF NOT EXISTS announcements (
    announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL DEFAULT 'Announcement',
    message         TEXT NOT NULL,
    created_by      INT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS assessments (
    assessment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    section_id    INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    max_score     DECIMAL(8,2) NOT NULL DEFAULT 100.00,
    weight        DECIMAL(5,2) NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id    INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NULL,
    action      VARCHAR(100) NOT NULL,
    object_type VARCHAR(50) NULL,
    object_id   INT NULL,
    details     TEXT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS branches (
    branch_id   INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS course_eligibility (
    eligibility_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id      INT NOT NULL,
    target_branch  VARCHAR(50) NULL,
    target_year    INT NULL,
    is_mandatory   TINYINT(1) DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS course_offerings (
    offering_id  INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    branch_id    INT NOT NULL,
    semester     INT NOT NULL,
    course_id    INT NOT NULL,
    is_mandatory TINYINT(1) DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS courses (
    course_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code      VARCHAR(20) NOT NULL UNIQUE,
    title     VARCHAR(255) NOT NULL,
    credits   INT NULL
    );

CREATE TABLE IF NOT EXISTS enrollments (
                                           enrollment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                           student_id    INT NOT NULL,
                                           section_id    INT NOT NULL,
                                           status        VARCHAR(20) DEFAULT 'enrolled',
    final_grade   VARCHAR(5) NULL,
    cgpa          DECIMAL(3,1) NULL
    );

CREATE TABLE IF NOT EXISTS grades (
  grade_id      INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  enrollment_id INT NOT NULL,
  assessment_id INT NOT NULL,
  score         DECIMAL(5,2) NULL
    );

CREATE TABLE IF NOT EXISTS instructors (
   user_id    INT NOT NULL PRIMARY KEY,
   name       VARCHAR(100) NULL,
    email      VARCHAR(255) NULL,
    department VARCHAR(100) NULL,
    fullName   VARCHAR(100) NULL
    );

CREATE TABLE IF NOT EXISTS master_timetable (
    id          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    day         VARCHAR(20) NOT NULL,
    time_slot   VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NULL,
    INDEX idx_day (day)
    );

CREATE TABLE IF NOT EXISTS section_enrollment_counts (
     section_id     INT NOT NULL DEFAULT 0,
     capacity       INT NULL,
     enrolled_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sections (
    section_id    INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id     INT NOT NULL,
    instructor_id INT NULL,
    day_time      VARCHAR(50) NULL,
    room          VARCHAR(30) NULL,
    capacity      INT NULL,
    semester      INT NULL,
    year          INT NULL
    );

CREATE TABLE IF NOT EXISTS settings (
    setting_key   VARCHAR(50) NOT NULL PRIMARY KEY,
    setting_value VARCHAR(255) NULL
    );

CREATE TABLE IF NOT EXISTS students (
    user_id           INT NOT NULL PRIMARY KEY,
    name              VARCHAR(100) NULL,
    roll_no           VARCHAR(20) NOT NULL UNIQUE,
    fullName          VARCHAR(100) NULL,
    program           VARCHAR(100) NULL,
    branch            VARCHAR(100) NULL,
    year_of_admission INT NULL,
    cgpa              DECIMAL(4,2) NULL,
    year              INT NULL
    );

