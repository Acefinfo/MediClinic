/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.DoctorSchedule;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class DoctorScheduleDao {
    
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;
    
    /**
     * Saves a new availability slot to the database
     * 
     * @param schedule 
     */
    public void create(DoctorSchedule schedule) {
        em.persist(schedule);
    }
    
    /**
     * Update an existing availability slot. 
     * 
     * @param schedule
     * @return 
     */
    public DoctorSchedule update(DoctorSchedule schedule){
        return em.merge(schedule);
    }
    
    /**
     * Finds an availability slot by its primary key.
     * 
     * @param id
     * @return
     */
    public DoctorSchedule findById(Long id){
        if(id == null){
            return null;
        }
        return em.find(DoctorSchedule.class, id);
    }
    
    /**
     * Deletes an availability slot by its primary key.
     * 
     * @param id
     */
    public void delete(Long id){
        DoctorSchedule schedule = findById(id);
        if (schedule != null){
            em.remove(
                    em.contains(schedule) ? schedule : em.merge(schedule)
            );
        }    
    }
    
    /**
     * Retrieves all availability slots for a given doctor.
     * 
     * @param doctorId
     * @return
     */
    public List<DoctorSchedule> findByDoctorId(Long doctorId){
        return em.createQuery("SELECT s FROM DoctorSchedule s WHERE s.doctor.id = :doctorId ORDER BY s.dayOfWeek, s.startTime", DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .getResultList();
    }
    
    /**
     * Retrieves every availability slot in the system (admin view).
     * 
     * @return
     */
    public List <DoctorSchedule> findAll(){
        return em.createQuery("SELECT s FROM DoctorSchedule s ORDER BY s.doctor.name, s.dayOfWeek, s.startTime", DoctorSchedule.class)
                .getResultList();
    }
    
    
    
    
}
