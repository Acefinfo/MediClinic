/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Patient;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.PatientService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "patientProfileBean")
public class PatientProfileBean {
    private static final long serialVersionUID = 1L;
    
    private Patient patient;
    
    private String phone;
    private Date dateOfBirth;
    private Patient.Gender gender;
    private String address;
    private String allergies;
    private String chronicConditions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    
    @EJB
    private PatientService patientService;
    
    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;
    
    
    /**
     * Automatically executed after the bean is created.
     * Loads the logged-in patient's profile information.
     */
    @PostConstruct
    public void init() {
        patient = patientService.findByUserId(loggedInUser.getUser().getId());
        if (patient != null) {
            phone = patient.getPhone();
            dateOfBirth = patient.getDateOfBirth();
            gender = patient.getGender();
            address = patient.getAddress();
            allergies = patient.getAllergies();
            chronicConditions = patient.getChronicConditions();
            emergencyContactName = patient.getEmergencyContactName();
            emergencyContactPhone = patient.getEmergencyContactPhone();
        }
    }
    
    /**
     * Saves updated patient profile information.
     */
    public void save() {
        try {
            patientService.updateOwnProfile(loggedInUser.getUser(), phone, dateOfBirth, gender, address,
                    allergies, chronicConditions, emergencyContactName, emergencyContactPhone);
            patient = patientService.findByUserId(loggedInUser.getUser().getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Helper method to display JSF message.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
 
    public Patient getPatient() {
        return patient;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Patient.Gender getGender() {
        return gender;
    }

    public void setGender(Patient.Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(String chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}