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

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    public void create(Patient patient) throws Exception {
        em.persist(patient);
    }

    public Patient findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Patient.class, id);
    }

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