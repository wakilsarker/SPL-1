package hospital.menu;

import hospital.service.*;
import hospital.util.Input;

/** Terminal menus. Keeping menus separate from services makes the program easy to explain. */
public class MainMenu {
    private final PatientService patients=new PatientService();
    private final DoctorService doctors=new DoctorService();
    private final AppointmentService appointments=new AppointmentService(patients,doctors);
    private final MedicalRecordService records=new MedicalRecordService(patients,doctors);
    private final LaboratoryService laboratory=new LaboratoryService(patients);
    private final PharmacyService pharmacy=new PharmacyService();
    private final PrescriptionService prescriptions=new PrescriptionService(patients,doctors,pharmacy,records);
    private final BedService beds=new BedService(patients);
    private final BillingService billing=new BillingService(patients);
    private final AccountingService accounting=new AccountingService();
    private final BloodBankService donors=new BloodBankService();
    private final ReportService reports=new ReportService(patients,doctors,appointments,beds,billing,accounting);

    public void start(){
        while(true){
            title("HOSPITAL MANAGEMENT SYSTEM");
            System.out.println("1. Patient Management\n2. Doctor Management\n3. Appointment Management\n4. Medical Records\n5. Prescription Management\n6. Laboratory Management\n7. Pharmacy Management\n8. Bed Management\n9. Billing & Payment\n10. Accounting\n11. Blood Donor Management\n12. Reports\n0. Exit");
            switch(Input.number("Enter choice: ")){
                case 1:patientMenu();break; case 2:doctorMenu();break; case 3:appointmentMenu();break;case 4:recordMenu();break;case 5:prescriptionMenu();break;case 6:labMenu();break;case 7:pharmacyMenu();break;case 8:bedMenu();break;case 9:billingMenu();break;case 10:accountingMenu();break;case 11:donorMenu();break;case 12:reports.show();Input.pause();break;case 0:System.out.println("Thank you for using CarePlus.");return;default:System.out.println("Invalid option.");
            }
        }
    }
    private void patientMenu(){while(true){
        title("PATIENT MANAGEMENT");
        System.out.println("1. Register Patient\n2. View All Patients\n3. Search Patient\n4. Update Patient\n5. Delete Patient\n6. View Patient History\n0. Back");switch(Input.number("Choice: ")){case 1:patients.register();break;case 2:patients.view();break;case 3:patients.search();break;case 4:patients.update();break;case 5:patients.delete();break;case 6:records.patientHistory();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void doctorMenu(){while(true){title("DOCTOR MANAGEMENT");System.out.println("1. Add Doctor\n2. View Doctors\n3. Search Doctor\n4. Update Doctor\n5. Delete Doctor\n0. Back");switch(Input.number("Choice: ")){case 1:doctors.add();break;case 2:doctors.view();break;case 3:doctors.search();break;case 4:doctors.update();break;case 5:doctors.delete();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void appointmentMenu(){while(true){title("APPOINTMENT MANAGEMENT");System.out.println("1. Schedule Appointment\n2. View Appointments\n3. Mark Completed\n4. Cancel Appointment\n0. Back");switch(Input.number("Choice: ")){case 1:appointments.schedule();break;case 2:appointments.view();break;case 3:appointments.complete();break;case 4:appointments.cancel();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void recordMenu(){while(true){title("HEALTH REPORTS");System.out.println("1. Add Health Report\n2. View All Reports\n3. Search Reports\n4. View Patient Report History\n5. Update Health Report\n0. Back");switch(Input.number("Choice: ")){case 1:records.add();break;case 2:records.view();break;case 3:records.search();break;case 4:records.patientHistory();break;case 5:records.update();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void prescriptionMenu(){while(true){title("PRESCRIPTION MANAGEMENT");System.out.println("1. Make Prescription & Health Report\n2. View Prescriptions\n3. Search Prescriptions\n4. View Patient Report History\n0. Back");switch(Input.number("Choice: ")){case 1:prescriptions.makePrescriptionAndHealthReport();break;case 2:prescriptions.view();break;case 3:prescriptions.search();break;case 4:records.patientHistory();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void labMenu(){while(true){title("LABORATORY MANAGEMENT");System.out.println("1. Add Lab Test\n2. View Lab Tests\n3. Enter Lab Result\n4. View Lab Results\n0. Back");switch(Input.number("Choice: ")){case 1:laboratory.addTest();break;case 2:laboratory.viewTests();break;case 3:laboratory.addResult();break;case 4:laboratory.viewResults();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void pharmacyMenu(){while(true){title("PHARMACY MANAGEMENT");System.out.println("1. Add Medicine\n2. View Medicines\n3. Search Medicine\n4. Update Stock\n5. Dispense Medicine\n0. Back");switch(Input.number("Choice: ")){case 1:pharmacy.add();break;case 2:pharmacy.view();break;case 3:pharmacy.search();break;case 4:pharmacy.updateStock();break;case 5:pharmacy.dispense();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void bedMenu(){while(true){title("BED MANAGEMENT");System.out.println("1. Add Bed\n2. View Beds\n3. Assign Bed\n4. Release Bed\n5. Search Available Beds\n0. Back");switch(Input.number("Choice: ")){case 1:beds.add();break;case 2:beds.view();break;case 3:beds.assign();break;case 4:beds.release();break;case 5:beds.available();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void billingMenu(){while(true){title("BILLING & PAYMENT");System.out.println("1. Create Bill\n2. View Bills\n3. Search Bills\n4. Receive Payment\n0. Back");switch(Input.number("Choice: ")){case 1:billing.add();break;case 2:billing.view();break;case 3:billing.search();break;case 4:billing.receivePayment();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void accountingMenu(){while(true){title("ACCOUNTING");System.out.println("1. Add Expense\n2. View Expenses\n3. Add Revenue\n4. View Revenues\n5. Financial Summary\n0. Back");switch(Input.number("Choice: ")){case 1:accounting.addExpense();break;case 2:accounting.viewExpenses();break;case 3:accounting.addRevenue();break;case 4:accounting.viewRevenues();break;case 5:accounting.summary();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void donorMenu(){while(true){title("BLOOD DONOR MANAGEMENT");System.out.println("1. Add Donor\n2. View Donors\n3. Search Donor\n4. Find Available Donor by Group\n0. Back");switch(Input.number("Choice: ")){case 1:donors.add();break;case 2:donors.view();break;case 3:donors.search();break;case 4:donors.findAvailable();break;case 0:return;default:System.out.println("Invalid option.");}pause();}}
    private void title(String text){System.out.println("\n========================================\n       "+text+"\n========================================");}
    private void pause(){Input.pause();}
}
