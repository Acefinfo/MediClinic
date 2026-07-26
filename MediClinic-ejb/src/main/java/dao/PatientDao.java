/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Patient;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class PatientDao {
    
    // EntityManager used to interact with the persistence context.
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new patient record to the database.
     *
     * @param patient
     * @throws Exception 
     */
    public void create(Patient patient) throws Exception {
        em.persist(patient);
    }

    /**
     * Retrieves a patient by their unique id
     * @param id
     * @return 
     */
    public Patient findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Patient.class, id);
    }

    /**
     * Retrieves patient by their phone number. 
     * @param phone
     * @return 
     */
    public Patient findByPhone(String phone) {
        try {
            return em.createQuery("SELECT p FROM Patient p WHERE p.phone = :phone", Patient.class)
                    .setParameter("phone", phone)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}