/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.PatientDao;
import entity.Patient;
import entity.Prescription;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import service.BillingService;
import service.PrescriptionService;

/**
 *
 * @author acefonfo
 */

@ManagedBean(name = "patientPrescriptionsBean")
@ViewScoped
public class PatientPrescriptionsBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<Prescription> prescriptions;
    
    @EJB
    private PrescriptionService prescriptionService;
    @EJB
    private BillingService billingService;
    @EJB
    private PatientDao patientDao;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;
    
    /**
     * It initializes the prescription list and loads the prescriptions 
     * belonging to the currently logged-in patient.
     */
    @PostConstruct
    public void init() {
        Patient patient = patientDao.findByUserId(loggedInUser.getUser().getId());
        
        prescriptions = new ArrayList<>();
        
        if (patient == null) {
            return;
        }
        
        for (Prescription rx : prescriptionService.listForPatient(patient.getId())) {
            if (billingService.isConsultationUnlockedForPatient(rx.getConsultation().getId())) {
                prescriptions.add(rx);
            }
        }
    }
    
    public List<Prescription> getPrescriptions(){
        return prescriptions;
    }
    
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    
    
    
    
}
