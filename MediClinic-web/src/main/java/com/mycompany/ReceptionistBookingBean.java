/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

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
import service.PatientService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "receptionistBookingBean")
@ViewScoped
public class ReceptionistBookingBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    @EJB
    private AppointmentService appointmentService;
    @EJB
    private PatientService patientService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    // Patient search
    private String patientSearchKeyword;
    private List<Patient> patients;

    // Booking form fields
    private Patient selectedPatient;
    private Long selectedDoctorId;
    private Long selectedScheduleId;
    private List<Doctor> doctors;
    private List<SelectItem> scheduleItems;
    private Date appointmentDate;
    private Date appointmentTime;
    private String reason;

    /**
     * Loads the doctor list and the full patient list after bean creation.
     */
    @PostConstruct
    public void init() {
        doctors = appointmentService.listDoctors();
        patients = patientService.listAll();
    }

    /**
     * Searches for a patient by name, phone, or email.
     */
    public void searchPatients() {
        patients = patientService.search(patientSearchKeyword);
    }

    /**
     * Clears the patient search and restores the full patient list.
     */
    public void clearPatientSearch() {
        patientSearchKeyword = null;
        patients = patientService.listAll();
    }

    /**
     * Selects a patient to book the appointment for.
     *
     * @param patient
     */
    public void selectPatient(Patient patient) {
        selectedPatient = patient;
    }

    /**
     * Clears the currently selected patient, returning the form to the
     * patient-search step.
     */
    public void clearSelectedPatient() {
        selectedPatient = null;
    }

    /**
     * Triggered when the receptionist selects a doctor.
     * Reloads the available schedule list for that doctor.
     */
    public void onDoctorChange() {
        selectedScheduleId = null;
        scheduleItems = buildScheduleItems(selectedDoctorId);
    }

    private List<SelectItem> buildScheduleItems(Long doctorId) {
        List<SelectItem> items = new ArrayList<>();
        if (doctorId == null) {
            return items;
        }
        for (DoctorSchedule s : appointmentService.listSchedulesForDoctor(doctorId)) {
            String label = s.getDayOfWeek() + " " + TIME_FORMAT.format(s.getStartTime())
                    + " - " + TIME_FORMAT.format(s.getEndTime()) + " (max " + s.getMaxPatients() + ")";
            items.add(new SelectItem(s.getId(), label));
        }
        return items;
    }

    /**
     * Books the appointment for the selected patient.
     * Clears the booking form (but keeps the patient search results) on success.
     */
    public void book() {
        try {
            if (selectedPatient == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Please select a patient first.");
                return;
            }
            appointmentService.bookAppointmentForPaitent(loggedInUser.getUser(), selectedPatient.getId(),
                    selectedDoctorId, selectedScheduleId, appointmentDate, appointmentTime, reason);

            String patientName = selectedPatient.getName();
            selectedPatient = null;
            selectedDoctorId = null;
            selectedScheduleId = null;
            scheduleItems = null;
            appointmentDate = null;
            appointmentTime = null;
            reason = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment booked for " + patientName + ".");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public String getPatientSearchKeyword() {
        return patientSearchKeyword;
    }

    public void setPatientSearchKeyword(String patientSearchKeyword) {
        this.patientSearchKeyword = patientSearchKeyword;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public List<SelectItem> getScheduleItems() {
        return scheduleItems;
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

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}