package com.example.backend.service.implementation;

import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Patient;
import com.example.backend.model.entity.User;
import com.example.backend.model.exception.InvalidAccountType;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.model.repo.UserRepo;
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
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class SendEmailServiceImpl implements SendEmailService {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final Map<String, UserRepo> userRepositories;
    private final JavaMailSender emailSender;

    private final Configuration configuration;

    @Value("${spring.mail.username}")
    private String adminEmail;

    @Value("${aims.app.bcrypt.salt}")
    private String bcryptSalt;

    private final String companyName = "CardioHealth Companion";

    public SendEmailServiceImpl(DoctorRepo doctorRepo,
                                PatientRepo patientRepo,
                                JavaMailSender emailSender,
                                Configuration configuration) {
        this.emailSender = emailSender;
        this.configuration = configuration;
        this.userRepositories = new HashMap<>();
        this.userRepositories.put("Doctor", doctorRepo);
        this.userRepositories.put("Patient", patientRepo);
    }

    private void sendEmailUtils(String fileName, String emailTo, String password, String subject) {
        Map<String, Object> mapUser = new HashMap<>();
        mapUser.put("email", emailTo);
        mapUser.put("companyName", companyName);
        mapUser.put("password", password);
        mapUser.put("date", new Date().toString());
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
        //sendEmailUtils("welcome-template.ftl", email, password, subject);
        UserRepo repo = this.userRepositories.get(accountType);
        if (repo == null) {
            throw new InvalidAccountType("Invalid account type");
        }

        if(accountType.equals("Doctor")) {
            Doctor doctor = new Doctor();
            doctor.setEmail(email);
            doctor.setPassword(password.concat("HASHED"));
            doctor.setFirstLoginEver(true);
            repo.save(doctor);
            log.info("In SendEmailService: S-a trimis si salvat contul pt doctor!");
            return doctor;
        } else {
            Patient patient = new Patient();
            patient.setEmail(email);
            patient.setPassword(password.concat("HASHED"));
            patient.setFirstLoginEver(true);
            repo.save(patient);
            log.info("In SendEmailService: S-a trimis si salvat contul pt pacient!");
            return patient;
        }
    }
}
