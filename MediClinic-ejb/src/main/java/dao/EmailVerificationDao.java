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

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    public void create(EmailVerification ev) {
        em.persist(ev);
    }

    public EmailVerification update(EmailVerification ev) {
        return em.merge(ev);
    }

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
