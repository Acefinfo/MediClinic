/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Doctor;
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
@PermitAll
@Stateless
public class DoctorDao {

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new doctor entity to the database.
     *
     * @param doctor
     */
    public void create(Doctor doctor) {
        em.persist(doctor);
    }

    /**
     * Updates an existing Doctor entity in the database.
     *
     * @param doctor
     * @return
     */
    public Doctor update(Doctor doctor) {
        return em.merge(doctor);
    }

    /**
     * Find a doctor by its primary key (Id)
     *
     * @param id
     * @return
     */
    public Doctor findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Doctor.class, id);
    }

    /**
     * Finds a Doctor associated with a specific User Id.
     *
     * @param userId
     * @return
     */
    public Doctor findByUserId(Long userId) {
        try {
            return em.createQuery("SELECT d FROM Doctor d WHERE d.user.id = :userId", Doctor.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retrieves all Doctor from the database.
     *
     * @return
     */
    public List<Doctor> findAll() {
        return em.createQuery("SELECT d FROM Doctor d", Doctor.class)
                .getResultList();
    }

    /**
     * Counts total number of doctor
     *
     * @return
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(d) FROM Doctor d", Long.class)
                .getSingleResult();
    }
}
