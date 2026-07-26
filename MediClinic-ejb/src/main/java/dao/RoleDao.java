/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Role;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class RoleDao {

    // EntityManager used to interact with the persistence context.
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Save new role to the database.
     * @param role 
     */
    public void create(Role role) {
        em.persist(role);
    }

    /**
     * Retrieves a role by its name.
     * 
     * @param name
     * @return 
     */
    public Role findByName(String name) {
        try {
            return em.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retrieves all roles stored in the database
     * 
     * @return 
     */
    public List<Role> findAll() {
        return em.createQuery("SELECT r FROM Role r", Role.class).getResultList();
    }
}
