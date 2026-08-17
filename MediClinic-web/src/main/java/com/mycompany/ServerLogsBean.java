/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.ActivityLogDao;
import entity.ActivityLog;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "serverLogsBean")
@ViewScoped
public class ServerLogsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ActivityLogDao activityLogDao;

    private List<ActivityLog> logs;

    /**
     * Loads all activity log entries, newest first, after bean creation.
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * Reloads the log list from the database.
     */
    public void refresh() {
        logs = activityLogDao.findAll();
    }

    public List<ActivityLog> getLogs() {
        return logs;
    }
}