# AutoCare - Vehicle Service Management System (VSMS)

A complete, production-quality desktop application developed for the **B.Tech CSE 2nd Year Object-Oriented Programming with Java (OOPJ)** curriculum. 

AutoCare VSMS provides comprehensive automotive service station management covering **Customer & Vehicle Registration**, **Service Job Scheduling**, **Real-Time Job Card Tracking & Multithreaded Progress Simulation**, **Transactional Spare Parts Inventory**, and **Itemized Tax Invoice Generation with 18% GST**.

---

## 1. Technology Stack

- **Language**: Java 17 (Java 8+ backward and forward compatible)
- **GUI Framework**: Java Swing (`JFrame`, `CardLayout`, `JTable`, `GridBagLayout`, `JProgressBar`, `Timer`)
- **Database Engine**: MySQL 8.x (InnoDB engine with foreign key constraints)
- **Connectivity**: JDBC (`PreparedStatement`, `Statement.RETURN_GENERATED_KEYS`, row-level locking with `SELECT ... FOR UPDATE`, transaction rollbacks)
- **JDBC Driver**: MySQL Connector/J (`mysql-connector-j-8.3.0.jar`)
- **Build Systems Supported**: Plain `javac` command line, `build.bat`, `build.sh`, Maven (`pom.xml`), VS Code, IntelliJ IDEA

---

## 2. Project Directory Structure

```
VSMS/
├── lib/
│   └── mysql-connector-j-8.3.0.jar       # MySQL Connector/J JDBC Driver
├── sql/
│   ├── schema.sql                        # Re-runnable DDL schema definition
│   └── sample_data.sql                   # Realistic sample data (10 vehicles, 10 jobs, 14 parts)
├── config/
│   └── db.properties                     # Database connection properties
├── src/
│   └── service/
│       ├── core/                         # Core models, services, multithreading, and main entry
│       │   ├── Customer.java             # Owner entity (Composition)
│       │   ├── Part.java                 # Inventory entity
│       │   ├── ServiceJob.java           # Job card appointment entity
│       │   ├── ProgressListener.java     # Callback interface for thread events
│       │   ├── JobProgressThread.java    # Multithreaded simulation (implements Runnable)
│       │   ├── Validator.java            # Comprehensive input validation utilities
│       │   ├── JobService.java           # Service layer with polymorphic labour calculation
│       │   ├── PartService.java          # Service layer for inventory
│       │   ├── BillingService.java       # Service layer for invoices
│       │   └── MainApp.java              # Application main entry point (EDT launcher)
│       ├── vehicle/                      # Vehicle inheritance hierarchy & polymorphism
│       │   ├── Vehicle.java              # Abstract parent class
│       │   ├── Car.java                  # Concrete subclass (1.5x labour multiplier)
│       │   └── Bike.java                 # Concrete subclass (1.0x labour multiplier)
│       ├── billing/                      # Billing interfaces & models
│       │   ├── Billable.java             # Contract interface for billing
│       │   └── Bill.java                 # Tax invoice model with Math.round() currency precision
│       ├── database/                     # Data Access Objects (DAOs)
│       │   ├── DBConnection.java         # Connection manager & driver loader
│       │   ├── VehicleDAO.java           # CRUD & search for vehicles
│       │   ├── JobDAO.java               # Atomic Job ID generator (JOB-YYYY-NNNN) & CRUD
│       │   ├── PartDAO.java              # Inventory DAO with low-stock filtering
│       │   ├── JobPartsDAO.java          # Transactional row-locked part assignment
│       │   └── DashboardDAO.java         # Aggregation count queries & metrics
│       ├── exception/                    # Custom checked exceptions
│       │   └── InvalidJobException.java  # Business validation & state violation exception
│       └── gui/                          # Java Swing presentation layer
│           ├── Theme.java                # Navy/blue automotive styling, tokens, and renderers
│           ├── SidebarButton.java        # Custom sidebar button with active accent stripe
│           ├── StatCard.java             # Metric card component for KPI overview
│           ├── DashboardPanel.java       # Metric cards & recent jobs overview
│           ├── CustomerVehiclePanel.java # Registration form, search, and vehicle table
│           ├── ServiceBookingPanel.java  # Booking form with live polymorphic labour calculator
│           ├── JobCardPanel.java         # Job details inspector, simulation, & assigned parts
│           ├── PartsPanel.java           # Spare parts CRUD, low-stock highlight, & assignment
│           ├── BillingPanel.java         # Completed-job invoice viewer & text file exporter
│           └── MainFrame.java            # Main shell with sidebar, live digital clock & cards
├── pom.xml                               # Optional Maven configuration
├── build.bat                             # Windows compilation & run batch script
├── build.sh                              # Linux / macOS compilation & run shell script
├── .gitignore                            # Build artifact exclusions
└── README.md                             # Project documentation & viva guide
```

---

## 3. Database Setup (MySQL 8)

### Prerequisites
1. Ensure MySQL Server 8.x is installed and running on port `3306`.
2. Locate or install MySQL Command Line Client or MySQL Workbench.

### Step 3.1: Execute Schema & Sample Data via Command Line
Open PowerShell, CMD, or Terminal and run:

```bash
# 1. Create database and tables
mysql -u root -p < sql/schema.sql

# 2. Populate realistic sample records
mysql -u root -p < sql/sample_data.sql
```

### Step 3.2: Alternative Setup via MySQL Workbench
1. Open **MySQL Workbench** and connect to your local MySQL instance.
2. Select **File -> Open SQL Script...** and choose `sql/schema.sql`. Click the lightning bolt (**Execute**) icon.
3. Select **File -> Open SQL Script...** and choose `sql/sample_data.sql`. Click **Execute**.
4. Right-click the schema list in the left navigator and click **Refresh All**. Verify `vehicle_service_db` is present with tables: `Vehicle`, `Jobs`, `Parts`, and `Job_Parts`.

### Step 3.3: Configure `config/db.properties`
Verify that `config/db.properties` contains your MySQL password:

```properties
db.url=jdbc:mysql://localhost:3306/vehicle_service_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true&characterEncoding=utf8
db.user=root
db.password=your_mysql_password_here
```

---

## 4. How to Compile & Run

### Method A: Automated Scripts (Quickest)

- **On Windows**:
  Double-click `build.bat` or run:
  ```cmd
  build.bat
  ```

- **On Linux / macOS**:
  Make executable and run:
  ```bash
  chmod +x build.sh
  ./build.sh
  ```

---

### Method B: Plain Command Line (`javac` & `java`)

#### 1. Compile Java Source:
- **Windows (PowerShell / Command Prompt)**:
  ```cmd
  mkdir bin
  javac -encoding UTF-8 -cp "lib/*" -d bin src/service/exception/*.java src/service/vehicle/*.java src/service/billing/*.java src/service/core/*.java src/service/database/*.java src/service/gui/*.java
  ```

- **Linux / macOS**:
  ```bash
  mkdir -p bin
  javac -encoding UTF-8 -cp "lib/*" -d bin src/service/exception/*.java src/service/vehicle/*.java src/service/billing/*.java src/service/core/*.java src/service/database/*.java src/service/gui/*.java
  ```

#### 2. Run Application:
- **Windows** (uses semicolon `;` classpath separator):
  ```cmd
  java -cp "bin;lib/*" service.core.MainApp
  ```

- **Linux / macOS** (uses colon `:` classpath separator):
  ```bash
  java -cp "bin:lib/*" service.core.MainApp
  ```

---

### Method C: Running in Visual Studio Code (VS Code)

1. Open the project root folder (`VSMS/`) in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. In the left Explorer pane, expand **JAVA PROJECTS -> Referenced Libraries**.
4. Click the **+** (Add) icon and select `lib/mysql-connector-j-8.3.0.jar`.
5. Open `src/service/core/MainApp.java` and click **Run** above `public static void main(String[] args)`.

---

### Method D: Running in IntelliJ IDEA

1. Open IntelliJ IDEA, select **Open**, and choose the project directory.
2. Open **File -> Project Structure... -> Libraries**.
3. Click **+ -> Java**, navigate to the `lib/` directory, and select `mysql-connector-j-8.3.0.jar`. Click **Apply -> OK**.
4. Set Project SDK to Java 17 (or Java 8+).
5. Navigate to `src/service/core/MainApp.java`, right-click inside the file, and select **Run 'MainApp.main()'**.

---

## 5. OOPJ Syllabus Concept Mapping

Every major Object-Oriented Programming with Java syllabus concept is implemented with production rigor and marked in code comments:

| Unit | Syllabus Concept | Implementation File & Location | Description & Code Manifestation |
|---|---|---|---|
| **Unit 1** | **Classes & Objects** | `service.core.Customer`<br>`service.core.Part`<br>`service.core.ServiceJob`<br>`service.billing.Bill` | Real-world automotive entities encapsulating data and operational methods. |
| **Unit 1** | **Encapsulation** | All models in `service.core` and `service.vehicle` | Private attributes guarded by public getters and validation-checked mutators (e.g. non-negative pricing). |
| **Unit 1** | **Constructor Overloading** | `Customer.java`, `Part.java`, `ServiceJob.java`, `Bill.java` | Every model features both a default no-argument constructor and parameterized constructors. |
| **Unit 1** | **Math API** | `Bill.java`<br>`Part.java`<br>`JobPartsDAO.java` | Currency rounded via `Math.round(x * 100.0) / 100.0`; non-negative stock enforced via `Math.max(0, stock - used)`. |
| **Unit 2** | **Inheritance** | `service.vehicle.Vehicle` (Parent)<br>`service.vehicle.Car` (Child)<br>`service.vehicle.Bike` (Child) | `Car` and `Bike` extend `Vehicle`, reusing common registration and owner attributes while specializing behaviour. |
| **Unit 2** | **Method Overriding & Polymorphism** | `Vehicle.java`<br>`Car.java`<br>`Bike.java`<br>`JobService.java` | `Car` overrides `getLabourMultiplier()` to return `1.5`, whereas `Bike` returns `1.0`. `JobService` calls `vehicle.calculateLabourSurcharge()` polymorphically at runtime. |
| **Unit 2** | **Interfaces** | `service.billing.Billable`<br>`service.core.ProgressListener` | `Billable` defines calculation contracts implemented by `Bill`. `ProgressListener` decouples background thread events from GUI updates. |
| **Unit 2** | **Composition (HAS-A)** | `service.vehicle.Vehicle`<br>`service.core.Customer` | `Vehicle` contains a `Customer` object (`vehicle.getCustomer()`), modeling ownership without redundancy. |
| **Unit 2** | **Packages** | 6 distinct packages: `core`, `vehicle`, `billing`, `database`, `exception`, `gui` | Clean separation of business logic, presentation, data tier, models, and exceptions. |
| **Unit 3** | **Custom Exceptions** | `service.exception.InvalidJobException` | Checked exception extending `Exception` thrown when jobs are malformed, not found, or during illegal state transitions. |
| **Unit 3** | **Exception Handling** | All DAOs and GUI Actions | Explicit `try-catch-finally` blocks; closing `ResultSet`, `PreparedStatement`, `Connection`; catching `SQLException` and `NumberFormatException`. |
| **Unit 3** | **Multithreading** | `service.core.JobProgressThread`<br>`service.gui.JobCardPanel` | Implements `Runnable` wrapped in `Thread`; advances service stages with `Thread.sleep()`; notifies GUI safely on EDT via `SwingUtilities.invokeLater()`; synchronized active job set prevents duplicate simulations. |
| **Unit 3** | **Transactions & Atomicity** | `service.database.JobPartsDAO`<br>`service.database.JobDAO` | Atomic transactions using `setAutoCommit(false)`, `SELECT ... FOR UPDATE` row locks, commit on success, and rollback in catch blocks. |

---

## 6. Detailed Module Guide & Features

### 1. Dashboard Overview (`DashboardPanel.java`)
- **Live KPI Stat Cards**: Total Vehicles, Total Bookings, Pending Jobs, In Progress Jobs, Completed Jobs, and Low-Stock Spare Parts (<5 units).
- **Quick Actions**: One-click shortcuts to book services, register vehicles, check stock, or generate invoices.
- **Recent Appointments Table**: Real-time listing of the 10 most recent service appointments with status-based color badges.

### 2. Customers & Vehicles (`CustomerVehiclePanel.java`)
- **Registration Form**: Customer Name, 10-digit Phone, Indian Registration Number (e.g. `DL01AB1234`), Manufacturer Brand, Model Name, Category (`Car` or `Bike`).
- **Input Validation**: Strict validation via `Validator.java` preventing invalid names, malformed phones, or duplicate registration numbers.
- **Search & Filter**: Search vehicle records by registration plate, customer name, phone number, brand, or model.
- **Delete Protection**: Catches foreign key violations and displays clear warnings if a vehicle has active service appointments.

### 3. Service Booking & Polymorphic Labour Estimator (`ServiceBookingPanel.java`)
- **Vehicle Selection**: Dropdown displaying all registered vehicles.
- **Service Categories**: `General Service` (Rs. 1200 base), `Engine Repair` (Rs. 3500 base), `Oil Change` (Rs. 500 base), `Brake Inspection` (Rs. 800 base).
- **Live Polymorphic Estimator**: Changes dynamically as you switch vehicles or service categories. Four-wheelers (`Car`) multiply the base rate by 1.5x; two-wheelers (`Bike`) use 1.0x.
- **Unique Job ID Generation**: Atomically assigns a formatted Job ID (`JOB-2026-0001`) inside an atomic database transaction.

### 4. Job Card Tracking & Multithreaded Simulation (`JobCardPanel.java`)
- **Job Card Inspector**: Displays complete appointment details, polymorphic vehicle specification (`v.getVehicleDetails()`), customer contact info, and assigned spare parts.
- **Job ID Search**: Immediate lookup with custom `InvalidJobException` error handling.
- **Manual Status Updates**: Enforces status integrity (cannot move a `Completed` job back to `Pending` or `In Progress`).
- **Automated Service Simulation**:
  - Launches `JobProgressThread` in the background.
  - Updates progress through 4 realistic milestones: **Job Received -> Inspection -> Mechanical Repair -> Service Completed**.
  - Animated `JProgressBar` and stage description updating every 2 seconds.
  - Synchronized concurrency guard prevents starting duplicate simulations on the same job.
  - Safely communicates with the GUI using `SwingUtilities.invokeLater()`.

### 5. Spare Parts Inventory & Transactional Assignment (`PartsPanel.java`)
- **Part CRUD Form**: Part Name, Unit Price (in INR), and Stock Quantity.
- **Inventory Table**: Visual `LOW STOCK` badge and red highlighting for items below 5 units.
- **Transactional Job Assignment**:
  - Assigns spare parts to active service jobs.
  - Locks inventory rows with `SELECT ... FOR UPDATE`.
  - Blocks requests exceeding available stock: `"Insufficient stock. Available: X, requested: Y"`.
  - Atomically decrements stock and updates the `Job_Parts` junction table.

### 6. Service Billing & Tax Invoice (`BillingPanel.java`)
- **Completed Job Restriction**: Only jobs marked as `Completed` can be billed (throws `InvalidJobException` otherwise).
- **Accurate Financial Calculations**:
  $$\text{Labour Charges} + \sum (\text{Part Price} \times \text{Qty}) = \text{Subtotal}$$
  $$\text{GST} = \text{Round}(\text{Subtotal} \times 0.18)$$
  $$\text{Grand Total} = \text{Subtotal} + \text{GST}$$
- **Official Invoice Preview**: Monospaced tax invoice ready for viewing.
- **Save as Text File**: Interactive `JFileChooser` exports clean tax invoices (e.g. `Invoice_JOB-2026-0001.txt`).
- **Print Integration**: Native Java Print API support.

---

## 7. Troubleshooting Guide

| Issue / Error Message | Root Cause | Solution |
|---|---|---|
| `CommunicationsException: Communications link failure` | MySQL Server service is stopped or port 3306 is blocked. | Open Windows Services (`services.msc`), find **MySQL80**, and click **Start**. Verify MySQL is listening on port 3306. |
| `Access denied for user 'root'@'localhost'` | Password mismatch in `config/db.properties`. | Open `config/db.properties` and update `db.password` with your actual MySQL root password. |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL Connector JAR missing from classpath. | Verify `lib/mysql-connector-j-8.3.0.jar` exists. When compiling, include `-cp "lib/*"` and when running include `-cp "bin;lib/*"`. |
| `The server time zone value '...' is unrecognized` | MySQL server timezone issue. | Ensure the connection URL in `config/db.properties` includes `serverTimezone=Asia/Kolkata` (already configured by default). |
| `Unknown database 'vehicle_service_db'` | Database schema has not been imported. | Run `mysql -u root -p < sql/schema.sql` to initialize the database. |

---

## 8. Viva Voce Questions & Answers (OOPJ Exam Prep)

#### Q1: Why did you use an abstract class `Vehicle` instead of an interface?
> **Answer**: `Vehicle` represents a real-world entity with state (vehicle ID, registration number, brand, model, customer). An abstract class allows us to define private fields, common constructors, concrete getters/setters, and implement shared methods (`calculateLabourSurcharge`), while leaving vehicle-specific behaviour (`getLabourMultiplier`, `getVehicleDetails`) abstract for child classes to implement.

#### Q2: Where and how is runtime polymorphism demonstrated in your project?
> **Answer**: Runtime polymorphism is demonstrated in labour charge calculation. We hold references to `Vehicle v` (which can be `new Car(...)` or `new Bike(...)`). When calling `v.calculateLabourSurcharge(baseLabour)`, it dynamically invokes the overridden `getLabourMultiplier()` of `Car` (1.5x) or `Bike` (1.0x) based on the actual object created at runtime by the factory method.

#### Q3: Why implement `Runnable` instead of extending `Thread` for `JobProgressThread`?
> **Answer**: In Java, a class can only extend one superclass. Implementing `Runnable` preserves single inheritance, decouples the job simulation logic from thread execution mechanics, and makes the task compatible with thread pools and executor frameworks.

#### Q4: Why must GUI updates from a background thread go through `SwingUtilities.invokeLater()`?
> **Answer**: Java Swing components are not thread-safe and must only be accessed or modified from the Swing Event Dispatch Thread (EDT). Calling `SwingUtilities.invokeLater()` places the GUI update task into the EDT event queue, preventing race conditions, UI freezing, and unpredictable graphics glitches.

#### Q5: How did you ensure database transaction atomicity when assigning spare parts?
> **Answer**: In `JobPartsDAO.java`, we call `conn.setAutoCommit(false)`. We lock the part inventory record using `SELECT ... FOR UPDATE` to avoid dirty reads. If stock is sufficient, we update `Job_Parts` and deduct stock in `Parts`, then call `conn.commit()`. If an exception occurs, `conn.rollback()` is invoked in the catch block to ensure the database remains consistent.

#### Q6: What is the purpose of the custom exception `InvalidJobException`?
> **Answer**: It represents business rule violations specific to vehicle servicing—such as searching for a non-existent Job ID, attempting to bill a job that has not been completed, or performing illegal status transitions. This separates domain-specific errors from generic technical exceptions like `SQLException`.

#### Q7: How does your application prevent duplicate Indian vehicle registration numbers?
> **Answer**: At the database level, `registration_number` has a `UNIQUE` constraint. At the application level, `Validator.java` normalizes and verifies the registration pattern using regular expressions (`^[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{4}$`), and `VehicleDAO.java` checks for duplicate entries before executing queries.

#### Q8: What design pattern did you use to switch between screens in the GUI?
> **Answer**: We used Swing's `CardLayout` inside `MainFrame.java`. All panel instances (`DashboardPanel`, `CustomerVehiclePanel`, etc.) are mounted inside a card container, and sidebar button click events switch screens instantly without creating and destroying frames.

---

## 9. License & Credits

- Developed for: **B.Tech CSE 2nd Year - Object-Oriented Programming with Java (OOPJ)**
- Academic Year: **2026**
- Standard: Production-grade Java Swing & JDBC Architecture
