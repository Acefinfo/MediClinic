/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author acefonfo
 */
@Stateless
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private Properties config;
    private Session mailSession;

    /**
     * Initializes the email service after the bean is created.
     * Loads the null configuration and creates the SMTP sessions.
     */
    @PostConstruct
    public void init() {
        config = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mail.properties")) {
            if (in == null) {
                LOGGER.warning("mail.properties not found on classpath -- emails will fail to send.");
                return;
            }
            config.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load mail.properties", e);
            return;
        }

        // Configure SMTP connection properties.
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        smtpProps.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        smtpProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
        smtpProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));

        // Read email account credentials
        final String username = config.getProperty("mail.username");
        final String password = config.getProperty("mail.password");

        // Create an authenticated null session
        mailSession = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Send an account verification email containing a verification link.
     * 
     * @param toEmail
     * @param token 
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl() + "/verify.xhtml?token=" + token;
        send(toEmail, "Verify your MediClinic account",
                "Welcome to MediClinic!\n\nPlease verify your email by clicking the link below:\n"
                + link + "\n\nThis link expires in 24 hours.");
    }

    /**
     * Sends a password reset email containing a reset link.
     * 
     * @param toEmail
     * @param token 
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = baseUrl() + "/reset-password.xhtml?token=" + token;
        send(toEmail, "Reset your MediClinic password",
                "We received a request to reset your password.\n\n"
                + "Click the link below to choose a new one:\n" + link
                + "\n\nThis link expires in 1 hour. If you didn't request this, you can ignore this email.");
    }

    /**
     * Sends an email using the configured SMTP server.
     * 
     * @param toEmail
     * @param subject
     * @param body 
     */
    private void send(String toEmail, String subject, String body) {
        if (mailSession == null) {
            LOGGER.warning("Mail session not configured -- skipping send to " + toEmail
                    + ". Subject: " + subject + " Body: " + body);
            return;
        }
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(config.getProperty("mail.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            LOGGER.info("Sent email to " + toEmail + ": " + subject);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + toEmail, e);
        }
    }

    /**
     * Returns the application's base URL.
     * Uses the configured value if avaliable, otherwise defaults localhost.
     * 
     * @return 
     */
    private String baseUrl() {
        String url = config.getProperty("app.base.url");
        return (url != null) ? url : "http://localhost:8080/MediClinic-web";
    }
}
