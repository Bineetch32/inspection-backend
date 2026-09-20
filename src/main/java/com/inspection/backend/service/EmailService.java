package com.inspection.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReportEmail(byte[] pdf, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(mailAddress);
            helper.setSubject("Inspection Report");
            helper.setText(
                    "Please find the Inspection Report attached.\n\n"
                    + "This is an automated email from Inspection Management System.");

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(pdf));

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "Unable to prepare inspection report email.",
                    e);
        }
    }
}
