/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.ActivityLog;
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
public class ActivityLogDao {
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new activity log record to the database.
     *
     * @param log the ActivityLog entity to be persisted
     */
    public void create(ActivityLog log) {
        em.persist(log);
    }
    
    /**
     * Retrieves the most recent activity logs.
     * Results are sorted by timestamp in descending order
     * (newest records first).
     *
     * @param maxResults the maximum number of records to return
     * @return a list of recent ActivityLog records
     */
    public List<ActivityLog> findRecent(int maxResults) {
        return em.createQuery("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC", ActivityLog.class)
                .setMaxResults(maxResults)
                .getResultList();
    }
    
    /**
     * Retrieves every activity log record, newest first. 
     * Used by the admin server
     * @return 
     */
    public List<ActivityLog> findAll() {
        return em.createQuery("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC", ActivityLog.class)
                .getResultList();
    }
    
    /**
     * Counts all the activity Log recorded by the system.
     * 
     * @return 
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(a) FROM ActivityLog a", Long.class)
                .getSingleResult();
    }
    
    /**
     * Retrieves a single page of activity log records, newest first.
     * 
     * @param first
     * @param pageSize
     * @return 
     */
    public List<ActivityLog> findPage (int first, int pageSize){
        return em.createQuery("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC", ActivityLog.class)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }
}
