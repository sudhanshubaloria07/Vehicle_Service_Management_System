-- =====================================================================
-- Vehicle Service Management System (VSMS)
-- Database Schema Definition
-- Engine: InnoDB | Charset: utf8mb4
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `vehicle_service_db`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `vehicle_service_db`;

-- ---------------------------------------------------------------------
-- Table: Vehicle
-- Holds customer contact information alongside vehicle specs
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `Job_Parts`;
DROP TABLE IF EXISTS `Jobs`;
DROP TABLE IF EXISTS `Parts`;
DROP TABLE IF EXISTS `Vehicle`;

CREATE TABLE `Vehicle` (
    `vehicle_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(15) NOT NULL,
    `registration_number` VARCHAR(20) NOT NULL UNIQUE,
    `brand` VARCHAR(50) NOT NULL,
    `model` VARCHAR(50) NOT NULL,
    `vehicle_type` ENUM('Car', 'Bike') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: Jobs
-- Stores service appointments, service types, status and labour fees
-- ---------------------------------------------------------------------
CREATE TABLE `Jobs` (
    `job_id` VARCHAR(20) PRIMARY KEY,
    `vehicle_id` INT NOT NULL,
    `service_type` ENUM('General Service', 'Engine Repair', 'Oil Change', 'Brake Inspection') NOT NULL,
    `booking_date` DATE NOT NULL,
    `job_status` ENUM('Pending', 'In Progress', 'Completed') NOT NULL DEFAULT 'Pending',
    `labour_charges` DECIMAL(10, 2) NOT NULL,
    CONSTRAINT `fk_jobs_vehicle`
        FOREIGN KEY (`vehicle_id`) REFERENCES `Vehicle` (`vehicle_id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: Parts
-- Inventory of replacement parts with unit pricing and stock counts
-- ---------------------------------------------------------------------
CREATE TABLE `Parts` (
    `part_id` INT AUTO_INCREMENT PRIMARY KEY,
    `part_name` VARCHAR(100) NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL CHECK (`price` >= 0),
    `quantity` INT NOT NULL CHECK (`quantity` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: Job_Parts (Junction Table)
-- Associates parts used in specific service jobs (M:N relationship)
-- ---------------------------------------------------------------------
CREATE TABLE `Job_Parts` (
    `job_id` VARCHAR(20) NOT NULL,
    `part_id` INT NOT NULL,
    `quantity_used` INT NOT NULL CHECK (`quantity_used` > 0),
    PRIMARY KEY (`job_id`, `part_id`),
    CONSTRAINT `fk_jobparts_job`
        FOREIGN KEY (`job_id`) REFERENCES `Jobs` (`job_id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT `fk_jobparts_part`
        FOREIGN KEY (`part_id`) REFERENCES `Parts` (`part_id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
