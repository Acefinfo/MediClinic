/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.User;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author acefonfo
 */

@ManagedBean(name = "loggedInUser")
@SessionScoped
public class LoggedInUser implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private User user;
    
    public boolean isLoggedIn(){
        return user != null;
    }
    
    public String getRoleName(){
        return (user != null && user.getRole() != null) ? user.getRole().getName() : null;
    }
    
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user = user;
    }
    
    public String logout(){
        user = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login?faces-redirect=true";
    }
    
}
