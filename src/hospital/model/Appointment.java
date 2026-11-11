package hospital.model;

public class Appointment {
    private final String id,patientId,doctorId,date,time,status;
    public Appointment(String id,String patientId,String doctorId,String date,String time,String status){this.id=id;this.patientId=patientId;this.doctorId=doctorId;this.date=date;this.time=time;this.status=status;}
    public String[] toCsv(){
        return new String[]{id,patientId,doctorId,date,time,status};
    }
    public static Appointment from(String[] r){
        return new Appointment(r[0],r[1],r[2],r[3],r[4],r[5]);
    }
    public String toString(){
        return id+" | Patient: "+patientId+" | Doctor: "+doctorId+" | "+date+" "+time+" | "+status;}
}
