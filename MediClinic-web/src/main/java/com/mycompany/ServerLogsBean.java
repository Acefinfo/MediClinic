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
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

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

    private LazyDataModel<ActivityLog> logs;

    /**
     * Builds the lazy data model after bean creation. The actual database query
     * only runs when the data table asks for a page via load(...).
     */
    @PostConstruct
    public void init() {
        logs = new LazyDataModel<ActivityLog>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<ActivityLog> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                setRowCount((int) activityLogDao.countAll());
                return activityLogDao.findPage(first, pageSize);
            }
        };
    }

    public LazyDataModel<ActivityLog> getLogs() {
        return logs;
    }
}
