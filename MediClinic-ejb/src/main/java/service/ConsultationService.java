/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.AppointmentDao;
import dao.ConsultationDao;
import entity.ActivityLog;
import entity.Appointment;
import entity.Appointment.Status;
import entity.Consultation;
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
public class ConsultationService {
    
    @EJB
    private AppointmentDao appointmentDao;
    @EJB
    private ConsultationDao consultationDao;
    @EJB
    private ActivityLogDao activityLogDao;
    @EJB
    private BillingService billingService;
    
    /**
     * Loads the appointment 
     * @param appointmentId
     * @return
     * @throws AuthException 
     */
    public Appointment findAppointment(Long appointmentId) throws AuthException{
        Appointment appointment = appointmentDao.findById(appointmentId);
        if (appointment == null){
            throw new AuthException("Appointment not found.");
        }
        return appointment;
    }
   
    /**
     * Retrieves the consultation associated with specific appointment.
     * 
     * @param appointmentId
     * @return 
     */
    public Consultation findConsultationForAppointment(Long appointmentId){
        return consultationDao.findByAppointmentId(appointmentId);
    }
    
    /**
     * Creates a new consultation or update an existing one.
     * Only assigned doctor can perform this action.
     * THe appointment must be approved or already be completed.
     * @param actor
     * @param appointmentId
     * @param symptoms
     * @param diagnosis
     * @param notes
     * @return
     * @throws AuthException 
     */
    public Consultation saveConsultation(User actor, Long appointmentId, String symptoms, String diagnosis,String notes) throws AuthException {
        
        Appointment appointment = appointmentDao.findById(appointmentId);
        
        if (appointment == null){
            throw new AuthException("Appointment now found");
        }
        
        if (appointment.getDoctor().getUser() == null || !appointment.getDoctor().getUser().getId().equals(actor.getId())) {
            throw new AuthException("You can only record a consultation for your own appointments.");
        }
        
        if (appointment.getStatus() != Status.APPROVED && appointment.getStatus() != Status.COMPLETED) {
            throw new AuthException("A consultation can only be started from an approved appointment.");
        }
        
        Consultation consultation = consultationDao.findByAppointmentId(appointmentId);
        boolean isNew = (consultation == null);
        
        if (isNew){
            consultation = new Consultation();
            consultation.setAppointment(appointment);
        }
        consultation.setSymptoms(symptoms);
        consultation.setDiagnosis(diagnosis);
        consultation.setNotes(notes);
        
        
        if (isNew){
            consultationDao.create(consultation);
        } else{
            consultationDao.update(consultation);
        }
        
        if (appointment.getStatus() != Status.COMPLETED) {
            appointment.setStatus(Status.COMPLETED);
            appointmentDao.update(appointment);
        }

        billingService.generateInvoiceForConsultation(consultation);

        log(actor, isNew ? "START_CONSULTATION" : "UPDATE_CONSULTATION", "Consultation", consultation.getId(), "Consultation recorded for appointment #" + appointmentId);

        return consultation;
        
    }
    

    public List<Consultation> listPatientHistoryForDoctor(User actor, Long patientId) throws AuthException {
        boolean hasRelationship = false;
        for (Appointment a : appointmentDao.findByPatientId(patientId)) {
            if (a.getDoctor() != null && a.getDoctor().getUser() != null
                    && a.getDoctor().getUser().getId().equals(actor.getId())) {
                hasRelationship = true;
                break;
            }
        }
        if(!hasRelationship){
            throw new AuthException("You can only view medical history for patients you have treated.");
        }
        return consultationDao.findByPatientId(patientId);
    }
    /**
     * Creates an activity log entry for adding user action. 
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
