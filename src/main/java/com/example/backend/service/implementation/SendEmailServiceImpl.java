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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class SendEmailServiceImpl implements SendEmailService {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
        String subject = "Activare cont "  + companyName;
        String password = PasswordGenerator.generatePassayPassword(15);
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        if(accountType.equals("Doctor")) {
            Doctor doctor = new Doctor();
            doctor.setEmail(email);
            doctor.setPassword(passwordEncoder.encode(password));
            log.info("Parola: " + password + ", parola hashed " + passwordEncoder.encode(password));
            doctor.setFirstLoginEver(true);
            repo.save(doctor);
            String link = "http://192.168.56.1:5173/change-password?for=d";
            sendEmailUtils("welcome-template.ftl", email, password, subject, link);
            log.info("In SendEmailService: S-a trimis si salvat contul pt doctor!");
            return doctor;
        } else {
            Patient patient = new Patient();
            patient.setEmail(email);
            patient.setPassword(passwordEncoder.encode(password));
            log.info("Parola: " + password + ", parola hashed " + passwordEncoder.encode(password));
            patient.setFirstLoginEver(true);
            repo.save(patient);
            String link = "http://192.168.56.1:5173/change-password?for=p";
            sendEmailUtils("welcome-template.ftl", email, password, subject, link);
            log.info("In SendEmailService: S-a trimis si salvat contul pt pacient!");
            return patient;
        }
    }

    public Map<String, Object> setCreateAppointmentEmailDetails(String doctorEmail, String patientEmail,
                                                                Date date, String visitType, String link, String comment) {
        Map<String, Object> mapUser = new HashMap<>();
        DoctorRepo doctorRepo = (DoctorRepo) this.userRepositories.get("Doctor");
        PatientRepo patientRepo = (PatientRepo) this.userRepositories.get("Patient");
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).get();
        Patient patient = patientRepo.findByEmail(patientEmail).get();
        String doctorName = doctor.getFirstName().concat(" " + doctor.getLastName());
        String patientName = patient.getFirstName().concat(" " + patient.getLastName());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);

        mapUser.put("doctorName", doctorName);
        mapUser.put("patientName", patientName);
        mapUser.put("date", strDate);
        mapUser.put("visitType", visitType);
        mapUser.put("link", link);
        mapUser.put("comment", comment);

        return mapUser;
    }

    @Override
    public void sendResetPasswordEmail(String email, String accountType) throws ObjectNotFound {
        String subject = "Resetare parola cont "  + companyName;
        String password = PasswordGenerator.generatePassayPassword(15);
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        if(accountType.equals("Doctor")) {
            if(! repo.findByEmail(email).isPresent()) {
                throw new ObjectNotFound("No doctor account for this email");
            }

            Doctor doctor = (Doctor) repo.findByEmail(email).get();
            doctor.setPassword(passwordEncoder.encode(password));
            log.info("Parola temporara: " + password + ", parola hashed " + passwordEncoder.encode(password));
            doctor.setFirstLoginEver(true);
            repo.save(doctor);
            String link = "http://192.168.56.1:5173/change-password?for=d";
            sendEmailUtils("change-password.ftl", email, password, subject, link);
        } else {
            if(! repo.findByEmail(email).isPresent()) {
                throw new ObjectNotFound("No patient account for this email");
            }

            Patient patient = (Patient) repo.findByEmail(email).get();
            patient.setPassword(passwordEncoder.encode(password));
            log.info("Parola temporara: " + password + ", parola hashed " + passwordEncoder.encode(password));
            patient.setFirstLoginEver(true);
            repo.save(patient);
            String link = "http://192.168.56.1:5173/change-password?for=p";
            sendEmailUtils("change-password.ftl", email, password, subject, link);
        }
    }

    public void sendCreateAppointmentEmail(Appointment appointment, Long id) {
        String doctorEmail = appointment.getDoctor().getEmail();
        String patientEmail = appointment.getPatient().getEmail();
        Date date = appointment.getTime();
        String visitType = appointment.getVisitType();
        String comment = appointment.getComment();
        String patientLink = "http://localhost:8080/appointments/cancel/patient/" + id;
        String doctorLink = "http://localhost:8080/appointments/cancel/doctor/" + id;
        Map<String, Object> mapDoctor = setCreateAppointmentEmailDetails(doctorEmail, patientEmail, date, visitType, doctorLink, comment);
        Map<String, Object> mapPatient = setCreateAppointmentEmailDetails(doctorEmail, patientEmail, date, visitType, patientLink, comment);
        try {
            Template template  = configuration.getTemplate("create-appointment.ftl");
            String htmlTemplateDoctor = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapDoctor);
            String htmlTemplatePatient = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapPatient);
            sendEmail(doctorEmail, "Programare noua: " + patientEmail, htmlTemplateDoctor);
            sendEmail(patientEmail, "Programare noua: " + patientEmail, htmlTemplatePatient);
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendUpdateAppointmentEmail(Appointment appointment, Long id) {
        String doctorEmail = appointment.getDoctor().getEmail();
        String patientEmail = appointment.getPatient().getEmail();
        Date date = appointment.getTime();
        String visitType = appointment.getVisitType();
        String comment = appointment.getComment();
        String patientLink = "http://localhost:8080/appointments/confirm-canceled/patient/" + id;
        String doctorLink = "http://localhost:8080/appointments/confirm-canceled/doctor/" + id;
        Map<String, Object> mapDoctor = setCreateAppointmentEmailDetails(doctorEmail, patientEmail, date, visitType, doctorLink, comment);
        Map<String, Object> mapPatient = setCreateAppointmentEmailDetails(doctorEmail, patientEmail, date, visitType, patientLink, comment);
        try {
            Template template  = configuration.getTemplate("change-appointment-date.ftl");
            String htmlTemplateDoctor = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapDoctor);
            String htmlTemplatePatient = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapPatient);
            sendEmail(doctorEmail, "Modificare data programare: " + patientEmail, htmlTemplateDoctor);
            sendEmail(patientEmail, "Modificare data programare: " + patientEmail, htmlTemplatePatient);
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void sendTreatmentAdministrationReminder(Long treatmentId, String email) {
        Treatment treatment = treatmentRepo.findById(treatmentId).orElseThrow(() -> new ObjectNotFound("Treatment not found"));
        String medicine = treatment.getMedicine().getName();
        Integer doses = treatment.getDoses();
        TreatmentTaking lastAdministration = treatmentTakingRepo.findLatestTreatmentTaking(treatment.getId(), email);
        Map<String, Object> mapTreatmentTaking = new HashMap<>();

        if(lastAdministration == null) {
            mapTreatmentTaking.put("latest", "nu exista");
        } else {
            mapTreatmentTaking.put("latest", lastAdministration);
        }

        mapTreatmentTaking.put("patientName", email);
        mapTreatmentTaking.put("medicine", medicine);
        mapTreatmentTaking.put("doses", doses);


        try {
            Template template  = configuration.getTemplate("treatment-reminder.ftl");
            String htmlTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(template, mapTreatmentTaking);
            //sendEmail(email, "Notificare administrare tratament", htmlTemplate);
            log.info("S-a trimis mail-ul pentru tratamentul: " + treatment + " la pacientul: " + email + " pentru medicamentul " + treatment.getMedicine().getName());
        } catch (IOException | TemplateException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
