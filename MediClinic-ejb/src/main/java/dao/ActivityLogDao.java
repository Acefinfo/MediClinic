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

    public void create(ActivityLog log) {
        em.persist(log);
    }

    public List<ActivityLog> findRecent(int maxResults) {
        return em.createQuery("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC", ActivityLog.class)
                .setMaxResults(maxResults)
                .getResultList();
    }
}
