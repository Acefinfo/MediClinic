/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

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
import service.AuthException;
import service.ConsultationService;
import service.PatientService;
import service.PrescriptionService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "doctorPatientHistoryBean")
@ViewScoped
public class DoctorPatientHistoryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Groups the prescriptions written for a single consultation, so the
     * page can list them together.
     */
    public static class ConsultationGroup implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Consultation consultation;
        private final List<Prescription> prescriptions;

        public ConsultationGroup(Consultation consultation, List<Prescription> prescriptions) {
            this.consultation = consultation;
            this.prescriptions = prescriptions;
        }

        public Consultation getConsultation() {
            return consultation;
        }

        public List<Prescription> getPrescriptions() {
            return prescriptions;
        }
    }

    private Long patientId;
    private Patient patient;
    private List<ConsultationGroup> groups = new ArrayList<>();
    private boolean accessDenied;
    private String accessDeniedMessage;

    @EJB
    private ConsultationService consultationService;
    @EJB
    private PrescriptionService prescriptionService;
    @EJB
    private PatientService patientService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Loads the patient's profile and full consultation/prescription
     * history.
     */
    @PostConstruct
    public void load() {
        if (patientId == null) {
            accessDenied = true;
            accessDeniedMessage = "No patient specified.";
            return;
        }

        try {
            List<Consultation> consultations = consultationService.listPatientHistoryForDoctor(
                    loggedInUser.getUser(), patientId);

            patient = patientService.findById(patientId);

            Map<Long, ConsultationGroup> byConsultation = new LinkedHashMap<>();
            for (Consultation c : consultations) {
                List<Prescription> prescriptions = prescriptionService.listForConsultation(c.getId());
                byConsultation.put(c.getId(), new ConsultationGroup(c, prescriptions));
            }
            groups.addAll(byConsultation.values());

        } catch (AuthException e) {
            accessDenied = true;
            accessDeniedMessage = e.getMessage();
        }
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Patient getPatient() {
        return patient;
    }

    public List<ConsultationGroup> getGroups() {
        return groups;
    }

    public boolean isAccessDenied() {
        return accessDenied;
    }

    public String getAccessDeniedMessage() {
        return accessDeniedMessage;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}