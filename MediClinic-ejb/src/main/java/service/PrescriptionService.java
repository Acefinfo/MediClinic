/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.ConsultationDao;
import dao.PrescriptionDao;
import entity.ActivityLog;
import entity.Consultation;
import entity.Prescription;
import entity.User;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author acefonfo
 */
@Stateless 
@PermitAll
public class PrescriptionService {
    
    @EJB
    private PrescriptionDao prescriptionDao;
    @EJB
    private ConsultationDao consultationDao;
    @EJB
    private ActivityLogDao activityLogDao;
    
    /**
     * Add new prescription to new consultation.
     * @param actor
     * @param consultationId
     * @param medicineName
     * @param dosage
     * @param frequency
     * @param duration
     * @param instructions
     * @return
     * @throws AuthException 
     */
    public Prescription addPrescription(User actor, Long consultationId, String medicineName, String dosage, String frequency, String duration, String instructions) throws AuthException{
        
        Consultation consultation = consultationDao.findById(consultationId);
        if(consultation == null){
            throw new AuthException("Consultation not found");
        }
        
        if( consultation.getAppointment().getDoctor().getUser() == null || !consultation.getAppointment().getDoctor().getUser().getId().equals(actor.getId())) {
            throw new AuthException("You can onlt add prescription to your own consultation");
        }
        
        if (medicineName == null || medicineName.trim().isEmpty()) {
            throw new AuthException("Medicine name is required.");
        }
        
        Prescription prescription = new Prescription();
        prescription.setConsultation(consultation);
        prescription.setMedicineName(medicineName.trim());
        prescription.setDosage(dosage);
        prescription.setDuration(duration);
        prescription.setInstructions(instructions);
        prescriptionDao.create(prescription);
        
        log(actor, "ADD_PRESCRIPTION", "Prescription", prescription.getId(), "Added " + medicineName + " to consultation #" + consultationId);
        return prescription;
        
    }
    
    /**
     * Retrieves all prescription associates with a consultation.
     * 
     * @param consultationId
     * @return 
     */
     public List<Prescription> listForConsultation(Long consultationId) {
        return prescriptionDao.findByConsultationId(consultationId);
    }

     /**
      * Retrieves all prescription belonging to a patient
      *  
      * @param patientId
      * @return 
      */
    public List<Prescription> listForPatient(Long patientId) {
        return prescriptionDao.findByPatientId(patientId);
    }
    
    /**
     * Records an activity log in the system.
     * 
     * @param actor
     * @param action
     * @param entityName
     * @param entityId
     * @param details 
     */
    private void log(User actor, String action, String entityName, Long entityId, String details) {
        ActivityLog entry = new ActivityLog();
        entry.setUser(actor);
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        activityLogDao.create(entry);
    }
}
