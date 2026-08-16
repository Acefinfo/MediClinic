/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Appointment;
import entity.Doctor;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import service.AppointmentService;
import service.DoctorService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "doctorAppointmentBean")
@ViewScoped
public class DoctorAppointmentBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private List<Appointment> allAppointments;

    private List<Appointment> appointments;

    private String searchKeyword;

    @EJB
    private AppointmentService appointmentService;
    @EJB
    private DoctorService doctorService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the managed bean after creation. Retrieves the logged-in
     * doctor profile and loads all appointments assigned to that doctor.
     */
    @PostConstruct
    public void init() {
        Doctor doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
        if (doctor != null) {
            allAppointments = appointmentService.listForDoctor(doctor.getId());
        } else {
            allAppointments = new ArrayList<>();
        }
        appointments = allAppointments;
    }

    /**
     * Filters the appointment list by patient name, reason, or status using the
     * current search keyword. Case-insensitive, partial match.
     */
    public void search() {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            appointments = allAppointments;
            return;
        }

        String keyword = searchKeyword.trim().toLowerCase(Locale.US);
        List<Appointment> filtered = new ArrayList<>();

        for (Appointment a : allAppointments) {
            String patientName = (a.getPatient() != null && a.getPatient().getName() != null)
                    ? a.getPatient().getName().toLowerCase(Locale.US) : "";
            String reason = (a.getReason() != null) ? a.getReason().toLowerCase(Locale.US) : "";
            String status = (a.getStatus() != null) ? a.getStatus().name().toLowerCase(Locale.US) : "";

            if (patientName.contains(keyword) || reason.contains(keyword) || status.contains(keyword)) {
                filtered.add(a);
            }
        }

        appointments = filtered;
    }

    /**
     * Clears the search keyword and restores the full appointment list.
     */
    public void clearSearch() {
        searchKeyword = null;
        appointments = allAppointments;
    }

    /**
     * Retrieves all appointments schedules for today.
     * 
     * @return 
     */
    public List<Appointment> getTodayAppointments() {
        List<Appointment> result = new ArrayList<>();
        String today = DAY_FORMAT.format(new Date());

        for (Appointment a : appointments) {
            if (a.getAppointmentDate() != null && DAY_FORMAT.format(a.getAppointmentDate()).equals(today)) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Retrieves all appointments that are not scheduled for today.
     * 
     * @return 
     */
    public List<Appointment> getOtherAppointments() {
        List<Appointment> result = new ArrayList<>();
        String today = DAY_FORMAT.format(new Date());

        for (Appointment a : appointments) {
            if (a.getAppointmentDate() == null || !DAY_FORMAT.format(a.getAppointmentDate()).equals(today)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
