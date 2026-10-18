# CarePlus — Console Hospital Management System

A terminal-based SPL-1 project built with Core Java only. It uses classes, packages, `ArrayList`, CSV file handling, validation, searching, updates, deletes, and calculations. No database, Maven, framework, or external library is required.

## Run

From the project folder:

```bash
find src -name "*.java" -print | xargs javac -d bin
java -cp bin hospital.Main
```

## Multi-user login (separate terminals)

This SPL-1 version uses the JDK's TCP sockets for lightweight message passing: a login server coordinates sessions while each user opens the same console menu in a separate terminal. It intentionally has no third-party dependency. MPI itself requires a separate runtime such as MPJ Express and is designed to launch one fixed process group, not independently opened interactive terminals.

Compile once, then keep the server terminal open:

```bash
find src -name "*.java" -print | xargs javac -d bin
java -cp bin hospital.Main server
```

In each user terminal, from this project directory:

```bash
java -cp bin hospital.Main
```

The first server launch creates `data/users.csv` with these demo accounts: `admin/admin123`, `doctor/doctor123`, and `reception/reception123`. Each account can have only one active session. Use distinct accounts in separate terminals, then select `0` in the menu to log out.

For the original single-terminal menu without a login server, run:

```bash
java -cp bin hospital.Main standalone
```

The multi-user mode is intended for terminals on the same computer, started from the same project folder. CSV file access is serialized while a file is read or written; it remains a small SPL-1 CSV application rather than a network database.

Appointment scheduling also prevents an active doctor or patient from being booked twice at the same date and time. Cancelled appointments no longer reserve their slot.

The program stores its persistent data in `data/*.csv`. It includes a preloaded pharmacy catalogue of 100 medicines. Doctors can use **Prescription Management → Make Prescription & Health Report** to create one linked health report and one or more prescription lines for a patient. Updating a health report preserves every version in `data/medical_record_history.csv`.

## Structure

- `hospital.menu` — terminal menus and navigation
- `hospital.service` — application logic and CSV operations
- `hospital.model` — domain classes (`Patient`, `Doctor`, `Appointment`)
- `hospital.util` — reusable CSV, input, and ID utilities

The remaining lightweight modules use the reusable `TableService` so the project stays small enough to understand in a viva.
