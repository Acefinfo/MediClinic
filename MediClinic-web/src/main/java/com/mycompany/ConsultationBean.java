/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.PatientDao;
import entity.Appointment;
import entity.Consultation;
import entity.Prescription;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.BillingService;
import service.ConsultationService;
import service.PrescriptionService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "consultationBean")
@ViewScoped
public class ConsultationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long appointmentId;
    private Appointment appointment;
    private Consultation consultation;

    private String symptoms;
    private String diagnosis;
    private String notes;

    private boolean accessDenied;
    private boolean canEdit;

    private List<Prescription> prescriptions;
    private String newMedicineName;
    private String newDosage;
    private String newFrequency;
    private String newDuration;
    private String newInstructions;

    @EJB
    private ConsultationService consultationService;

    @EJB
    private PrescriptionService prescriptionService;
    
    @EJB
    private BillingService billingService;
    @EJB
    private PatientDao patientDao;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Loads appointment and consultation data. Determines weather the current
     * user has permission to access and edit the consultation.
     */
    public void load() {
        String role = loggedInUser.getRoleName();

        try {
            appointment = consultationService.findAppointment(appointmentId);
            consultation = consultationService.findConsultationForAppointment(appointmentId);

            if ("PATIENT".equals(role)) {
                entity.Patient patient = patientDao.findByUserId(loggedInUser.getUser().getId());
                boolean ownsAppointment = patient != null && appointment.getPatient().getId().equals(patient.getId());
                boolean unlocked = consultation != null && billingService.isConsultationUnlockedForPatient(consultation.getId());

                if (!ownsAppointment || !unlocked) {
                    accessDenied = true;
                    return;
                }
            }

            if (consultation != null) {
                symptoms = consultation.getSymptoms();
                diagnosis = consultation.getDiagnosis();
                notes = consultation.getNotes();
                loadPrescriptions();
            }
            canEdit = "DOCTOR".equals(role)
                    && appointment.getDoctor().getUser() != null
                    && appointment.getDoctor().getUser().getId().equals(loggedInUser.getUser().getId());

        } catch (AuthException e) {
            accessDenied = true;
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
        
    }

    /**
     * Saves or updates the consultation information.
     */
    public void save() {
        try {
            consultation = consultationService.saveConsultation(loggedInUser.getUser(), appointmentId, symptoms, diagnosis, notes);
            appointment = consultationService.findAppointment(appointmentId);
            loadPrescriptions();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Consultation saved.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Retrieves all prescription associates with the consultation.
     */
    public void loadPrescriptions() {
        if (consultation != null) {
            prescriptions = prescriptionService.listForConsultation(consultation.getId());
        }
    }

    /**
     * Adds a new prescription associated with the consultation
     * Clears the input feilds after successful save.
     */
    public void addPrescription() {
        if (consultation == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Please save the consultation before adding a prescription.");
            return;
        }
        
        try {
            prescriptionService.addPrescription(loggedInUser.getUser(), consultation.getId(), newMedicineName, newDosage, newFrequency, newDuration, newInstructions);
            newMedicineName = null;
            newDosage = null;
            newFrequency = null;
            newDuration = null;
            newInstructions = null;
            loadPrescriptions();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Prescription added.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }

    }

    /**
     * Utility method for displaying JSF methods
     *
     * @param severity
     * @param summary
     * @param detail
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isAccessDenied() {
        return accessDenied;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public String getNewMedicineName() {
        return newMedicineName;
    }

    public void setNewMedicineName(String newMedicineName) {
        this.newMedicineName = newMedicineName;
    }

    public String getNewDosage() {
        return newDosage;
    }

    public void setNewDosage(String newDosage) {
        this.newDosage = newDosage;
    }

    public String getNewFrequency() {
        return newFrequency;
    }

    public void setNewFrequency(String newFrequency) {
        this.newFrequency = newFrequency;
    }

    public String getNewDuration() {
        return newDuration;
    }

    public void setNewDuration(String newDuration) {
        this.newDuration = newDuration;
    }

    public String getNewInstructions() {
        return newInstructions;
    }

    public void setNewInstructions(String newInstructions) {
        this.newInstructions = newInstructions;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
