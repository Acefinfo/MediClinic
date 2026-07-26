/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.PasswordReset;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class PasswordResetDao {

    // EntityManager used to interact with the persistence context.
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new password reset record to the database.
     * @param pr 
     */
    public void create(PasswordReset pr) {
        em.persist(pr);
    }

    /**
     * Updates an existing password reset record.
     * @param pr
     * @return 
     */
    public PasswordReset update(PasswordReset pr) {
        return em.merge(pr);
    }

    /**
     * Retrieves a password reset record using its token 
     * @param token
     * @return 
     */
    public PasswordReset findByToken(String token) {
        try {
            return em.createQuery("SELECT p FROM PasswordReset p WHERE p.token = :token", PasswordReset.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
