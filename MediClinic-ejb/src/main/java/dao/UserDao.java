/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.User;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class UserDao {

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    public void create(User user) {
        em.persist(user);
    }

    public User update(User user) {
        return em.merge(user);
    }

    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(User.class, id);
    }

    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE lower(u.email) = lower(:email)", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }
}