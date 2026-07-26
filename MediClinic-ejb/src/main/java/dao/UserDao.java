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

    // EntityManager used to interact with the persistence context.
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new user to the database.
     *
     * @param user The User entity to be persisted.
     */
    public void create(User user) {
        em.persist(user);
    }

    /**
     * Updates an existing user record.
     *
     * @param user The User entity containing updated information.
     * @return The updated User entity.
     */
    public User update(User user) {
        return em.merge(user);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The user's ID.
     * @return The matching User entity, or null if the ID is null
     *         or no user exists with the given ID.
     */
    public User findById(Long id) {

        // Return null if no ID is provided.
        if (id == null) {
            return null;
        }

        // Find and return the user.
        return em.find(User.class, id);
    }

    /**
     * Retrieves a user using their email address.
     * The search is case-insensitive.
     *
     * @param email The user's email address.
     * @return The matching User entity, or null if no user is found.
     */
    public User findByEmail(String email) {
        try {
            // Execute JPQL query to find the user by email.
            return em.createQuery(
                    "SELECT u FROM User u WHERE lower(u.email) = lower(:email)",
                    User.class)
                    .setParameter("email", email)
                    .getSingleResult();

        } catch (NoResultException e) {
            // Return null if no matching user exists.
            return null;
        }
    }

    /**
     * Checks whether a user already exists with the specified email address.
     *
     * @param email The email address to check.
     * @return true if a matching user exists; otherwise false.
     */
    public boolean existsByEmail(String email) {

        // Reuse the findByEmail() method to determine existence.
        return findByEmail(email) != null;
    }
}