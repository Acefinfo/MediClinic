/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author martin
 */
@Stateless
@LocalBean
public class NewSessionBean {

    
    @Inject
    private HttpServletRequest request;
    
    @PersistenceContext(unitName="um_mediclinicdb")
    private EntityManager em;
    
    public void businessMethod() {
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

}
