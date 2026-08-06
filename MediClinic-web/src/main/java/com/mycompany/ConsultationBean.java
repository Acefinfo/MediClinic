/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Appointment;
import entity.Consultation;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.ConsultationService;

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

    @EJB
    private ConsultationService consultationService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Loads appointment and consultation data.
     * Determines weather the current user has permission to access and edit the consultation.
     */
    public void load() {
        String role = loggedInUser.getRoleName();
        if ("PATIENT".equals(role)) {
            accessDenied = true;
            return;
        }

        try {
            appointment = consultationService.findAppointment(appointmentId);
            consultation = consultationService.findConsultationForAppointment(appointmentId);

            if (consultation != null) {
                symptoms = consultation.getSymptoms();
                diagnosis = consultation.getDiagnosis();
                notes = consultation.getNotes();
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
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Consultation saved.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Utility method for displaying JSF methods 
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

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
