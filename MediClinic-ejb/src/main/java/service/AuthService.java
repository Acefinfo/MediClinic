/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.EmailVerificationDao;
import dao.PasswordResetDao;
import dao.PatientDao;
import dao.RoleDao;
import dao.UserDao;
import entity.EmailVerification;
import entity.PasswordReset;
import entity.Patient;
import entity.Role;
import entity.User;
import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import util.PasswordUtil;
import util.TokenUtil;

/**
 *
 * @author acefonfo
 */
@Stateless
public class AuthService {

    private static final long EMAIL_TOKEN_VALIDITY_MS = 24L * 60 * 60 * 1000; // 24 hours
    private static final long RESET_TOKEN_VALIDITY_MS = 60L * 60 * 1000;      // 1 hour

    @EJB private UserDao userDao;
    @EJB private RoleDao roleDao;
    @EJB private PatientDao patientDao;
    @EJB private EmailVerificationDao emailVerificationDao;
    @EJB private PasswordResetDao passwordResetDao;
    @EJB private EmailService emailService;

    public User registerPatient(String email, String password, String name, String phone) throws AuthException, Exception {
        if (userDao.existsByEmail(email)) {
            throw new AuthException("An account with this email already exists.");
        }

        Role patientRole = roleDao.findByName("PATIENT");
        if (patientRole == null) {
            throw new AuthException("PATIENT role is not configured.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.hash(password));
        user.setRole(patientRole);
        user.setStatus(User.UserStatus.PENDING);
        user.setEmailVerified(false);
        userDao.create(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName(name);
        patient.setPhone(phone);
        patientDao.create(patient);

        String token = TokenUtil.generateToken();
        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setToken(token);
        ev.setExpiryDate(new Date(System.currentTimeMillis() + EMAIL_TOKEN_VALIDITY_MS));
        ev.setVerified(false);
        emailVerificationDao.create(ev);

        emailService.sendVerificationEmail(email, token);

        return user;
    }

    public void verifyEmail(String token) throws AuthException {
        EmailVerification ev = emailVerificationDao.findByToken(token);
        if (ev == null) {
            throw new AuthException("Invalid verification link.");
        }
        if (ev.isVerified()) {
            throw new AuthException("This email is already verified.");
        }
        if (ev.getExpiryDate().before(new Date())) {
            throw new AuthException("This verification link has expired.");
        }

        ev.setVerified(true);
        emailVerificationDao.update(ev);

        User user = ev.getUser();
        user.setEmailVerified(true);
        user.setStatus(User.UserStatus.ACTIVE);
        userDao.update(user);
    }

    public User login(String email, String password) throws AuthException {
        User user = userDao.findByEmail(email);
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            throw new AuthException("Invalid email or password.");
        }
        if (user.getStatus() == User.UserStatus.DEACTIVATED) {
            throw new AuthException("This account has been deactivated. Contact an administrator.");
        }
        if (!user.isEmailVerified()) {
            throw new AuthException("Please verify your email before logging in.");
        }
        return user;
    }

    public void requestPasswordReset(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            return; // don't reveal whether the email exists
        }
        String token = TokenUtil.generateToken();
        PasswordReset pr = new PasswordReset();
        pr.setUser(user);
        pr.setToken(token);
        pr.setExpiryDate(new Date(System.currentTimeMillis() + RESET_TOKEN_VALIDITY_MS));
        pr.setUsed(false);
        passwordResetDao.create(pr);

        emailService.sendPasswordResetEmail(email, token);
    }

    public void resetPassword(String token, String newPassword) throws AuthException {
        PasswordReset pr = passwordResetDao.findByToken(token);
        if (pr == null || pr.isUsed()) {
            throw new AuthException("Invalid or already used reset link.");
        }
        if (pr.getExpiryDate().before(new Date())) {
            throw new AuthException("This reset link has expired.");
        }

        User user = pr.getUser();
        user.setPassword(PasswordUtil.hash(newPassword));
        userDao.update(user);

        pr.setUsed(true);
        passwordResetDao.update(pr);
    }
}