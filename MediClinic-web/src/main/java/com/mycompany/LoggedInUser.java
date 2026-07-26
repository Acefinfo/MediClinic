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
    
    /**
     * Checks whether a user is currently logged in.
     *
     * @return true if a user session exists; otherwise false.
     */
    public boolean isLoggedIn(){
        return user != null;
    }
    
    /**
     * Retrieves the role name of the currently logged-in user.
     *
     * @return The user's role name, or null if no user or role exists.
     */
    public String getRoleName(){
        return (user != null && user.getRole() != null) ? user.getRole().getName() : null;
    }
    
    /**
     * Sets the currently logged-in user.
     *
     * @return 
     */
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user = user;
    }
    
    /**
     * Logs out the current user by removing user information
     * and invalidating the session.
     *
     * @return Redirects the user to the login page.
     */
    public String logout(){
        user = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login?faces-redirect=true";
    }
    
}
