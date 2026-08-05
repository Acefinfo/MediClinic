/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.PatientDao;
import entity.Appointment;
import entity.Doctor;
import entity.DoctorSchedule;
import entity.Patient;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import service.AppointmentService;
import service.AuthException;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "patientAppointmentBean")
@ViewScoped
public class PatientAppointmentBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private List<Doctor> doctors;
    private List<SelectItem> scheduleItems;
    private List<Appointment> myAppointments;

    // Booking form fields
    private Long selectedDoctorId;
    private Long selectedScheduleId;
    private Date appointmentDate;
    private Date appointmentTime;
    private String reason;

    // Reschedule form fields
    private Long rescheduleAppointmentId;
    private List<SelectItem> reschedScheduleItems;
    private Long rescheduleScheduleId;
    private Date rescheduleDate;
    private Date rescheduleTime;

    @EJB
    private AppointmentService appointmentService;
    @EJB
    private PatientDao patientDao;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the page after bean creation.
     * Loads doctor and the patients appointments.
     */
    @PostConstruct
    public void init() {
        doctors = appointmentService.listDoctors();
        loadMyAppointments();
    }

    /**
     * Loads all appointment belonging to the currently logged in patient
     */
    public void loadMyAppointments() {
        Patient patient = patientDao.findByUserId(loggedInUser.getUser().getId());
        if (patient != null) {
            myAppointments = appointmentService.listForPatient(patient.getId());
        }
    }

    /**
     * triggered when the user selects a doctor 
     * Reloads the available schedule  list.
     */
    public void onDoctorChange() {
        selectedScheduleId = null;
        scheduleItems = buildScheduleItems(selectedDoctorId);
    }

    /**
     * Builds  the schedule drop-down for the selected doctor.
     * @param doctorId
     * @return 
     */
    private List<SelectItem> buildScheduleItems(Long doctorId) {
        List<SelectItem> items = new ArrayList<>();
        if (doctorId == null) {
            return items;
        }
        for (DoctorSchedule s : appointmentService.listSchedulesForDoctor(doctorId)) {
            String label = s.getDayOfWeek() + " " + TIME_FORMAT.format(s.getStartTime()) + " - " + TIME_FORMAT.format(s.getEndTime()) + " (max " + s.getMaxPatients() + ")";
            items.add(new SelectItem(s.getId(), label));
        }
        return items;
    }

    /**
     * Books a new appointment.
     * If successful:
     *  - Clears the booking form
     *  - Reloads appointments
     *  - Displays a success message
     *
     * Otherwise displays an error message.
     */
    public void book() {
        try {
            appointmentService.bookAppointment(loggedInUser.getUser(), selectedDoctorId, selectedScheduleId,
                    appointmentDate, appointmentTime, reason);
            selectedDoctorId = null;
            selectedScheduleId = null;
            scheduleItems = null;
            appointmentDate = null;
            appointmentTime = null;
            reason = null;
            loadMyAppointments();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment requested. Awaiting approval.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Cancels the selected appointment.
     * 
     * @param appointment 
     */
    public void cancel(Appointment appointment) {
        try {
            appointmentService.cancelAppointment(loggedInUser.getUser(), appointment.getId());
            loadMyAppointments();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment cancelled.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Loads the selected appointment into the reschedule form.
     * 
     * @param appointment 
     */
    public void startReschedule(Appointment appointment) {
        rescheduleAppointmentId = appointment.getId();
        reschedScheduleItems = buildScheduleItems(appointment.getDoctor().getId());
        rescheduleScheduleId = appointment.getSchedule() != null ? appointment.getSchedule().getId() : null;
        rescheduleDate = appointment.getAppointmentDate();
        rescheduleTime = appointment.getAppointmentTime();
    }

    /**
     * Saves appointment changes after rescheduling.
     * 
     */
    public void saveReschedule() {
        try {
            appointmentService.rescheduleAppointment(loggedInUser.getUser(), rescheduleAppointmentId,
                    rescheduleScheduleId, rescheduleDate, rescheduleTime);
            loadMyAppointments();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment rescheduled. Awaiting re-approval.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Display a JSF FacesMessage.
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public List<SelectItem> getScheduleItems() {
        return scheduleItems;
    }

    public List<Appointment> getMyAppointments() {
        return myAppointments;
    }

    public Long getSelectedDoctorId() {
        return selectedDoctorId;
    }

    public void setSelectedDoctorId(Long selectedDoctorId) {
        this.selectedDoctorId = selectedDoctorId;
    }

    public Long getSelectedScheduleId() {
        return selectedScheduleId;
    }

    public void setSelectedScheduleId(Long selectedScheduleId) {
        this.selectedScheduleId = selectedScheduleId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Date getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Date appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRescheduleAppointmentId() {
        return rescheduleAppointmentId;
    }

    public void setRescheduleAppointmentId(Long rescheduleAppointmentId) {
        this.rescheduleAppointmentId = rescheduleAppointmentId;
    }

    public List<SelectItem> getReschedScheduleItems() {
        return reschedScheduleItems;
    }

    public Long getRescheduleScheduleId() {
        return rescheduleScheduleId;
    }

    public void setRescheduleScheduleId(Long rescheduleScheduleId) {
        this.rescheduleScheduleId = rescheduleScheduleId;
    }

    public Date getRescheduleDate() {
        return rescheduleDate;
    }

    public void setRescheduleDate(Date rescheduleDate) {
        this.rescheduleDate = rescheduleDate;
    }

    public Date getRescheduleTime() {
        return rescheduleTime;
    }

    public void setRescheduleTime(Date rescheduleTime) {
        this.rescheduleTime = rescheduleTime;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
