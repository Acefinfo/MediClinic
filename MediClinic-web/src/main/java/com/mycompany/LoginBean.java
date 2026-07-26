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
    
    public String getEmail(){
        return email;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void setLoggedInUser(LoggedInUser loggedInUser){
        this.loggedInUser = loggedInUser;
    }
    
}
