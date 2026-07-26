/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.AuthService;

/**
 *
 * @author acefonfo
 */

@ManagedBean(name = "resetPasswordBean")
@RequestScoped
public class ResetPasswordBean {
    
    private String token;
    private String newPassword;
    private String confirmPassword;
    
    @EJB 
    private AuthService authService;
    
    public String reset(){
        
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Reset failed", "Passwords do not match"));
            return null;
        }
        
        try{
            authService.resetPassword(token, newPassword);
            return "login?faces-redirect=true";
            
        } catch (AuthException e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Reset Failed", e.getMessage()));
            return null;
        }
        
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
}
