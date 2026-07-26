/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.EmailVerification;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class EmailVerificationDao {

    // EntityManager used to interact with the persistence context.
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new email verification record the database.
     * @param ev 
     */
    public void create(EmailVerification ev) {
        em.persist(ev);
    }

    /**
     * Updates the existing email record
     * @param ev
     * @return 
     */
    public EmailVerification update(EmailVerification ev) {
        return em.merge(ev);
    }

    /**
     * Retrieves an email verification record using its token. 
     * @param token
     * @return 
     */
    public EmailVerification findByToken(String token) {
        try {
            return em.createQuery("SELECT e FROM EmailVerification e WHERE e.token = :token", EmailVerification.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
