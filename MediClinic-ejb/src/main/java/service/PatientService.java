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
     * Register new  walk in patient.
     * 
     * Process: 
     *      1. Check whether the email is already registered.
     *      2. Retrieve the PATIENT role.
     *      3. Create a user account.
     *      4. Create the patient profile.
     *      5. Record the registration in the activity log.
     *
     * Since the patient registers in person, the account is immediately
     * activated and marked as email verified.
     * @param actor
     * @param email
     * @param password
     * @param name
     * @param phone
     * @param dateOfBirth
     * @param gender
     * @param address
     * @return Newly created patient record. 
     * @throws AuthException 
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

    /**
     * Returns all patient in the system
     * 
     * @return 
     */
    public List<Patient> listAll() {
        return patientDao.findAll();
    }

    /**
     * Searches patients using a keyword
     * If no keyword is supplied all patients are returned. 
     * 
     * @param keyword
     * @return 
     */
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
     * Allows patients to update thir own profile.
     * 
     * Editable fields:
     *      - Phone
     *      - Date of birth
     *      - Gender
     *      - Address
     *      - Allergies
     *      - Chronic conditions
     *      - Emergency contact
     *
     * Name is intentionally excluded to preserve identity integrity.
     * 
     * @param actor
     * @param phone
     * @param dateOfBirth
     * @param gender
     * @param address
     * @param allergies
     * @param chronicConditions
     * @param emergencyContactName
     * @param emergencyContactPhone
     * @throws AuthException 
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
    * Allows administrator or receptionist to update patients record. 
    * 
    * @param actor
    * @param patientId
    * @param name
    * @param phone
    * @param dateOfBirth
    * @param gender
    * @param address
    * @param allergies
    * @param chronicConditions
    * @param emergencyContactName
    * @param emergencyContactPhone
    * @throws AuthException 
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

    /**
     * Retrieves the patient profile associated with user account
     * 
     * @param userId
     * @return
     * @throws AuthException 
     */
    private Patient requirePatientForUser(Long userId) throws AuthException {
        Patient patient = patientDao.findByUserId(userId);
        if (patient == null) {
            throw new AuthException("No patient profile found for this account.");
        }
        return patient;
    }

    /**
     * Create an activity log entry for auditing purposes.
     * 
     * @param actor
     * @param action
     * @param entityName
     * @param entityId
     * @param details 
     */
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