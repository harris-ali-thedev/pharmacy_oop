# 💊 Pharmacy Management System (PMS)
## OOP Semester Project — Java Swing + File Handling

---

## 📋 Project Overview

A full-featured desktop Pharmacy Management System built with **Java 21 + Swing GUI + File Handling**, demonstrating comprehensive Object-Oriented Programming concepts and Java Generics.

---

## 🎓 OOP Concepts Demonstrated

| Concept | Where Used |
|---------|-----------|
| **Abstraction** | `Entity` (abstract class), `Repository<T,ID>` (interface), `FileRepository<T>` (abstract) |
| **Encapsulation** | All model classes (`User`, `Medicine`, `Patient`, `Sale`, etc.) — private fields + getters/setters |
| **Inheritance** | `User`, `Medicine`, `Patient`, `Supplier`, `Sale`, `SaleItem` all extend `Entity` |
| **Polymorphism** | `Searchable.matches()`, `Deactivatable.deactivate()`, `Entity.getDisplayName()` — overridden in each subclass |
| **Interfaces** | `Repository<T,ID>`, `SearchableRepository<T>`, `Deactivatable`, `Searchable` |
| **Generics** | `FileRepository<T>`, `Repository<T,ID>`, `Result<T>`, `BoundedStack<T>`, `Pair<A,B>` |
| **Singleton** | `SessionManager`, `UserDAO`, `MedicineDAO`, `SupplierDAO`, `PatientDAO`, `SaleDAO`, all service classes |
| **Composition** | `Sale` contains `List<SaleItem>`; `POSService` uses `MedicineDAO` + `SaleDAO` |
| **Template Method** | `FileRepository.save()` calls abstract `getArrayClass()` in subclasses |
| **Enum** | `Role`, `Sale.PaymentMethod`, `Sale.Status` |
| **File Handling** | `FileStore` serializes all data to binary `.dat` files in the `data/` directory |

---

## 📁 Project Structure

```
PharmacyPMS/
├── src/
│   └── com/pharmacy/
│       ├── model/               ← Domain entities (OOP + Generics)
│       │   ├── Entity.java          Abstract base class (Abstraction)
│       │   ├── Deactivatable.java   Soft-delete interface
│       │   ├── Searchable.java      Keyword search interface
│       │   ├── Role.java            Enum for user roles
│       │   ├── User.java            Extends Entity, implements Deactivatable+Searchable
│       │   ├── Medicine.java        Extends Entity, implements Deactivatable+Searchable
│       │   ├── Supplier.java        Extends Entity, implements Deactivatable+Searchable
│       │   ├── Patient.java         Extends Entity, implements Deactivatable+Searchable
│       │   ├── Sale.java            Extends Entity — transaction record
│       │   ├── SaleItem.java        Extends Entity — line item
│       │   └── CartItem.java        Transient POS cart item (Composition)
│       │
│       ├── dao/                 ← Data Access Objects (Generics + File Handling)
│       │   ├── Repository.java      Generic CRUD interface <T extends Entity, ID>
│       │   ├── SearchableRepository.java  Extends Repository with search()
│       │   ├── FileRepository.java  Abstract generic file-backed repo
│       │   ├── UserDAO.java         Extends FileRepository<User>
│       │   ├── MedicineDAO.java     Extends FileRepository<Medicine>
│       │   ├── SupplierDAO.java     Extends FileRepository<Supplier>
│       │   ├── PatientDAO.java      Extends FileRepository<Patient>
│       │   └── SaleDAO.java         Extends FileRepository<Sale>
│       │
│       ├── service/             ← Business logic layer
│       │   ├── UserService.java     User authentication + management
│       │   ├── InventoryService.java Medicine + Supplier management
│       │   ├── PatientService.java  Patient CRUD
│       │   └── POSService.java      Checkout workflow + cart management
│       │
│       ├── generics/            ← Generic utility classes (pure Generics showcase)
│       │   ├── Result.java          Generic Result<T> wrapper (success/failure)
│       │   ├── BoundedStack.java    Generic bounded stack implementation
│       │   └── Pair.java            Generic two-element tuple Pair<A,B>
│       │
│       ├── util/                ← Utility classes
│       │   ├── FileStore.java       Binary file serialization (I/O)
│       │   ├── PasswordUtil.java    SHA-256 password hashing
│       │   ├── SessionManager.java  Singleton session state
│       │   └── AuditLogger.java     Append-only text audit log
│       │
│       └── ui/                  ← Swing GUI (MVC View layer)
│           ├── PharmacyApp.java     Entry point + L&F setup
│           ├── SwingUtils.java      Shared Swing helpers, colors, fonts
│           ├── LoginPanel.java      Login screen
│           ├── MainFrame.java       Main window + sidebar navigation
│           ├── DashboardPanel.java  KPI cards + recent sales + alerts
│           ├── POSPanel.java        Point-of-Sale / checkout
│           ├── InventoryPanel.java  Medicine CRUD + stock management
│           ├── PatientPanel.java    Patient registration + search
│           ├── SupplierPanel.java   Supplier management
│           ├── SalesReportPanel.java Sales analytics + reports
│           ├── UserManagementPanel.java Admin user management
│           └── AuditLogPanel.java   Audit log viewer
│
├── data/                        ← Auto-created at runtime (file storage)
│   ├── users.dat                    Serialized user objects
│   ├── medicines.dat                Serialized medicine objects
│   ├── suppliers.dat                Serialized supplier objects
│   ├── patients.dat                 Serialized patient objects
│   ├── sales.dat                    Serialized sale objects
│   └── audit.log                    Plain-text audit trail
│
├── bin/                         ← Compiled .class files
├── PharmacyPMS.jar              ← Runnable JAR
├── build.sh                     ← Build script
└── README.md
```

---

## 🚀 Running the Application

### Option 1 — Run the JAR directly

```bash
java -jar PharmacyPMS.jar
```

### Option 2 — Compile and run from source

```bash
# Compile
bash build.sh

# Run
java -cp bin com.pharmacy.ui.PharmacyApp
```

---

## 👥 Default Accounts

| Username     | Password    | Role        |
|-------------|-------------|-------------|
| `admin`      | `admin123`  | Administrator |
| `pharmacist` | `pharma123` | Pharmacist   |
| `cashier`    | `cashier123`| Cashier      |

---

## 🧩 Modules

### 📊 Dashboard
- Today's revenue, transaction count, low-stock alerts
- Recent 15 sales table
- Low-stock medicine alert table

### 🛒 Point of Sale (POS)
- Medicine search by name/category/barcode
- Cart management with quantity editing
- Patient selection (optional)
- Discount % and Tax % with role-based limits
- Cash/Card/Digital Wallet payment modes
- Change calculator
- In-window receipt generation

### 📦 Inventory Management
- Full CRUD for medicines
- Add stock (quantity + batch note)
- Low-stock filter view
- Supplier linkage

### 👤 Patients
- Full CRUD for patient records
- Allergy tracking
- Loyalty points display
- Search by name, phone, or NIC

### 🚚 Suppliers
- Supplier CRUD with contact details
- Lead time tracking

### 📈 Sales Reports
- Date range filtering
- Revenue, transaction count, average sale KPIs
- Recent 50 sales with double-click detail view
- Top 10 medicines by quantity sold

### 👥 User Management *(Admin only)*
- Create/edit/deactivate user accounts
- Role assignment (Admin/Pharmacist/Cashier)
- Password reset

### 📝 Audit Log *(Admin only)*
- All system events recorded to file
- Filterable keyword search

---

## 💾 Data Storage (File Handling)

All data is persisted to the `data/` directory using Java Object Serialization:

- **Binary `.dat` files** — store entity collections (users, medicines, etc.)
- **`audit.log`** — plain-text append-only event log

The `FileStore` utility class handles all I/O:
```java
FileStore.save("medicines", collection);   // → data/medicines.dat
List<?> data = FileStore.load("medicines"); // ← data/medicines.dat
FileStore.appendLine("audit", line);       // → data/audit.log
```

---

## 🔐 Security

- Passwords hashed with SHA-256 + salt (no plain-text storage)
- Role-based access control enforced at service layer
- Account lockout after 5 failed login attempts
- All actions recorded in the audit log

---

## 📦 Dependencies

**Zero external dependencies.** Only standard Java SE 21 libraries:
- `java.io` / `java.nio` — file handling
- `java.security` — password hashing
- `javax.swing` / `java.awt` — GUI
- `java.util` — collections, generics
- `java.time` — date/time
