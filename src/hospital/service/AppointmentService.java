package hospital.service;

import hospital.model.Appointment;
import hospital.util.*;
import java.util.*;

public class AppointmentService {
    private static final String FILE="data/appointments.csv"; private final PatientService patients; private final DoctorService doctors;
    public AppointmentService(PatientService patients,DoctorService doctors){this.patients=patients;this.doctors=doctors;}
    public void schedule(){
        String patient=Input.text("Patient ID: "),doctor=Input.text("Doctor ID: ");
        if(!patients.exists(patient)||!doctors.exists(doctor)){System.out.println("Patient or doctor ID does not exist.");return;}
        String date=Input.text("Date (YYYY-MM-DD): "),time=Input.text("Time (HH:MM): ");
        CsvUtil.withFileLock(FILE+".lock", () -> {
            String conflict=slotConflict(patient,doctor,date,time);
            if(conflict!=null){System.out.println(conflict);return;}
            Appointment appointment=new Appointment(IdGenerator.next(FILE,"A"),patient,doctor,date,time,"Scheduled");
            CsvUtil.append(FILE,appointment.toCsv());
            System.out.println("Appointment scheduled. ID: "+appointment.toCsv()[0]);
        });
    }
    public List<Appointment> all(){List<Appointment>list=new ArrayList<>();for(String[]r:CsvUtil.read(FILE))if(r.length>=6)list.add(Appointment.from(r));return list;}
    public void view(){print(all());}
    public void complete(){changeStatus("Completed");} public void cancel(){changeStatus("Cancelled");}
    private void changeStatus(String status){String id=Input.text("Appointment ID: ");List<String[]>rows=new ArrayList<>();boolean found=false;for(Appointment a:all()){String[]r=a.toCsv();if(r[0].equalsIgnoreCase(id)){r[5]=status;found=true;}rows.add(r);}CsvUtil.writeAll(FILE,rows);System.out.println(found?"Status updated.":"Appointment not found.");}
    private String slotConflict(String patient,String doctor,String date,String time){
        boolean doctorBusy=false,patientBusy=false;
        for(Appointment appointment:all()){
            String[] row=appointment.toCsv();
            if(!"Scheduled".equalsIgnoreCase(row[5])||!row[3].equalsIgnoreCase(date)||!row[4].equalsIgnoreCase(time))continue;
            doctorBusy|=row[2].equalsIgnoreCase(doctor);
            patientBusy|=row[1].equalsIgnoreCase(patient);
        }
        if(doctorBusy&&patientBusy)return "The doctor and patient already have an appointment at this time.";
        if(doctorBusy)return "Doctor already has an appointment at this date and time.";
        if(patientBusy)return "Patient already has an appointment at this date and time.";
        return null;
    }
    private void print(List<Appointment> list){if(list.isEmpty())System.out.println("No appointments found.");else for(Appointment a:list)System.out.println(a);}
}
