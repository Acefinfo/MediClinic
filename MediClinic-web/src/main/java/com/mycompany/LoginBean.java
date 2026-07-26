/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.User;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.AuthService;

/**
 *
 * @author acefonfo
 */

@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginBean {
    
    private String email;
    private String password;

    @EJB
    private AuthService authService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;
    
    /**
     * Authenticate a user using the provided email and password
     * If the authenticate is successful the user information is stored in the logged in user session 
     * and the user is redirected to the dashboard.
     * If authentication fails, an error message is displayed.
     * 
     * @return dashboard URL if successful or null if authentication fails 
     */
    public String login(){
        try{
            User user = authService.login(email, password);
            loggedInUser.setUser(user);
            return "dashboard?faces-redirect=true";
        }catch (AuthException e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", e.getMessage()));
            return null;
        }
    }
    
    /**
     * Retrieves the email address entered by the user.
     *
     * @return the user's email address
     */
    public String getEmail(){
        return email;
    }
    
    /**
     * Updates the email address used for authentication.
     *
     * @param email the user's email address
     */
    public void setEmail(String email){
        this.email = email;
    }
    
    /**
     * Retrieves the password entered by the user.
     *
     * @return the user's password
     */
    public String getPassword(){
        return password;
    }
    
    /**
     * Updates the password used for authentication.
     *
     * @param password the user's password
     */
    public void setPassword(String password){
        this.password = password;
    }
    
    /**
     * Sets the currently logged-in user object.
     *
     * @param loggedInUser the object containing the current user's session information
     */
    public void setLoggedInUser(LoggedInUser loggedInUser){
        this.loggedInUser = loggedInUser;
    }
}