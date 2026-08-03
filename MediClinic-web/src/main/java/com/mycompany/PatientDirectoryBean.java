/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Patient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.PatientService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "patientDirectoryBean")
@ViewScoped
public class PatientDirectoryBean implements Serializable{
     
    private static final long serialVersionUID = 1L;
    
    private List<Patient> patients;
    private String searchKeyword;
    
    // Walk-in registration form fields
    private String name;
    private String phone;
    private String email;
    private String password;
    private Date dateOfBirth;
    private Patient.Gender gender;
    private String address;
    
    // Edit form fields
    private Long editPatientId;
    private String editName;
    private String editPhone;
    private Date editDateOfBirth;
    private Patient.Gender editGender;
    private String editAddress;
    private String editAllergies;
    private String editChronicConditions;
    private String editEmergencyContactName;
    private String editEmergencyContactPhone;

    @EJB
    private PatientService patientService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;
    
    /**
     * Automatically executed after bean creation.
     * Loads all patients into the directory.
     */
    @PostConstruct
    public void init(){
        loadPatients();
    }
    
    /**
     * Retrieves all patients from the database.
     */
    public void loadPatients(){
        patients = patientService.listAll();
    }
    
    /**
     * Searches patients using the entered keyword.
     */
    public void search(){
        patients = patientService.search(searchKeyword);
    }
    
    /**
     * Registers a new walk-in patient.
     * Clears the registration form after successful registration.
     */
    public void registerWalkIn(){
        try{
            patientService.registerWalkIn(loggedInUser.getUser(), email, password, name, phone, dateOfBirth, gender, address);
            name = null;
            phone = null;
            email = null;
            password = null;
            dateOfBirth = null;
            gender = null;
            address = null;
            loadPatients();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient registered.");
        }catch (AuthException e){
            addMessage(FacesMessage.SEVERITY_ERROR,"Failed", e.getMessage());
           
        }
    }
    
    /**
     * Loads the selected patient's details into edit form.
     * 
     * @param patient 
     */
    public void startEdit(Patient patient){
        editPatientId = patient.getId();
        editName = patient.getName();
        editPhone = patient.getPhone();
        editDateOfBirth = patient.getDateOfBirth();
        editGender = patient.getGender();
        editAddress = patient.getAddress();
        editAllergies = patient.getAllergies();
        editChronicConditions = patient.getChronicConditions();
        editEmergencyContactName = patient.getEmergencyContactName();
        editEmergencyContactPhone = patient.getEmergencyContactPhone();
    }
    
    /**
     * Saves the edited patient details.
     */
    public void saveEdit() {
        try {
            patientService.updatePatient(loggedInUser.getUser(), editPatientId, editName, editPhone,
                    editDateOfBirth, editGender, editAddress, editAllergies, editChronicConditions,
                    editEmergencyContactName, editEmergencyContactPhone);
            loadPatients();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient record updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }
    
    /**
     * Helper method for displaying JSF message.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    // Getter and setter 
    // Required for the JSF data binding 
    
    public List<Patient> getPatients() {
        return patients;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Long getEditPatientId() {
        return editPatientId;
    }

    public void setEditPatientId(Long editPatientId) {
        this.editPatientId = editPatientId;
    }

    public String getEditName() {
        return editName;
    }

    public void setEditName(String editName) {
        this.editName = editName;
    }

    public String getEditPhone() {
        return editPhone;
    }

    public void setEditPhone(String editPhone) {
        this.editPhone = editPhone;
    }

    public Date getEditDateOfBirth() {
        return editDateOfBirth;
    }

    public void setEditDateOfBirth(Date editDateOfBirth) {
        this.editDateOfBirth = editDateOfBirth;
    }

    public Patient.Gender getEditGender() {
        return editGender;
    }

    public void setEditGender(Patient.Gender editGender) {
        this.editGender = editGender;
    }

    public String getEditAddress() {
        return editAddress;
    }

    public void setEditAddress(String editAddress) {
        this.editAddress = editAddress;
    }

    public String getEditAllergies() {
        return editAllergies;
    }

    public void setEditAllergies(String editAllergies) {
        this.editAllergies = editAllergies;
    }

    public String getEditChronicConditions() {
        return editChronicConditions;
    }

    public void setEditChronicConditions(String editChronicConditions) {
        this.editChronicConditions = editChronicConditions;
    }

    public String getEditEmergencyContactName() {
        return editEmergencyContactName;
    }

    public void setEditEmergencyContactName(String editEmergencyContactName) {
        this.editEmergencyContactName = editEmergencyContactName;
    }

    public String getEditEmergencyContactPhone() {
        return editEmergencyContactPhone;
    }

    public void setEditEmergencyContactPhone(String editEmergencyContactPhone) {
        this.editEmergencyContactPhone = editEmergencyContactPhone;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
    

