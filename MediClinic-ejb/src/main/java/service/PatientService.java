/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.PatientDao;
import dao.RoleDao;
import dao.UserDao;
import entity.ActivityLog;
import entity.Patient;
import entity.Role;
import entity.User;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import util.PasswordUtil;

/**
 *
 * @author acefonfo
 */
@Stateless
public class PatientService {

    @EJB
    private PatientDao patientDao;
    @EJB
    private UserDao userDao;
    @EJB
    private RoleDao roleDao;
    @EJB
    private ActivityLogDao activityLogDao;

    /**
     * Registers a walk-in patient at the front desk. Creates both the login
     * account and the patient record. Because staff verified the person in
     * person, the account is activated immediately (no email verification
     * step required).
     */
    public Patient registerWalkIn(User actor, String email, String password, String name, String phone,
            Date dateOfBirth, Patient.Gender gender, String address) throws AuthException {

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
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userDao.create(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setName(name);
        patient.setPhone(phone);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setAddress(address);

        try {
            patientDao.create(patient);
        } catch (Exception e) {
            throw new AuthException("Could not create patient record: " + e.getMessage());
        }

        log(actor, "REGISTER_WALKIN_PATIENT", "Patient", patient.getId(), "Registered walk-in patient " + email);
        return patient;
    }

    public List<Patient> listAll() {
        return patientDao.findAll();
    }

    public List<Patient> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAll();
        }
        return patientDao.search(keyword.trim());
    }

    public Patient findByUserId(Long userId) {
        return patientDao.findByUserId(userId);
    }

    public Patient findById(Long id) {
        return patientDao.findById(id);
    }

    /**
     * Patient editing their own profile: demographic + medical info only.
     * Identity fields (name) are intentionally excluded from self-edit.
     */
    public void updateOwnProfile(User actor, String phone, Date dateOfBirth, Patient.Gender gender,
            String address, String allergies, String chronicConditions,
            String emergencyContactName, String emergencyContactPhone) throws AuthException {

        Patient patient = requirePatientForUser(actor.getId());
        patient.setPhone(phone);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setAllergies(allergies);
        patient.setChronicConditions(chronicConditions);
        patient.setEmergencyContactName(emergencyContactName);
        patient.setEmergencyContactPhone(emergencyContactPhone);
        patientDao.update(patient);

        log(actor, "UPDATE_OWN_PROFILE", "Patient", patient.getId(), "Patient updated their own profile");
    }

    /**
     * Admin/Receptionist editing any patient's full record, including name/phone.
     */
    public void updatePatient(User actor, Long patientId, String name, String phone, Date dateOfBirth,
            Patient.Gender gender, String address, String allergies, String chronicConditions,
            String emergencyContactName, String emergencyContactPhone) throws AuthException {

        Patient patient = patientDao.findById(patientId);
        if (patient == null) {
            throw new AuthException("Patient not found.");
        }
        patient.setName(name);
        patient.setPhone(phone);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setAllergies(allergies);
        patient.setChronicConditions(chronicConditions);
        patient.setEmergencyContactName(emergencyContactName);
        patient.setEmergencyContactPhone(emergencyContactPhone);
        patientDao.update(patient);

        log(actor, "UPDATE_PATIENT", "Patient", patient.getId(), "Updated patient record for " + patient.getName());
    }

    private Patient requirePatientForUser(Long userId) throws AuthException {
        Patient patient = patientDao.findByUserId(userId);
        if (patient == null) {
            throw new AuthException("No patient profile found for this account.");
        }
        return patient;
    }

    private void log(User actor, String action, String entityName, Long entityId, String details) {
        ActivityLog entry = new ActivityLog();
        entry.setUser(actor);
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        activityLogDao.create(entry);
    }
}