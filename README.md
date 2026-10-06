# ⚡ Smart Digital Queue Management System (Enterprise Edition)

A full-stack, enterprise-grade Queue Management System in **Java** featuring a **Java Swing GUI Frontend**, **JDBC & SQL Database Backend** (with SQLite zero-config out of the box and optional MySQL support), and persistent state synchronization with Java Data Structures (`PriorityQueue`, `LinkedList`, and `ArrayList`).

---

## 🌟 Key Features

### 🖥️ Front-End (Java Swing GUI)
* **Live Calling Station & Public Display**:
  * Large, digital display monitor showing "NOW SERVING" with token number, customer name, and counter name.
  * Counter Staff Controls: Call Next Customer, Re-Announce (chime bell), Complete Service, Skip/Cancel.
  * Audio chime / notification upon calling tickets.
  * Real-time preview of upcoming customers in line (VIP priority highlighted).
* **Token Dispenser & Customer Registration**:
  * Form for customer registration: Full Name, Age, Phone, Service Type, Priority Flag (VIP / Senior Citizen / Special Assistance).
  * Fast Token Issuance for existing registered customers.
  * **Digital Token Receipt Slip Popup** with estimated wait time and printable receipt view.
* **Real-Time Queue Monitor**:
  * Live filterable table: All Tickets, Waiting, Serving, Completed.
  * Custom colored badges: VIP Priority (Gold/Amber), Standard, Status (Waiting, Serving, Completed).
  * Direct table actions: Call Selected, Complete Selected, Cancel Ticket.
* **Customer Directory**:
  * Search customers in real time by ID, Name, or Phone.
  * View complete customer profile and past queue history.
* **System Analytics & Database Tools**:
  * Real-time KPI summary cards (Total Customers, Waiting Normal, Waiting Priority, Serving, Completed, Est. Wait).
  * **1-Click Demo Data Generator**: Seeds realistic bank/hospital queue records.
  * **CSV Export**: Export queue tickets and customer logs into a `.csv` file.
  * **Queue Reset**: Reset tickets at the end of the day.

---

### 🗄️ Backend (JDBC & SQL Database)
* **Zero-Setup SQLite Out-of-the-Box**: Database file `queue_system.db` is created and managed automatically without needing an external database server.
* **Configurable MySQL Support**: Simply configure `db.properties` to connect to a local or remote MySQL server.
* **DAO (Data Access Object) Pattern**:
  * [`CustomerDAO`](file:///Users/siddarthvijayanand/Downloads/java%20project/dao/CustomerDAO.java): Prepares and executes SQL queries for customers (Insert, Update, Delete, Find, Search, Auto-sequence).
  * [`TicketDAO`](file:///Users/siddarthvijayanand/Downloads/java%20project/dao/TicketDAO.java): Handles ticket generation, priority-ordered queries, counter assignment, status updates, and SQL aggregation statistics.
* **Security & Reliability**: Uses `PreparedStatement` everywhere to prevent SQL injection vulnerabilities and `try-with-resources` for connection leak prevention.

---

### 🧱 Core OOP Principles & Data Structures
* **Abstract Class**: [`Person`](file:///Users/siddarthvijayanand/Downloads/java%20project/model/Person.java) encapsulates base attributes (`name`, `age`, `phone`) and abstract method `displayDetails()`.
* **Inheritance**: [`Customer`](file:///Users/siddarthvijayanand/Downloads/java%20project/model/Customer.java) extends [`Person`](file:///Users/siddarthvijayanand/Downloads/java%20project/model/Person.java) with specialized customer attributes.
* **Polymorphism**: Overrides `displayDetails()` to format details specific to queue customers.
* **Encapsulation**: Private and protected fields accessible only through public accessors/mutators.
* **Comparable Interface**: Implemented by `Customer` and `QueueTicket` to prioritize VIP customers ahead of standard customers.
* **Data Structures**:
  * `PriorityQueue<Customer>`: O(log n) VIP queue prioritization.
  * `Queue<Customer>` (`LinkedList`): FIFO queue for regular tickets.
  * `ArrayList<Customer>`: In-memory cache synced with the SQL database.

---

## 📁 Project Structure

```
├── App.java                     # Master application launcher (GUI default, --cli flag support)
├── Customer.java                # Root Customer & Person classes (maintains backward compatibility)
├── QueueManager.java            # Root QueueManager with JDBC DAO synchronization
├── main.java                    # Terminal Interactive CLI Console application
├── compile.sh                   # Compilation script
├── run.sh                       # Application execution script
├── db.properties.example        # Database configuration template (SQLite / MySQL)
├── queue_system.db              # SQLite Database file (generated automatically)
│
├── db/
│   └── DatabaseManager.java     # Singleton JDBC connection manager & table schema creator
│
├── model/
│   ├── Person.java              # Abstract Person class
│   ├── Customer.java            # Customer entity extending Person & Comparable
│   ├── QueueTicket.java         # QueueTicket model with status lifecycle
│   └── QueueStatistics.java     # Metrics and aggregate statistics model
│
├── dao/
│   ├── CustomerDAO.java         # JDBC SQL operations for Customer
│   └── TicketDAO.java           # JDBC SQL operations for Queue Tickets
│
├── service/
│   └── QueueService.java        # Business logic layer bridging Data Structures & SQL DAOs
│
├── ui/
│   ├── ThemeUtils.java          # FlatLaf styling, color palette, custom renderers
│   ├── TokenSlipDialog.java     # Digital token slip receipt modal dialog
│   └── QueueDashboardFrame.java # Master Java Swing Graphical Dashboard
│
└── lib/                         # Included self-contained libraries
    ├── flatlaf.jar              # Modern Swing theme engine
    ├── sqlite-jdbc.jar          # SQLite JDBC Driver
    ├── mysql-connector-j.jar    # MySQL JDBC Driver
    ├── slf4j-api.jar            # Logging API
    └── slf4j-simple.jar         # Logging implementation
```

---

## 🗃️ Database Schema

### 1. `customers` Table
| Column | Type | Description |
|---|---|---|
| `customer_id` | `VARCHAR(20)` | Primary Key (e.g., `C1001`, `C1002`) |
| `name` | `VARCHAR(100)` | Customer Full Name |
| `age` | `INTEGER` | Customer Age |
| `phone` | `VARCHAR(20)` | Phone Number |
| `service_type` | `VARCHAR(50)` | Requested Service |
| `is_priority` | `INTEGER` | 1 = VIP / Priority, 0 = Standard |
| `token_number` | `VARCHAR(20)` | Active token number |
| `created_at` | `VARCHAR(30)` | Registration Timestamp |

### 2. `queue_tickets` Table
| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER` | Auto-incrementing Ticket ID |
| `token_number` | `VARCHAR(20)` | Token code (e.g. `P101` for priority, `T101` for normal) |
| `customer_id` | `VARCHAR(20)` | Foreign key reference to customer |
| `customer_name` | `VARCHAR(100)` | Cached customer name |
| `customer_phone` | `VARCHAR(20)` | Phone number |
| `service_type` | `VARCHAR(50)` | Service desk type |
| `is_priority` | `INTEGER` | Priority status flag |
| `status` | `VARCHAR(20)` | `WAITING`, `SERVING`, `COMPLETED`, `CANCELLED` |
| `counter_assigned`| `VARCHAR(50)` | Name of operating counter |
| `issued_at` | `VARCHAR(30)` | Timestamp of token issuance |
| `served_at` | `VARCHAR(30)` | Timestamp when called to counter |
| `completed_at` | `VARCHAR(30)` | Timestamp when service completed |

### 3. `service_counters` Table
| Column | Type | Description |
|---|---|---|
| `counter_id` | `INTEGER` | Primary Key (1, 2, 3, 4) |
| `name` | `VARCHAR(50)` | Counter title (e.g. `Counter 1 - General Banking`) |
| `status` | `VARCHAR(20)` | Counter state (`AVAILABLE`, `BUSY`, `CLOSED`) |

---

## 🚀 How to Run

### 1. Run the Java Swing GUI (Recommended)
Using the runner script:
```bash
./run.sh
```
Or directly using Java:
```bash
java -cp "lib/*:." App
```

### 2. Run the Interactive CLI Console Mode
Using the runner script:
```bash
./run.sh --cli
```
Or directly:
```bash
java -cp "lib/*:." main
```

### 3. Recompile Project
```bash
./compile.sh
```

---

## ⚙️ Switching from SQLite to MySQL (Optional)
If you wish to use a local or remote MySQL database instead of the built-in SQLite:
1. Copy `db.properties.example` to `db.properties`:
   ```bash
   cp db.properties.example db.properties
   ```
2. Edit `db.properties`:
   ```properties
   db.type=mysql
   mysql.host=localhost
   mysql.port=3306
   mysql.database=queue_system
   mysql.user=root
   mysql.password=your_password
   ```
3. Restart the application. The system will automatically create tables in MySQL!
