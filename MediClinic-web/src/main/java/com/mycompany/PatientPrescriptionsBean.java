/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.PatientDao;
import entity.Consultation;
import entity.Patient;
import entity.Prescription;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    
    
    /**
     * Inner class used to group prescription belonging to the same consultation.
     */
    public static class ConsultationGroup implements Serializable{
         private static final long serialVersionUID = 1L;

        private final Consultation consultation;
        private final List<Prescription> prescriptions;
        
        /**
         * Creates a consultationGroup
         * 
         * @param consultation
         * @param prescriptions 
         */
        public ConsultationGroup(Consultation consultation, List<Prescription> prescriptions) {
            this.consultation = consultation;
            this.prescriptions = prescriptions;
        }
        
        /**
         * Returns the consultation associated with this group.
         * 
         * @return 
         */
        public Consultation getConsultation(){
            return consultation;
        }
        
        /***
         * Returns all the prescription associated with the group.
         * 
         * @return 
         */
        public List<Prescription> getPrescriptions() {
            return prescriptions;
        }
    }
    
    private List<ConsultationGroup> groups;

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
    public void init(){
        groups = new ArrayList<>();
        
        Patient patient = patientDao.findByUserId(loggedInUser.getUser().getId());
        if (patient == null) {
            return;
        }
        
        /*
        * Linked hash map is used to group prescription by consultation Id.
        *
        * Key   = COnsultation Id
        * Value = ConsultationGroup
        *
        * LinkedHash map preservs the order to which consultation are added. 
        */
        Map<Long, ConsultationGroup> byConsultation = new LinkedHashMap<>();
        for (Prescription rx: prescriptionService.listForPatient(patient.getId())) {
            
            Consultation c = rx.getConsultation();
            
            if ( !billingService.isConsultationUnlockedForPatient(c.getId())){
                continue;
            }
            
            ConsultationGroup group = byConsultation.get(c.getId());
            if (group == null){
                group = new ConsultationGroup(c, new ArrayList<Prescription>());
                byConsultation.put(c.getId(), group);
                
            }
            group.getPrescriptions().add(rx);
        }
        
        groups.addAll(byConsultation.values());
        
    }
    
    public List<ConsultationGroup> getGroups() {
        return groups;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    
    
    
    
}