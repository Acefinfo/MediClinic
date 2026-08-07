/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Prescription;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class PrescriptionDao {

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new prescription line to the database.
     * @param prescription
     */
    public void create(Prescription prescription) {
        em.persist(prescription);
    }

    /**
     * Finds a prescription by its primary key.
     * @param id
     * @return
     */
    public Prescription findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Prescription.class, id);
    }

    /**
     * Retrieves all prescriptions recorded under a specific consultation.
     * @param consultationId
     * @return
     */
    public List<Prescription> findByConsultationId(Long consultationId) {
        return em.createQuery("SELECT p FROM Prescription p WHERE p.consultation.id = :consultationId ORDER BY p.id",Prescription.class)
                .setParameter("consultationId", consultationId)
                .getResultList();
    }

    /**
     * Retrieves every prescription ever issued to a specific patient, across all their consultations.
     * @param patientId
     * @return
     */
    public List<Prescription> findByPatientId(Long patientId) {
        return em.createQuery("SELECT p FROM Prescription p WHERE p.consultation.appointment.patient.id = :patientId ORDER BY p.consultation.consultationDate DESC",Prescription.class)
                .setParameter("patientId", patientId)
                .getResultList();
    }
}