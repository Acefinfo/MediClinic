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

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    public void create(PasswordReset pr) {
        em.persist(pr);
    }

    public PasswordReset update(PasswordReset pr) {
        return em.merge(pr);
    }

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
