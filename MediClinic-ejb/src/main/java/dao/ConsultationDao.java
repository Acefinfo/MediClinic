/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Consultation;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class ConsultationDao {
    
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;
    
    /**
     * Saves a new consultation record to the database.
     * @param consultation 
     */
    public void create(Consultation consultation){
        em.persist(consultation);
    }
    
    /**
     * Updates an existing consultation records. 
     * @param consultation
     * @return 
     */
    public Consultation update(Consultation consultation){
        return em.merge(consultation);
    }
    
    /**
     * Find an existing consultation by its primary key.
     * @param id
     * @return 
     */
    public Consultation findById(Long id){
        if (id == null){
            return null;
        }
        return em.find(Consultation.class, id);
    }
    /**
     * Finds the consultation tied to a specific appointment, if one exists.
     * @param appointmentId
     * @return 
     */
    public Consultation findByAppointmentId(Long appointmentId){
        try{
            return em.createQuery("SELECT c FROM Consultation c WHERE c.appointment.id = :appointmentId", Consultation.class)
                    .setParameter("appointmentId", appointmentId)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }
    
    /**
     * Retrieves all consultations recorded by a specific doctor.
     * @param doctorId
     * @return
     */
    public List<Consultation> findByDoctorId(Long doctorId) {
        return em.createQuery(
                "SELECT c FROM Consultation c WHERE c.appointment.doctor.id = :doctorId ORDER BY c.consultationDate DESC", Consultation.class)
                .setParameter("doctorId", doctorId)
                .getResultList();
    }
    
    /**
     * Retrieves every consultation in the system.
     * @return
     */
    public List<Consultation> findAll() {
        return em.createQuery(
                "SELECT c FROM Consultation c ORDER BY c.consultationDate DESC", Consultation.class)
                .getResultList();
    }
    
}

