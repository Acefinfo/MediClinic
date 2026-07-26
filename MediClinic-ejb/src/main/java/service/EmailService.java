/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.util.logging.Logger;
import javax.ejb.Stateless;

/**
 *
 * @author acefonfo
 */
@Stateless
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // No SMTP/JavaMail dependency is wired up yet, so this just logs the link so it
    // can be copy-pasted during development. Swap the body for real javax.mail sending
    // once mail server credentials are available.

    public void sendVerificationEmail(String toEmail, String token) {
        String link = "http://localhost:8080/MediClinic-web/verify.xhtml?token=" + token;
        LOGGER.info("Verification email for " + toEmail + " -> " + link);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = "http://localhost:8080/MediClinic-web/reset-password.xhtml?token=" + token;
        LOGGER.info("Password reset email for " + toEmail + " -> " + link);
    }
}
