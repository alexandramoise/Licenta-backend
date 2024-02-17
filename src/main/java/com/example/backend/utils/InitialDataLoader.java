package com.example.backend.utils;

import com.example.backend.model.entity.*;
import com.example.backend.model.repo.*;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
@Log4j2
public class InitialDataLoader implements CommandLineRunner {
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final AppointmentRepo appointmentRepo;
    private final BloodPressureRepo bloodPressureRepo;
    private final MedicalConditionRepo medicalConditionRepo;
    private final MedicineRepo medicineRepo;
    private final TreatmentRepo treatmentRepo;
    private final TreatmentTakingRepo treatmentTakingRepo;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlValue;

    public InitialDataLoader(DoctorRepo doctorRepo, PatientRepo patientRepo, AppointmentRepo appointmentRepo, BloodPressureRepo bloodPressureRepo, MedicalConditionRepo medicalConditionRepo, MedicineRepo medicineRepo, TreatmentRepo treatmentRepo, TreatmentTakingRepo treatmentTakingRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
        this.bloodPressureRepo = bloodPressureRepo;
        this.medicalConditionRepo = medicalConditionRepo;
        this.medicineRepo = medicineRepo;
        this.treatmentRepo = treatmentRepo;
        this.treatmentTakingRepo = treatmentTakingRepo;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        if(ddlValue.equals("create")) {
            /* create DOCTORS */
            Doctor doctor1 = createDoctor("Andrei", "Popescu", "andreipopescu@gmail.com", "password", true);
            Doctor doctor2 = createDoctor("Ana-Maria", "Ionescu", "anaionescu@gmail.com", "random", false);

            /* create PATIENTS */
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            Patient patient1 = createPatient("Roxana", "Pop", "roxanapop10@yahoo.com", "somethingsecure", true, formatter.parse("01-08-2002"), Gender.Feminine, doctor1);
            Patient patient2 = createPatient("Ionut", "Adam", "adamionut@yahoo.com", "secure", true, formatter.parse("20-09-1998"), Gender.Masculine, doctor2);
            Patient patient3 = createPatient("Ana", "Moise", "anamoise05@gmail.com", "lalalala", true, formatter.parse("05-02-1979"), Gender.Feminine, doctor1);
            Patient patient4 = createPatient("Fabian", "Popescu", "popescufabian80@yahoo.com", "blabla", false, formatter.parse("10-02-1980"), Gender.Masculine, doctor2);

            /* create APPOINTMENT */
            Appointment appointment1 = createAppointment(formatter.parse("15-02-2024"), patient1, doctor1, "Consultatie", true, true);
            Appointment appointment2 = createAppointment(formatter.parse("17-04-2024"), patient2, doctor2, "Rutina", true, true);

            /* create BLOOD PRESSURES */
            SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
            BloodPressure bloodPressure1 = createBloodPressure(120, 80, 80, formatter2.parse("15-02-2024 08:00"), patient1);
            BloodPressure bloodPressure2 = createBloodPressure(130, 90, 100, formatter2.parse("15-02-2024 15:10"), patient1);
            BloodPressure bloodPressure3 = createBloodPressure(150, 100, 100, formatter2.parse("15-02-2024 20:00"), patient1);
            BloodPressure bloodPressure4 = createBloodPressure(110, 65, 70, formatter2.parse("15-02-2024 08:00"), patient2);
            BloodPressure bloodPressure5 = createBloodPressure(120, 80, 80, formatter2.parse("15-02-2024 18:00"), patient2);
            BloodPressure bloodPressure6 = createBloodPressure(100, 50, 50, formatter2.parse("15-02-2024 10:00"), patient3);
            BloodPressure bloodPressure7 = createBloodPressure(110, 60, 60, formatter2.parse("15-02-2024 16:00"), patient3);
            BloodPressure bloodPressure8 = createBloodPressure(180, 120, 100, formatter2.parse("15-02-2024 09:15"), patient4);
            BloodPressure bloodPressure9 = createBloodPressure(135, 95, 90, formatter2.parse("15-02-2024 18:10"), patient4);

            /* create MEDICAL CONDITIONS */
                // asociate HIPERTENSIUNE
            MedicalCondition medicalCondition1 = createMedicalCondition("Diabet tip 1", true, false);
            MedicalCondition medicalCondition15 = createMedicalCondition("Diabet tip 2", true, false);
            MedicalCondition medicalCondition2 = createMedicalCondition("Boli renale", true, false);
            MedicalCondition medicalCondition3 = createMedicalCondition("Hipotiroidism", true, false);
            MedicalCondition medicalCondition4 = createMedicalCondition("Lupus", true, false);
            MedicalCondition medicalCondition5 = createMedicalCondition("Scleroderma", true, false);
            MedicalCondition medicalCondition8 = createMedicalCondition("Sedentarism", true, false);
            MedicalCondition medicalCondition9 = createMedicalCondition("Consum de alcool", true, false);
            MedicalCondition medicalCondition10 = createMedicalCondition("Fumat", true, false);
            MedicalCondition medicalCondition11 = createMedicalCondition("Obezitate", true, false);
                // asociate HIPOTENSIUNE
            MedicalCondition medicalCondition6 = createMedicalCondition("Anemie", false, true);
            MedicalCondition medicalCondition7 = createMedicalCondition("Hipoglicemie", false, true);
            MedicalCondition medicalCondition12 = createMedicalCondition("Aritmie cardiaca", false, true);
            MedicalCondition medicalCondition13 = createMedicalCondition("Parkinson", false, true);
            MedicalCondition medicalCondition14 = createMedicalCondition("Deshidratare", false, true);
            MedicalCondition hypertension = createMedicalCondition("Hipertensiune", true, false);
            MedicalCondition hypotension = createMedicalCondition("Hipotensiune", false, true);

            /* create MEDICINES */
                // pentru HIPERTENSIUNE
            Medicine medicine1 = createMedicine("Enalapril", List.of(hypertension, medicalCondition2));
            Medicine medicine2 = createMedicine("Lisinopril", List.of(hypertension));
            Medicine medicine3 = createMedicine("Candesartan", List.of(hypertension));
            Medicine medicine4 = createMedicine("Losartan", List.of(hypertension));
            Medicine medicine5 = createMedicine("Hidroclorotiazida", List.of(hypertension, medicalCondition2));
            Medicine medicine6 = createMedicine("Atenolol", List.of(hypertension));
            Medicine medicine7 = createMedicine("Amlodipina", List.of(hypertension));
            Medicine medicine8 = createMedicine("Diltiazem", List.of(hypertension));
            hypertension.setMedicines(List.of(medicine1, medicine2, medicine3, medicine4, medicine5, medicine6, medicine7, medicine8));
                // pentru HIPOTENSIUNE
            Medicine medicine9 = createMedicine("Astonin", List.of(hypotension));
            Medicine medicine10 = createMedicine("Gutron", List.of(hypotension));
            Medicine medicine11 = createMedicine("Efedrina", List.of(hypotension));
            hypotension.setMedicines(List.of(medicine9, medicine10, medicine11));
                // pentru ANEMIE
            medicalCondition6.setMedicines(List.of(createMedicine("Suplimente cu fier", List.of(medicalCondition6)),
                                                    createMedicine("Suplimente vitamina B12", List.of(medicalCondition6))
                                            )
            );
                // pentru DIABET
            Medicine medicine12 = createMedicine("Insulina", List.of(medicalCondition1, medicalCondition15));
            medicalCondition1.setMedicines(List.of(medicine12));
            medicalCondition15.setMedicines(List.of(medicine12, createMedicine("Metformin", List.of(medicalCondition15)),
                                                                createMedicine("Glucotrol", List.of(medicalCondition15)),
                                                                createMedicine("Pioglitazona", List.of(medicalCondition15)),
                                                                createMedicine("Januvia", List.of(medicalCondition15)),
                                                                createMedicine("Forxiga", List.of(medicalCondition15))
                                            )
            );
                // pentru HIPOTIROIDISM
            medicalCondition3.setMedicines(List.of(createMedicine("Levotiroxina", List.of(medicalCondition3))));
                // ETC RESTUL BOLILOR

            /* create TREATMENT */
            Treatment treatment1 = createTreatment(patient1, new Date(), 1, medicine1, hypertension);
            log.info("Treatment: " + treatment1.getPatient().getEmail() + ", " + treatment1.getMedicine().getName() + " for " + treatment1.getMedicalCondition().getName() + " to take " + treatment1.getDoses() + " times a day");

            /* create TREATMENT TAKING */
            TreatmentTaking treatmentTaking1 = createTreatmentTaking(patient1, treatment1, new Date());
        }
    }

    public Doctor createDoctor(String firstName, String lastName, String email, String password, Boolean firstLogin) {
        Doctor doctor = new Doctor();
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setEmail(email);
        doctor.setPassword(password);
        doctor.setFirstLoginEver(firstLogin);
        doctorRepo.save(doctor);
        return doctor;
    }

    public Patient createPatient(String firstName, String lastName, String email, String password,
                                 Boolean firstLogin, Date date, Gender gender, Doctor doctor) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setEmail(email);
        patient.setPassword(password);
        patient.setFirstLoginEver(firstLogin);
        patient.setDateOfBirth(date);
        patient.setGender(gender);
        patient.setDoctor(doctor);
        patientRepo.save(patient);
        return patient;
    }

    public Appointment createAppointment(Date date, Patient patient, Doctor doctor,
                                         String visitType, Boolean patientIsComing, Boolean doctorIsAvailable) {
        Appointment appointment = new Appointment();
        appointment.setTime(date);
        appointment.setDoctor(doctor);
        appointment.setVisitType(visitType);
        appointment.setPatientIsComing(patientIsComing);
        appointment.setDoctorIsAvailable(doctorIsAvailable);
        appointmentRepo.save(appointment);
        return appointment;
    }

    public BloodPressure createBloodPressure(Integer systolic, Integer diastolic, Integer pulse,
                                             Date date, Patient patient) {
        BloodPressure bloodPressure = new BloodPressure();
        bloodPressure.setDiastolic(diastolic);
        bloodPressure.setSystolic(systolic);
        bloodPressure.setPulse(pulse);
        bloodPressure.setDate(date);
        bloodPressure.setPatient(patient);
        bloodPressureRepo.save(bloodPressure);
        return bloodPressure;
    }

    public MedicalCondition createMedicalCondition(String name, Boolean increases, Boolean reduces) {
        MedicalCondition medicalCondition = new MedicalCondition();
        medicalCondition.setName(name);
        medicalCondition.setIncreasesBP(increases);
        medicalCondition.setReducesBP(reduces);
        medicalConditionRepo.save(medicalCondition);
        return medicalCondition;
    }

    public Medicine createMedicine(String name, List<MedicalCondition> medicalConditions) {
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setMedicalConditions(medicalConditions);
        medicineRepo.save(medicine);
        return medicine;
    }

    public Treatment createTreatment(Patient patient, Date startingDate, Integer doses, Medicine medicine, MedicalCondition medicalCondition) {
        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setStartingDate(startingDate);
        treatment.setDoses(doses);
        treatment.setMedicalCondition(medicalCondition);
        treatment.setMedicine(medicine);
        treatmentRepo.save(treatment);
        return treatment;
    }

    public TreatmentTaking createTreatmentTaking(Patient patient, Treatment treatment, Date date) {
        TreatmentTaking treatmentTaking = new TreatmentTaking();
        treatmentTaking.setPatient(patient);
        treatmentTaking.setTreatment(treatment);
        treatmentTaking.setAdministrationDate(date);
        treatmentTakingRepo.save(treatmentTaking);
        return treatmentTaking;
    }
}
