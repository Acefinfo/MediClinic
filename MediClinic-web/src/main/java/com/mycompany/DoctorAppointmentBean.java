/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Appointment;
import entity.Doctor;
import java.io.Serializable;
import java.util.List;
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

    private List<Appointment> appointments;

    @EJB
    private AppointmentService appointmentService;
    @EJB
    private DoctorService doctorService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the managed bean after creation.
     * Retrieves the logged-in doctor profile and loads all appointments assigned to that doctor.
     */
    @PostConstruct
    public void init() {
        Doctor doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
        if (doctor != null) {
            appointments = appointmentService.listForDoctor(doctor.getId());
        }
    }

    public List<Appointment> getAppointments() { return appointments; }

    public void setLoggedInUser(LoggedInUser loggedInUser) { this.loggedInUser = loggedInUser; }
}