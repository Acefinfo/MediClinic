/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Doctor;
import entity.DoctorSchedule;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import service.DoctorService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "adminDoctorBean")
@ViewScoped
public class AdminDoctorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Doctor> doctors;
    private List<DoctorSchedule> schedules;

    @EJB
    private DoctorService doctorService;

    /**
     * Initializes the bean after dependency injection.
     * Loads all doctors and their schedules from the database.
     */
    @PostConstruct
    public void init() {
        doctors = doctorService.listAll();
        schedules = doctorService.listAllSchedules();
    }

    
    /**
     * Returns the list of all doctors
     * @return 
     */
    public List<Doctor> getDoctors() {
        return doctors;
    }
    
    /**
     * Returns the list of all doctor schedules.
     * 
     * @return 
     */
    public List<DoctorSchedule> getSchedules() {
        return schedules;
    }
}
