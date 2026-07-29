/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.ActivityLog;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */

@Stateless
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
}
