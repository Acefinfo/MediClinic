/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Doctor;
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
public class DoctorDao {
    
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;
    
    public void create(Doctor doctor){
        em.persist(doctor);
    }
    
    public Doctor update(Doctor doctor){
        return em.merge(doctor);
    }
    
    public Doctor findById(Long id){
        if(id == null){
            return null;
        }
        return em.find(Doctor.class, id);
    }
    
    public Doctor findByUserId(Long userId){
        try{
            return em.createQuery("SELECT d FROM Doctor d WHERE d.user.id = :userId", Doctor.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e){
            return null;
        }
    }
    
    public List <Doctor> findAll(){
        return em.createQuery("SELECT d FROM doctor d")
                .getResultList();
    }
}
