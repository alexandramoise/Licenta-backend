package com.example.backend.service.implementation;

import com.example.backend.model.entity.table.*;
import com.example.backend.model.exception.InvalidAccountType;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.*;
import com.example.backend.service.SendEmailService;
import com.example.backend.utils.PasswordGenerator;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;

import javax.print.Doc;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class SendEmailServiceImpl implements SendEmailService {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final Map<String, UserRepo> userRepositories;
    private final TreatmentTakingRepo treatmentTakingRepo;
    private final TreatmentRepo treatmentRepo;
    private final JavaMailSender emailSender;

    private final Configuration configuration;

    @Value("${spring.mail.username}")
    private String adminEmail;

    @Value("${chc.app.bcrypt.salt}")
    private String bcryptSalt;

    private final String companyName = "CardioHealth Companion";
    private final PasswordEncoder passwordEncoder;

    public SendEmailServiceImpl(DoctorRepo doctorRepo,
                                PatientRepo patientRepo,
                                TreatmentTakingRepo treatmentTakingRepo, TreatmentRepo treatmentRepo, JavaMailSender emailSender,
                                Configuration configuration, PasswordEncoder passwordEncoder) {
        this.treatmentTakingRepo = treatmentTakingRepo;
        this.treatmentRepo = treatmentRepo;
        this.emailSender = emailSender;
        this.configuration = configuration;
        this.passwordEncoder = passwordEncoder;
        this.userRepositories = new HashMap<>();
        this.userRepositories.put("Doctor", doctorRepo);
        this.userRepositories.put("Patient", patientRepo);
    }

    private void sendEmailUtils(String fileName, String emailTo, String password, String subject, String link) {
        Map<String, Object> mapUser = new HashMap<>();
        mapUser.put("email", emailTo);
        mapUser.put("companyName", companyName);
        mapUser.put("password", password);
        mapUser.put("date", new Date().toString());
        mapUser.put("link", link);
        try {
            Template template  = configuration.getTemplate(fileName);
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapUser);
            sendEmail(emailTo, subject, htmlTemplate);
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void sendEmail(String emailTo, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setFrom(adminEmail);
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setText(text, true);
            emailSender.send(message);
            System.out.println("Email sent successfully to: " + emailTo);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object sendCreateAccountEmail(String email, String accountType) {
        String password = PasswordGenerator.generatePassayPassword(15);
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        Map<String, Object> mapUser = new HashMap<>();
        mapUser.put("email", email);
        mapUser.put("password", password);
        mapUser.put("companyName", companyName);
        mapUser.put("link", "https://localhost:5173/change-password?for=" + accountType.toLowerCase().substring(0,1));

        try {
            Template template = configuration.getTemplate("welcome-template.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapUser);
            sendEmail(email, "Activare cont " + companyName, htmlTemplate);
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }

        if(accountType.equals("Doctor")) {
            Doctor doctor = new Doctor();
            doctor.setEmail(email);
            doctor.setPassword(passwordEncoder.encode(password));
            doctor.setFirstLoginEver(true);

            return doctor;
        } else {
            Patient patient = new Patient();
            patient.setEmail(email);
            patient.setPassword(passwordEncoder.encode(password));
            patient.setFirstLoginEver(true);
            return patient;
        }
    }

    @Override
    public void sendResetPasswordEmail(String email, String accountType) throws ObjectNotFound {
        String password = PasswordGenerator.generatePassayPassword(15);
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        Map<String, Object> mapUser = new HashMap<>();
        mapUser.put("email", email);
        mapUser.put("password", password);
        mapUser.put("companyName", companyName);
        mapUser.put("link", "https://localhost:5173/change-password?for=" + accountType.toLowerCase().substring(0,1));

        if(accountType.equals("Doctor")) {
            if(! repo.findByEmail(email).isPresent()) {
                throw new ObjectNotFound("No doctor account for this email");
            }

            Doctor doctor = (Doctor) repo.findByEmail(email).get();
            doctor.setPassword(passwordEncoder.encode(password));
            log.info("Parola temporara: " + password + ", parola hashed " + passwordEncoder.encode(password));
            doctor.setFirstLoginEver(true);
            repo.save(doctor);

        } else {
            if(! repo.findByEmail(email).isPresent()) {
                throw new ObjectNotFound("No patient account for this email");
            }

            Patient patient = (Patient) repo.findByEmail(email).get();
            patient.setPassword(passwordEncoder.encode(password));
            log.info("Parola temporara: " + password + ", parola hashed " + passwordEncoder.encode(password));
            patient.setFirstLoginEver(true);
            repo.save(patient);
        }

        try {
            Template template = configuration.getTemplate("change-password.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapUser);
            sendEmail(email, "Resetare parola cont" + companyName, htmlTemplate);
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public void sendCreateAppointmentEmail(Appointment appointment, Long id) {
        String doctorEmail = appointment.getDoctor().getEmail();
        String patientEmail = appointment.getPatient().getEmail();

        Map<String, Object> mapAppointment = new HashMap<>();
        DoctorRepo doctorRepo = (DoctorRepo) this.userRepositories.get("Doctor");
        PatientRepo patientRepo = (PatientRepo) this.userRepositories.get("Patient");
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).get();
        Patient patient = patientRepo.findByEmail(patientEmail).get();
        String doctorName = doctor.getFirstName().concat(" " + doctor.getLastName());
        String patientName = patient.getFirstName().concat(" " + patient.getLastName());

        String strDate = dateFormat.format(appointment.getTime());

        mapAppointment.put("doctorName", doctorName);
        mapAppointment.put("patientName", patientName);
        mapAppointment.put("date", strDate);
        mapAppointment.put("visitType", appointment.getVisitType());
        mapAppointment.put("link", "https://localhost:5173/appointments");
        mapAppointment.put("comment", appointment.getComment());

        try {
            Template template  = configuration.getTemplate("create-appointment.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapAppointment);

            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctorEmail, "Programare noua: " + patientName, htmlTemplate);
                //log.info("CREARE PROGRAMARE TRIMIT LA DOCTOR");
            }

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(patientEmail, "Programare noua: " + patientName, htmlTemplate);
                //log.info("CREARE PROGRAMARE TRIMIT LA PACIENT");
            }
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendUpdateAppointmentEmail(Appointment appointment, Long id) {
        String doctorEmail = appointment.getDoctor().getEmail();
        String patientEmail = appointment.getPatient().getEmail();

        Map<String, Object> mapAppointment = new HashMap<>();
        DoctorRepo doctorRepo = (DoctorRepo) this.userRepositories.get("Doctor");
        PatientRepo patientRepo = (PatientRepo) this.userRepositories.get("Patient");
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).get();
        Patient patient = patientRepo.findByEmail(patientEmail).get();
        String doctorName = doctor.getFirstName().concat(" " + doctor.getLastName());
        String patientName = patient.getFirstName().concat(" " + patient.getLastName());

        String strDate = dateFormat.format(appointment.getTime());

        mapAppointment.put("doctorName", doctorName);
        mapAppointment.put("patientName", patientName);
        mapAppointment.put("date", strDate);
        mapAppointment.put("visitType", appointment.getVisitType());
        mapAppointment.put("link", "https://localhost:5173/appointments");
        mapAppointment.put("comment", appointment.getComment());

        try {
            Template template  = configuration.getTemplate("create-appointment.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapAppointment);

            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctorEmail, "Modificare data programare " + patientName, htmlTemplate);
                log.info("MODIFICARE PROGRAMARE TRIMIT LA DOCTOR");
            }

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(patientEmail, "Modificare data programare " + patientName, htmlTemplate);
                log.info("MODIFICARE PROGRAMARE TRIMIT LA PACIENT");
            }
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment, Long id) {
        String doctorEmail = appointment.getDoctor().getEmail();
        String patientEmail = appointment.getPatient().getEmail();

        Map<String, Object> mapAppointment = new HashMap<>();
        DoctorRepo doctorRepo = (DoctorRepo) this.userRepositories.get("Doctor");
        PatientRepo patientRepo = (PatientRepo) this.userRepositories.get("Patient");
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).get();
        Patient patient = patientRepo.findByEmail(patientEmail).get();
        String doctorName = doctor.getFirstName().concat(" " + doctor.getLastName());
        String patientName = patient.getFirstName().concat(" " + patient.getLastName());

        String strDate = dateFormat.format(appointment.getTime());

        mapAppointment.put("doctorName", doctorName);
        mapAppointment.put("patientName", patientName);
        mapAppointment.put("date", strDate);
        mapAppointment.put("visitType", appointment.getVisitType());
        mapAppointment.put("link", "https://localhost:5173/appointments");
        mapAppointment.put("comment", appointment.getComment());

        try {
            Template template  = configuration.getTemplate("appointment-reminder.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapAppointment);

            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctorEmail, "Reminder programare: " + mapAppointment.get("patientName"), htmlTemplate);
            }

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(patientEmail, "Reminder programare: " + mapAppointment.get("patientName"), htmlTemplate);
            }
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendTreatmentAdded(Long treatmentId, String email) {
        Treatment treatment = treatmentRepo.findById(treatmentId).orElseThrow(() -> new ObjectNotFound("Treatment not found"));
        String medicine = treatment.getMedicine().getName();
        Integer doses = treatment.getDoses();
        String comment = treatment.getComment();
        Map<String, Object> mapTreatment = new HashMap<>();

        UserRepo repo = this.userRepositories.get("Patient");
        PatientRepo patientRepo = (PatientRepo) repo;
        Patient patient = patientRepo.findByEmail(email).get();

        String patientName = patient.getFirstName() + " " + patient.getLastName();
        String doctorEmail = patient.getDoctor().getEmail();

        mapTreatment.put("patientName", patientName);
        mapTreatment.put("medicine", medicine);
        mapTreatment.put("doses", doses);
        mapTreatment.put("comment", comment);

        try {
            Template template  = configuration.getTemplate("treatment-added.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapTreatment);

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(email, "Medicament adaugat", htmlTemplate);
            }
            // sendEmail(doctorEmail, "Medicament adaugat " + patientName, htmlTemplate);
            //log.info("S-a trimis mail-ul pentru tratamentul: " + treatment + " la pacientul: " + email + " pentru medicamentul " + treatment.getMedicine().getName());
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendTreatmentChanged(Long treatmentId, String email) {
        Treatment treatment = treatmentRepo.findById(treatmentId).orElseThrow(() -> new ObjectNotFound("Treatment not found"));
        String medicine = treatment.getMedicine().getName();
        Integer doses = treatment.getDoses();
        String comment = treatment.getComment();
        Map<String, Object> mapTreatment = new HashMap<>();

        UserRepo repo = this.userRepositories.get("Patient");
        PatientRepo patientRepo = (PatientRepo) repo;
        Patient patient = patientRepo.findByEmail(email).get();
        Doctor doctor = patient.getDoctor();
        String patientName = patient.getFirstName() + " " + patient.getLastName();

        mapTreatment.put("patientName", patientName);
        mapTreatment.put("medicine", medicine);
        mapTreatment.put("doses", doses);
        mapTreatment.put("comment", comment);

        try {
            Template template  = configuration.getTemplate("treatment-modified.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapTreatment);

            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctor.getEmail(), "Medicament modificat", htmlTemplate);
            }

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(email, "Medicament modificat", htmlTemplate);
            }
            //log.info("S-a trimis mail-ul pentru tratamentul: " + treatment + " la pacientul: " + email + " pentru medicamentul " + treatment.getMedicine().getName());
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendEndedTreatment(Long treatmentId, String email) {
        Treatment treatment = treatmentRepo.findById(treatmentId).orElseThrow(() -> new ObjectNotFound("Treatment not found"));

        UserRepo repo = this.userRepositories.get("Patient");
        PatientRepo patientRepo = (PatientRepo) repo;
        Patient patient = patientRepo.findByEmail(email).get();
        Doctor doctor = patient.getDoctor();

        String patientName = patient.getFirstName() + " " + patient.getLastName();
        String doctorEmail = doctor.getEmail();

        String medicine = treatment.getMedicine().getName();
        Integer doses = treatment.getDoses();
        Map<String, Object> mapTreatment = new HashMap<>();

        mapTreatment.put("patientName", patientName);
        mapTreatment.put("medicine", medicine);
        mapTreatment.put("doses", doses);

        try {
            Template template  = configuration.getTemplate("treatment-ended.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapTreatment);

            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctorEmail, "Tratament incheiat: " + patientName, htmlTemplate);
            }

            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(email, "Tratament incheiat", htmlTemplate);
            }
            //log.info("S-a trimis mail-ul pentru tratamentul: " + treatment + " la pacientul: " + email + " pentru medicamentul " + treatment.getMedicine().getName());
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendPatientChangedType(Long patientId, String newTendency) {
        UserRepo repo = this.userRepositories.get("Patient");
        PatientRepo patientRepo = (PatientRepo) repo;
        Patient patient = patientRepo.findById(patientId).get();
        String name = patient.getFirstName() + " " + patient.getLastName();

        Doctor doctor = patient.getDoctor();
        String doctorEmail = doctor.getEmail();

        Date date = new Date();
        String strDate = dateFormat.format(date);

        Map<String, Object> mapPatient = new HashMap<>();
        mapPatient.put("patientName", name);
        mapPatient.put("tendency", newTendency);
        mapPatient.put("date", strDate);

        try {
            Template template  = configuration.getTemplate("tendency-changed.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapPatient);

            //sendEmail(patientEmail, "Modificare tendinta " + name, htmlTemplate);
            if(doctor.getIsActive() && doctor.getSendNotifications()) {
                sendEmail(doctorEmail, "Modificare tendinta " + name, htmlTemplate);
            }
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendTreatmentAdministrationReminder(Long treatmentId, String email) {
        Treatment treatment = treatmentRepo.findById(treatmentId).orElseThrow(() -> new ObjectNotFound("Treatment not found"));
        Patient patient = treatment.getPatient();
        String medicine = treatment.getMedicine().getName();
        Integer doses = treatment.getDoses();
        TreatmentTaking lastAdministration = treatmentTakingRepo.findLatestTreatmentTaking(treatment.getId(), email);
        Map<String, Object> mapTreatmentTaking = new HashMap<>();

        if(lastAdministration == null) {
            mapTreatmentTaking.put("latest", "nu exista");
        } else {
            mapTreatmentTaking.put("latest", dateFormat.format(lastAdministration.getAdministrationDate()));
        }

        mapTreatmentTaking.put("patientName", email);
        mapTreatmentTaking.put("medicine", medicine);
        mapTreatmentTaking.put("doses", doses);

        try {
            Template template  = configuration.getTemplate("treatment-reminder.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapTreatmentTaking);
            if(patient.getIsActive() && patient.getSendNotifications()) {
                sendEmail(email, "Notificare administrare tratament", htmlTemplate);
            }
            //log.info("S-a trimis mail-ul pentru tratamentul: " + treatment + " la pacientul: " + email + " pentru medicamentul " + treatment.getMedicine().getName());
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
