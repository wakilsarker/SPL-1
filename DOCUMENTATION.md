# CarePlus — Console Hospital Management System (Full Documentation)

This document explains the entire codebase so you can understand it easily before a viva or while extending it. It covers what the project does, how it is organised, how each file works, how the data is stored, and how the pieces fit together.

---

## 1. At a Glance

- **What it is:** A terminal-based (console) Hospital Management System.
- **Project type:** SPL-1 mini project. No database, no Maven, no framework, no external library.
- **Language:** Core Java only (requires any JDK, e.g. Java 8+).
- **Storage:** Plain-text CSV files inside the `data/` folder.
- **Build:** The project is compiled straight from the `src/` folder with `javac`.

Console output style, menus, and data all come from the `src/hospital` packages.

---

## 2. How to Build and Run

From the project folder:

```bash
find src -name "*.java" -print | xargs javac -d bin
java -cp bin hospital.Main
```

- `javac -d bin` compiles every `.java` file under `src/` into the `bin/` folder.
- `hospital.Main` is the class with the `main` method — running it starts the menu loop.
- All persistent data lives in `data/*.csv`. If a file is missing, the program creates it automatically (empty).

---

## 3. Project Structure

```
CarePlus/
├── data/                         # CSV files (the "database")
│   ├── patients.csv  doctors.csv  appointments.csv  medical_records.csv
│   ├── medical_record_history.csv lab_tests.csv    lab_results.csv
│   ├── medicines.csv prescriptions.csv beds.csv     bills.csv
│   ├── expenses.csv   revenues.csv  blood_donors.csv
│
├── src/hospital/
│   ├── Main.java                 # entry point
│   ├── menu/
│   │   └── MainMenu.java         # all menus and navigation
│   ├── model/                    # small domain classes
│   │   ├── Patient.java  Doctor.java  Appointment.java
│   ├── util/                     # reusable helpers
│   │   ├── Input.java  CsvUtil.java  IdGenerator.java
│   └── service/                  # application logic (one service per module)
│       ├── TableService.java       # reusable CRUD base class
│       ├── PatientService.java  DoctorService.java
│       ├── AppointmentService.java  MedicalRecordService.java
│       ├── LaboratoryService.java    PharmacyService.java
│       ├── PrescriptionService.java  BedService.java
│       ├── BillingService.java     AccountingService.java
│       ├── BloodBankService.java   ReportService.java
```

The idea behind the layout:

| Package | Responsibility |
|---|---|
| `hospital.menu` | Terminal menus and navigation only (no business logic). |
| `hospital.service` | Application logic + all CSV read/write operations. |
| `hospital.model` | Plain domain classes representing real-world things. |
| `hospital.util` | Generic reusable utilities (input, CSV, ID generation). |

---

## 4. How the Program Flow Works

```
User runs java -cp bin hospital.Main
        │
        ▼
Main.main()  ──►  new MainMenu().start()
                        │
                        ▼
              MainMenu.start() — infinite loop:
              └─ shows main menu (12 modules)
                 └─ Input.number() reads the choice
                    └─ switch calls the matching submenu (e.g. patientMenu())
                       └─ submenu loop shows options and calls services
```

Every module is a submenu (`patientMenu()`, `doctorMenu()`, `appointmentMenu()`, …). Each submenu is a `while(true)` loop with a `switch`; option `0` returns to the previous menu, and the main menu option `0` exits the program.

### The main menu (choice numbers)

| # | Module | Menu class method |
|---|---|---|
| 1 | Patient Management | `patientMenu()` |
| 2 | Doctor Management | `doctorMenu()` |
| 3 | Appointment Management | `appointmentMenu()` |
| 4 | Medical Records | `recordMenu()` |
| 5 | Prescription Management | `prescriptionMenu()` |
| 6 | Laboratory Management | `labMenu()` |
| 7 | Pharmacy Management | `pharmacyMenu()` |
| 8 | Bed Management | `bedMenu()` |
| 9 | Billing & Payment | `billingMenu()` |
| 10 | Accounting | `accountingMenu()` |
| 11 | Blood Donor Management | `donorMenu()` |
| 12 | Reports | `reports.show()` |
| 0 | Exit | — |

### How services are wired together

All services are created **once** in `MainMenu` (fields) and passed to the services that need them through their constructors:

```java
Patients   → used by appointment, records, lab, prescription, bed, billing, reports
Doctors    → used by appointment, records, prescription, reports
Pharmacy   → used by prescription (to look up medicine names/prices)
Records    → used by prescription (to auto-create a health report)
```

Example wiring line from `MainMenu`:
```java
private final AppointmentService appointments = new AppointmentService(patients, doctors);
```
This construction-injection pattern is why the program stays easy to understand: each service declares exactly what it depends on.

---

## 5. The `util` Package (Reusable Helpers)

### 5.1 `Input.java` — getting user input safely

| Method | What it does |
|---|---|
| `text(label)` | Prints the label, reads a whole line, trims it. |
| `number(label)` | Reads an **integer**. Loops until the user types a valid whole number. |
| `amount(label)` | Reads a **double**. Loops until the user types a valid number. |
| `pause()` | Waits for the user to press Enter ("Press Enter to continue..."). |

- It uses one shared static `Scanner`.
- The constructor is private → it is a utility class, used as `Input.text(...)`, never `new Input()`.

### 5.2 `CsvUtil.java` — the tiny CSV "database engine"

This is the core storage mechanic. Everything the program saves/loads goes through this class.

| Method | What it does |
|---|---|
| `read(file)` | Reads all rows of a CSV file into a `List<String[]>`. Blank lines are skipped. |
| `append(file, row)` | Appends **one** row to the end of a CSV file. |
| `writeAll(file, rows)` | Rewrites the whole file from a list of rows (used for update/delete). |
| `ensureFile(file)` | Creates the file (and any parent folders) if it does not exist yet. |

CSV format details (important for understanding the data files):

- Every value is stored **wrapped in double quotes**: `"A001","P001","D001",...`
- Values separated by commas.
- If a value itself contains a double quote, it is escaped by doubling it (`""`).
- The `join()` method builds a line; `parse()` is a small state machine that reads a line back into fields, correctly handling quoted commas and escaped quotes.

That is the whole "database": a folder of CSV files + a generic reader/writer. Nothing else is used for persistence.

### 5.3 `IdGenerator.java` — automatic ID generation

```java
static String next(String file, String prefix)
```

- Scans the given CSV file, finds all IDs that **start with the prefix**,
- takes the **highest number** found at the end of the ID,
- returns `prefix` + that number + 1, zero-padded to 3 digits.

Example: if `data/patients.csv` contains up to `P100`, the next patient gets `P101`.

Because it scans the file every time, IDs stay unique even after restarts and even if rows are deleted (it reuses the highest number, not the count).

### 5.4 ID prefixes used in this project

| File | Prefix | Example |
|---|---|---|
| patients.csv | `P` | P001 |
| doctors.csv | `D` | D001 |
| appointments.csv | `A` | A001 |
| lab_tests.csv | `T` | T001 |
| lab_results.csv | `L` | L001 |
| medical_records.csv | `R` | R001 |
| medical_record_history.csv | `HRH` | HRH001 |
| medicines.csv | `M` | M001 |
| prescriptions.csv | `PR` | PR001 |
| beds.csv | `B` | B001 |
| bills.csv | `BL` | BL001 |
| expenses.csv | `E` | E001 |
| revenues.csv | `RV` | RV001 |
| blood_donors.csv | `BD` | BD001 |

---

## 6. The `model` Package (Domain Classes)

Three simple classes represent real-world things. They are not connected to storage — the services turn them to/from CSV rows.

| Class | Fields |
|---|---|
| `Patient` | id, name, age, gender, bloodGroup, phone, address |
| `Doctor` | id, name, specialization, phone |
| `Appointment` | id, patientId, doctorId, date, time, status |

Each model class has three useful methods:

- `toCsv()` — returns the object as a `String[]` ready to be appended to a CSV file.
- `from(String[] r)` — a static factory that builds an object from a CSV row.
- `toString()` — a readable one-line display used when printing.

---

## 7. The `service` Package (Application Logic)

### 7.1 `TableService.java` — the reusable CRUD base class

Many modules are small CRUD (Create/Read/Update/Delete) tables. Instead of repeating code, they **extend** `TableService`. Its constructor takes:

```java
TableService(file, idPrefix, displayTitle, fieldNames...)
```

For example, medicine (Pharmacy):
```java
super("data/medicines.csv", "M", "Medicine", "Name", "Price", "Quantity");
```
means: file = medicines.csv, IDs start with M, title = "Medicine", and the columns after the ID are Name / Price / Quantity.

What the base class provides:

| Method | What it does |
|---|---|
| `rows()` | Returns every CSV row as `List<String[]>` (just calls `CsvUtil.read`). |
| `add()` | Generates an ID, asks for each field via `Input`, appends the row. |
| `view()` | Prints all rows. |
| `search()` | Asks for a search term, prints rows whose content contains it (case-insensitive). |
| `delete()` | Asks for an ID, rewrites the file without that row. |
| `print(list)` / `format(row)` | Reusable printing; format prints `ID: ... | Field1: ... | Field2: ...`. |

It also stores `file`, `prefix`, `title`, `fields` as `protected` so subclasses can reuse them.

### 7.2 Which services extend `TableService`

| Service | File | Prefix | Extra methods beyond CRUD |
|---|---|---|---|
| `PharmacyService` | medicines.csv | M | `findMedicine(id)` – returns the row or null; `dispense()` – reduces stock; `updateStock()` – sets stock |
| `BedService` | beds.csv | B | `assign()` – marks bed Occupied + links a patient; `release()` – frees the bed; `available()` – lists Available beds |
| `BillingService` | bills.csv | BL | `receivePayment()` – record a payment; `totalDue()` – sum of unpaid amounts |
| `BloodBankService` | blood_donors.csv | BD | `findAvailable()` – donors of a blood group that are available (`true`) |
| `MedicalRecordService` | medical_records.csv | R | `createReport()` – save report + first history snapshot; `update()` – versioned update; `patientHistory()` – version history of a patient |
| `PrescriptionService` | prescriptions.csv | PR | overrides `add()` to run `makePrescriptionAndHealthReport()` |

### 7.3 Services that do NOT extend `TableService`

| Service | Files it uses | Notes |
|---|---|---|
| `PatientService` | patients.csv | Uses the `Patient` model + its own CRUD. Also `all()`, `find(id)`, `exists(id)`. |
| `DoctorService` | doctors.csv | Same pattern as PatientService but for `Doctor`. |
| `AppointmentService` | appointments.csv | `schedule()`, `view()`, `complete()`, `cancel()`. Validates IDs and prevents active doctor/patient time-slot conflicts. |
| `LaboratoryService` | lab_tests.csv, lab_results.csv | Wraps **two** `TableService` instances (tests `T` and results `L`). |
| `AccountingService` | expenses.csv, revenues.csv | `addExpense/addRevenue/view*` + `summary()` (revenue − expense) + total sums. |
| `ReportService` | reads many files | Builds an overview report (totals, beds, money). |

---

## 8. Data Storage — CSV Reference (Data Dictionary)

All files live in `data/`. Relationships are maintained by **ID references**, e.g. an appointment row stores the patient ID `P001` and doctor ID `D001`.

### patients.csv
`ID(P), Name, Age, Gender, BloodGroup, Phone, Address`

### doctors.csv
`ID(D), Name, Specialization, Phone`

### appointments.csv
`ID(A), PatientID(P), DoctorID(D), Date, Time, Status`
- Status values used by the program: `Scheduled` / `Completed` / `Cancelled`.

### medical_records.csv  (current/active health report)
`ID(R), PatientID(P), DoctorID(D), Date, Diagnosis, Treatment, Notes`

### medical_record_history.csv  (version history of reports)
`ID(HRH), RecordID(R), Version, Action, PatientID(P), DoctorID(D), Date, Diagnosis, Treatment, Notes`
- One row is written each time a report is created (`Version 1, Created`) or updated (`Updated`).
- This is how old versions are never lost.

### lab_tests.csv
`ID(T), TestName, Price`

### lab_results.csv
`ID(L), PatientID(P), TestID(T), Date, Result, Status`

### medicines.csv
`ID(M), Name, Price, Quantity`
- Price is a text number (e.g. `2.50`), Quantity an integer (stock).

### prescriptions.csv
`ID(PR), PatientID(P), DoctorID(D), Date, MedicineID(M), Medicine, Dosage, Instructions`
- One line per medicine; one consultation can produce several lines.

### beds.csv
`ID(B), Ward, BedNumber, Status, PatientID(P)`
- Status: `Occupied` / `Available`. For Available beds the patient column is `-`.

### bills.csv
`ID(BL), PatientID(P), Date, Description, Amount, Paid, Status`
- Status is derived: `Paid` / `Partial` / `Unpaid`.

### expenses.csv
`ID(E), Date, Category, Description, Amount`

### revenues.csv
`ID(RV), Date, Source, Amount`

### blood_donors.csv
`ID(BD), Name, BloodGroup, Age, Phone, Address, Available(true/false)`

---

## 9. Key Behaviours Explained

### 9.1 Referential checks before saving
Several services verify an ID exists before creating a dependent record:
- `AppointmentService.schedule()` — checks patient and doctor exist, then rejects a matching active date/time for either the doctor or patient.
- `LaboratoryService.addResult()` — checks patient and test exist.
- `MedicalRecordService.add()` — checks patient and doctor exist.
- `PrescriptionService` — checks patient, doctor, and each medicine ID.
- `BedService.assign()` — checks the patient exists and the bed is currently `Available`.

Pattern used everywhere:
```java
if (!patients.exists(patient) || !doctors.exists(doctor)) { ... return; }
```

### 9.2 Update/Delete pattern (used by nearly every service)
Follow this pattern to understand any update or delete:

1. Ask for the ID.
2. Build the new list of rows:
   - for update: replace the matching row with a new one, keep all others;
   - for delete: keep all rows except the matching one.
3. Call `CsvUtil.writeAll(file, rows)` (rewrites the file).
4. Print a success/not-found message.

Here is the delete example from `PatientService`:
```java
for(Patient p: all())
    if(p.getId().equalsIgnoreCase(id)) removed = true;      // found → drop it
    else rows.add(p.toCsv());                                // keep others
CsvUtil.writeAll(FILE, rows);
```

### 9.3 Health report versioning (MedicalRecordService)

- `createReport(...)`:
  1. Generates `R###` ID.
  2. Appends the report row to `medical_records.csv`.
  3. Appends a history snapshot `HRH###` with `Version 1`, action `Created`.
- `update(...)`:
  1. Finds the report by ID, overwrites its row in `medical_records.csv`.
  2. Computes the next version number and appends a new history row with action `Updated`.
- `patientHistory()` prints the full version trail: `Record | Version | Action | Patient | Doctor | Date | Diagnosis | Treatment | Notes`.
- Older records written before history tracking are still shown, marked with action `Existing`.

This is the closest thing to a "transaction/audit log" in the project.

### 9.4 Prescription + health report in one consultation

`PrescriptionService.makePrescriptionAndHealthReport()`:
1. Reads patient, doctor, date, diagnosis, clinical notes.
2. Reads one or more medicine IDs; each is looked up in the pharmacy (`findMedicine`) to get its name.
3. Builds a `treatment` string like `"Paracetamol 500 mg (1+1+1); Aspirin 75 mg (0+0+1)"`.
4. Calls `records.createReport(...)` → creates the health report **and** its history row automatically.
5. Appends one `PR###` prescription line per medicine.
6. Prints the report ID so the user can find it later.

### 9.5 Pharmacy stock (PharmacyService)
- `dispense()`: takes a medicine ID + quantity. Validates `sold > 0` and `sold <= stock`, then rewrites the row with reduced stock.
- `updateStock()`: sets quantity to a new value.
- `findMedicine(id)`: returns the full row `[ID, Name, Price, Quantity]` or `null` (used by prescriptions).

### 9.6 Billing (BillingService)
- When creating a bill, status is decided automatically:
  - paid `0` → `Unpaid`
  - paid == amount → `Paid`
  - otherwise → `Partial`
- `receivePayment()`: adds a payment; refuses payments ≤ 0 or that would exceed the bill's amount; if `paid` reaches `amount`, status becomes `Paid`.
- `totalDue()`: sums `amount − paid` over all bills → used by the report as "Outstanding Bills".

### 9.7 Accounting (AccountingService)
- Expenses: 5 columns, amount at index 4. Revenue: 4 columns, amount at index 3.
- `summary()` prints total revenue, total expense, and **net = revenue − expense** (formatted with `%.2f`).

### 9.8 Reports (ReportService)
`show()` reads multiple files and prints:
- Total patients, total doctors, total appointments,
- Occupied and available beds (counted from beds.csv status),
- Total revenue, total expenses, net revenue (`revenue − expenses`),
- Outstanding bills (from `BillingService.totalDue()`).

---

## 10. Menu-by-Menu Operation Guide

### 10.1 Patient Management
1. Register Patient — creates `P###` (validates age as a number).
2. View All Patients — prints every patient.
3. Search Patient — substring match on ID or name.
4. Update Patient — find by ID, re-enter every field, rewrite file.
5. Delete Patient — remove by ID.
6. View Patient History — prints all health-report versions for the patient.

### 10.2 Doctor Management
Add / View / Search (same as patients) / Update / Delete.

### 10.3 Appointment Management
1. Schedule Appointment — validates patient + doctor, rejects an active doctor/patient time conflict, then stores `Scheduled`.
2. View Appointments.
3. Mark Completed — changes status to `Completed`.
4. Cancel Appointment — changes status to `Cancelled`.

### 10.4 Medical Records
1. Add Health Report — validates patient + doctor, creates report + history version 1.
2. View All Reports.
3. Search Reports.
4. View Patient Report History — the version trail.
5. Update Health Report — creates a new history version automatically.

### 10.5 Prescription Management
1. Make Prescription & Health Report — combined flow (see 9.4).
2. View Prescriptions / 3. Search Prescriptions / 4. View Patient Report History.

### 10.6 Laboratory
1. Add Lab Test (`T###`) · 2. View Lab Tests.
3. Enter Lab Result (`L###`) — patient + test must exist, status set to `Completed` automatically (unless edited manually in the CSV).
4. View Lab Results.

### 10.7 Pharmacy
1. Add Medicine (`M###`) · 2. View · 3. Search.
4. Update Stock · 5. Dispense Medicine (reduces stock).

### 10.8 Bed Management
1. Add Bed (`B###`) · 2. View Beds.
3. Assign Bed — avoids assigning an occupied bed.
4. Release Bed — frees the bed.
5. Search Available Beds — lists only `Available` beds.

### 10.9 Billing & Payment
1. Create Bill (auto status) · 2. View · 3. Search · 4. Receive Payment.

### 10.10 Accounting
1. Add Expense · 2. View Expenses · 3. Add Revenue · 4. View Revenues · 5. Financial Summary.

### 10.11 Blood Donor Management
1. Add Donor · 2. View · 3. Search · 4. Find Available Donor by Group (filters by blood group AND `Available=true`).

### 10.12 Reports
Prints the full hospital overview described in 9.8.

---

## 11. Where Is Each Piece of Code?

Quick lookup table for a viva.

| Task / Concept | Location |
|---|---|
| Entry point | `src/hospital/Main.java` |
| All menus | `src/hospital/menu/MainMenu.java` |
| Safe input reading | `src/hospital/util/Input.java` |
| CSV read/write engine | `src/hospital/util/CsvUtil.java` |
| Auto ID generation | `src/hospital/util/IdGenerator.java` |
| Reusable CRUD base | `src/hospital/service/TableService.java` |
| Patient CRUD | `src/hospital/service/PatientService.java` |
| Doctor CRUD | `src/hospital/service/DoctorService.java` |
| Appointment handling | `src/hospital/service/AppointmentService.java` |
| Health reports + version history | `src/hospital/service/MedicalRecordService.java` |
| Combined prescription + report | `src/hospital/service/PrescriptionService.java` |
| Lab tests & results | `src/hospital/service/LaboratoryService.java` |
| Medicine stock | `src/hospital/service/PharmacyService.java` |
| Bed assignment/release | `src/hospital/service/BedService.java` |
| Bills & payments | `src/hospital/service/BillingService.java` |
| Revenue & expenses | `src/hospital/service/AccountingService.java` |
| Blood donors | `src/hospital/service/BloodBankService.java` |
| Overview report | `src/hospital/service/ReportService.java` |
| Domain objects | `src/hospital/model/*.java` |

---

## 12. Design Patterns / Techniques Demonstrated

- **Utility classes with private constructors** — `Input`, `CsvUtil`, `IdGenerator` (static methods only).
- **Inheritance / code reuse** — `TableService` base + six subclasses; override `add()` where the flow differs (`PrescriptionService`).
- **Composition / dependency injection** — services receive the services they need via constructors (`MainMenu`).
- **Factory method** — `Patient.from(...)`, `Doctor.from(...)`, `Appointment.from(...)`.
- **Persistence without a database** — CSV files + a generic reader/writer (`CsvUtil`), with automatic file creation.
- **Referential integrity checks** — `exists()` + early return.
- **Audit/versioning** — `medical_record_history.csv` keeps every version of a report.
- **Data-driven printing** — `TableService.format()` prints columns using the `fields` array, so subclasses print correctly with no extra code.

---

## 13. Known Notes / Limitations

- **Concurrency:** No locking — two terminals editing the same CSV could overwrite each other. Fine for a single-user console app.
- **Data types:** Everything is stored as text; numeric fields are parsed only when needed (amounts, stock). A malformed row is skipped or causes a friendly "invalid" message rather than a crash (try/catch in key places).
- **Search:** Simple case-insensitive substring search; no fuzzy/advanced search.
- **Deletions are permanent:** Deliting a patient does not clean up their appointments/bills/reports in other files (no cascading delete).
- **No decimal validation on money in some paths** — but `Input.amount()` and explicit checks in BillingService (`paid > amount`) keep basic sanity.
- **Prescriptions keep their own medicine snapshot** (name/dosage/instructions are stored on the line), so old prescriptions stay readable even if the medicine list changes.

---

## 14. Quick Start to Extending the Project

To add a brand-new simple module (e.g. "Ambulance"):

1. Add a CSV file under `data/` (e.g. `ambulances.csv`).
2. Create a service that **extends `TableService`**:
   ```java
   public class AmbulanceService extends TableService {
       public AmbulanceService(){ super("data/ambulances.csv","AMB","Ambulance","Driver","Plate","Status"); }
   }
   ```
3. In `MainMenu`, add a field, a submenu method, and a main-menu option.
4. Done — `add`, `view`, `search`, `delete` all work for free. Build a more complex module by overriding `add()` like `PrescriptionService` does.

---

*Documentation generated from the CarePlus codebase. See also `README.md` for the quick run instructions.*
