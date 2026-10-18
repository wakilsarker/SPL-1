# CarePlus — Console Hospital Management System

A terminal-based SPL-1 project built with Core Java only. It uses classes, packages, `ArrayList`, CSV file handling, validation, searching, updates, deletes, and calculations. No database, Maven, framework, or external library is required.

## Run

From the project folder:

```bash
find src -name "*.java" -print | xargs javac -d bin
java -cp bin hospital.Main
```

The program stores its persistent data in `data/*.csv`. It includes a preloaded pharmacy catalogue of 100 medicines. Doctors can use **Prescription Management → Make Prescription & Health Report** to create one linked health report and one or more prescription lines for a patient. Updating a health report preserves every version in `data/medical_record_history.csv`.

## Structure

- `hospital.menu` — terminal menus and navigation
- `hospital.service` — application logic and CSV operations
- `hospital.model` — domain classes (`Patient`, `Doctor`, `Appointment`)
- `hospital.util` — reusable CSV, input, and ID utilities

The remaining lightweight modules use the reusable `TableService` so the project stays small enough to understand in a viva.

