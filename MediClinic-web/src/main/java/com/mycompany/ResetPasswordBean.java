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
    
    /**
     * Handles the password reset.
     * Checks that the new password matches the
     * confirmation password. If validation succeeds, it calls the
     * authentication service to update the password.
     * Redirect to login page if successful else stay on current page.
     * @return 
     */
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
    
     /** Gets the password reset token.
     *
     * @return reset token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the password reset token.
     *
     * @param token password reset token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the new password.
     *
     * @return new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password.
     *
     * @param newPassword new password entered by the user
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Gets the confirmation password.
     *
     * @return confirmation password
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the confirmation password.
     *
     * @param confirmPassword password confirmation value
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
