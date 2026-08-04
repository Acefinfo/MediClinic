/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Doctor;
import java.math.BigDecimal;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.DoctorService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "doctorProfileBean")
public class DoctorProfileBean {

    private Doctor doctor;

    private String specialization;
    private BigDecimal fee;
    private String bio;

    @EJB
    private DoctorService doctorService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the bean after dependency injection.
     * Retrieves the logged-in doctor's profile and populates
     * the form fields if a profile exists.
     */
    @PostConstruct
    public void init() {
        doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
        if (doctor != null) {
            specialization = doctor.getSpecialization();
            fee = doctor.getFee();
            bio = doctor.getBio();
        }
    }

    /**
     * Saves the updated doctor profile.
     * If successful, reloads the latest profile and displays
     * a success message. Otherwise, displays an error message.
     */
    public void save() {
        try {
            doctorService.updateOwnProfile(loggedInUser.getUser(), specialization, fee, bio);
            doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Displays a JSF FacesMessage on the page.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public BigDecimal getFee() {
        return fee;
    }
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Injects the logged-in user bean.
     */
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
