/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Patient;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

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
     *
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
     *
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

    /**
     * Searches patient by name, phone number or email
     *
     * @return
     */
    public List<Patient> findAll() {
        return em.createQuery("SELECT p from Patient p ORDER BY p.name", Patient.class)
                .getResultList();
    }

    /**
     * Searches patient profile linked to to user id.
     *
     * @param userId
     * @return
     */
    public Patient findByUserId(Long userId) {
        try {
            return em.createQuery("SELECT p FROM Patient p WHERE p.user.id = :userId", Patient.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Searches patients by name, phone, or email (case-insensitive, partial
     * match).
     *
     * @param keyword
     * @return
     */
    public List<Patient> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT p FROM Patient p WHERE lower(p.name) LIKE :kw OR p.phone LIKE :kw "
                + "OR lower(p.user.email) LIKE :kw ORDER BY p.name", Patient.class)
                .setParameter("kw", like)
                .getResultList();
    }

    /**
     * Updates an existing patient record.
     *
     * @param patient
     * @return
     */
    public Patient update(Patient patient) {
        return em.merge(patient);
    }

    /**
     * Counts all the patients.
     *
     * @return
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(p) FROM Patient p", Long.class)
                .getSingleResult();
    }

    /**
     * Counts patients grouped by gender.
     *
     * @return
     */
    public List<Object[]> countGroupedByGender() {
        return em.createQuery("SELECT p.gender, COUNT(p) FROM Patient p GROUP BY p.gender", Object[].class)
                .getResultList();
    }

    /**
     * FInds the registration date of patients registered from specified start
     * date.
     *
     * @param start
     * @return
     */
    public List<Date> findRegistrationDatesSince(Date start) {
        return em.createQuery("SELECT p.user.createdAt FROM Patient p WHERE p.user.createdAt >= :start", Date.class)
                .setParameter("start", start, TemporalType.TIMESTAMP)
                .getResultList();
    }
}
