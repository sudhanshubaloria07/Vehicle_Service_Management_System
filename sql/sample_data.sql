-- =====================================================================
-- Vehicle Service Management System (VSMS)
-- Realistic Sample Data for Testing & Demonstration
-- =====================================================================

USE `vehicle_service_db`;

-- ---------------------------------------------------------------------
-- 1. Insert Vehicles (8+ Indian vehicles, mix of Cars and Bikes)
-- ---------------------------------------------------------------------
INSERT INTO `Vehicle` (`vehicle_id`, `customer_name`, `phone`, `registration_number`, `brand`, `model`, `vehicle_type`) VALUES
(1, 'Aarav Sharma',     '9876543210', 'DL01AB1234', 'Hyundai', 'i20 Asta',           'Car'),
(2, 'Priya Patel',      '9823456789', 'MH12DE4567', 'Honda',   'Activa 6G',          'Bike'),
(3, 'Rohan Verma',      '9712345678', 'PB10AB5678', 'Maruti',  'Swift Dzire',        'Car'),
(4, 'Ananya Iyer',      '9654321098', 'KA05XY8888', 'Royal Enfield', 'Classic 350',  'Bike'),
(5, 'Vikram Singh',     '9543210987', 'HR26BC3344', 'Tata',    'Nexon EV',           'Car'),
(6, 'Neha Kulkarni',    '9432109876', 'MH14FF9012', 'Yamaha',  'FZ-S V3',            'Bike'),
(7, 'Aditya Malhotra',  '9321098765', 'CH01BB7788', 'Kia',     'Seltos HTX',         'Car'),
(8, 'Kavita Sundaram',  '9210987654', 'TN09GH4455', 'TVS',     'Jupiter 125',        'Bike'),
(9, 'Manish Gupta',     '9123456780', 'UP16AK2020', 'Toyota',  'Innova Crysta',      'Car'),
(10, 'Jaspreet Kaur',   '9888776655', 'PB08CG1122', 'Bajaj',   'Pulsar NS200',       'Bike');

-- ---------------------------------------------------------------------
-- 2. Insert Parts (14 realistic spare parts with pricing in INR)
-- ---------------------------------------------------------------------
INSERT INTO `Parts` (`part_id`, `part_name`, `price`, `quantity`) VALUES
(1,  'Synthetic Engine Oil 1L',    650.00,  35),
(2,  'Premium Oil Filter',          280.00,  28),
(3,  'Ceramic Front Brake Pads',    1450.00, 18),
(4,  'High-Flow Air Filter',        420.00,  24),
(5,  'Iridium Spark Plug',          320.00,  40),
(6,  'Heavy-Duty Clutch Cable',     290.00,   4),  -- low stock (< 5)
(7,  'Maintenance-Free Battery',   3800.00,   3),  -- low stock (< 5)
(8,  'Long-Life Radiator Coolant',  350.00,  15),
(9,  'Frameless Wiper Blades Pair', 750.00,  12),
(10, 'Reinforced Chain & Sprocket', 1850.00,  8),
(11, 'Ventilated Brake Disc Rotor', 2200.00,  6),
(12, 'Halogen Headlamp Bulb H4',    220.00,   2),  -- low stock (< 5)
(13, 'Brake Fluid DOT-4 500ml',     260.00,  16),
(14, 'Suspension Bushing Kit',     1100.00,  10);

-- ---------------------------------------------------------------------
-- 3. Insert Jobs (10 jobs across all 4 service types and all 3 statuses)
-- Base labour: General Service=1200, Engine Repair=3500, Oil Change=500, Brake Inspection=800
-- Car multiplier = 1.5, Bike multiplier = 1.0
-- ---------------------------------------------------------------------
INSERT INTO `Jobs` (`job_id`, `vehicle_id`, `service_type`, `booking_date`, `job_status`, `labour_charges`) VALUES
-- Completed jobs (ready for billing demo)
('JOB-2026-0001', 1, 'General Service',  '2026-11-15', 'Completed',   1800.00), -- Car (1200 * 1.5)
('JOB-2026-0002', 2, 'Oil Change',       '2026-11-16', 'Completed',    500.00), -- Bike (500 * 1.0)
('JOB-2026-0003', 3, 'Brake Inspection', '2026-11-17', 'Completed',   1200.00), -- Car (800 * 1.5)
('JOB-2026-0004', 4, 'General Service',  '2026-11-17', 'Completed',   1200.00), -- Bike (1200 * 1.0)

-- In Progress jobs (ready for simulation or parts assignment)
('JOB-2026-0005', 5, 'Engine Repair',    '2026-11-18', 'In Progress', 5250.00), -- Car (3500 * 1.5)
('JOB-2026-0006', 6, 'General Service',  '2026-11-19', 'In Progress', 1200.00), -- Bike (1200 * 1.0)
('JOB-2026-0007', 7, 'Oil Change',       '2026-11-19', 'In Progress',  750.00), -- Car (500 * 1.5)

-- Pending jobs (ready for starting simulation)
('JOB-2026-0008', 8, 'Brake Inspection', '2026-11-20', 'Pending',      800.00), -- Bike (800 * 1.0)
('JOB-2026-0009', 9, 'Engine Repair',    '2026-11-20', 'Pending',     5250.00), -- Car (3500 * 1.5)
('JOB-2026-0010', 10,'General Service',  '2026-11-21', 'Pending',     1200.00); -- Bike (1200 * 1.0)

-- ---------------------------------------------------------------------
-- 4. Insert Job_Parts (Junction Table Rows)
-- Consistent with parts usage and stock
-- ---------------------------------------------------------------------
-- JOB-2026-0001: General Service on Car (i20)
INSERT INTO `Job_Parts` (`job_id`, `part_id`, `quantity_used`) VALUES
('JOB-2026-0001', 1, 3),   -- 3L Synthetic Engine Oil
('JOB-2026-0001', 2, 1),   -- 1 Oil Filter
('JOB-2026-0001', 4, 1),   -- 1 Air Filter
('JOB-2026-0001', 9, 1);   -- 1 Pair Wiper Blades

-- JOB-2026-0002: Oil Change on Bike (Activa)
INSERT INTO `Job_Parts` (`job_id`, `part_id`, `quantity_used`) VALUES
('JOB-2026-0002', 1, 1),   -- 1L Synthetic Engine Oil
('JOB-2026-0002', 2, 1);   -- 1 Oil Filter

-- JOB-2026-0003: Brake Inspection on Car (Dzire)
INSERT INTO `Job_Parts` (`job_id`, `part_id`, `quantity_used`) VALUES
('JOB-2026-0003', 3, 1),   -- 1 Front Brake Pad Set
('JOB-2026-0003', 13, 1);  -- 1 Brake Fluid 500ml

-- JOB-2026-0004: General Service on Bike (Classic 350)
INSERT INTO `Job_Parts` (`job_id`, `part_id`, `quantity_used`) VALUES
('JOB-2026-0004', 1, 2),   -- 2L Engine Oil
('JOB-2026-0004', 2, 1),   -- 1 Oil Filter
('JOB-2026-0004', 5, 1);   -- 1 Spark Plug

-- JOB-2026-0005: Engine Repair on Car (Nexon) - In Progress
INSERT INTO `Job_Parts` (`job_id`, `part_id`, `quantity_used`) VALUES
('JOB-2026-0005', 8, 2),   -- 2 Coolant Bottles
('JOB-2026-0005', 5, 4);   -- 4 Spark Plugs
