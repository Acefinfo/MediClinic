/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Appointment;
import entity.Consultation;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AppointmentService;
import service.AuthException;
import service.ConsultationService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "staffAppointmentBean")
@ViewScoped
public class StaffAppointmentBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Appointment> appointments;

    // Consultation popup state
    private Appointment viewAppointment;
    private Consultation viewConsultation;

    @EJB
    private AppointmentService appointmentService;
    
    @EJB
    private ConsultationService consultationService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the managed bean after creation.
     * Loads all appointment into the table. 
     */
    @PostConstruct
    public void init() {
        loadAll();
    }

    /**
     * Retrieves all appointment from the system and refreshes the appointments into table.
     * 
     * If successful:
     *  - Updates the appointment status to APPROVED
     *  - Reloads the appointment list
     *  - Displays a success message
     *
      * If an error occurs, an error message is displayed.
     */
    public void loadAll() {
        appointments = appointmentService.listAll();
    }

    /**
     * Approves the selected appointment. 
     * @param appointment 
     */
    public void approve(Appointment appointment) {
        try {
            appointmentService.approveAppointment(loggedInUser.getUser(), appointment.getId());
            loadAll();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment approved.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Cancels the selected appointment.
     * if successful 
     * - Updates the appointment status to CANCELLED
     *  - Reloads the appointment list
     *  - Displays a success message
     *
     * If an error occurs, an error message is displayed.
     * 
     * @param appointment 
     */
    public void cancel(Appointment appointment) {
        try {
            appointmentService.cancelAppointment(loggedInUser.getUser(), appointment.getId());
            loadAll();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment cancelled.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

//    
//    public void viewConsultation(Appointment appointment) {
//        viewAppointment = appointment;
//        viewConsultation = consultationService.findConsultationForAppointment(appointment.getId());
//    }
    public void loadConsultation(Appointment appointment) {
        viewAppointment = appointment;
        viewConsultation = consultationService.findConsultationForAppointment(appointment.getId());
    }

    /**
     * Display a JSF FacesMessage.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public Appointment getViewAppointment() {
        return viewAppointment;
    }

    public Consultation getViewConsultation() {
        return viewConsultation;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
