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
@ManagedBean(name = "registerBean")
@RequestScoped
public class RegisterBean {

    private String name;
    private String phone;
    private String email;
    private String password;
    private String confirmPassword;

    @EJB
    private AuthService authService;

    /**
     * Handles the registration process. 
     * Checks password and confirm password matches. When that matches it calls the AuthService For validation.
     * If validation is success it calls AuthUser to create account.
     * @return
     * @throws Exception 
     */
    public String register() throws Exception {
        if (password == null || !password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", "Passwords do not match."));
            return null;
        }
        try {
            authService.registerPatient(email, password, name, phone);
            return "register-success?faces-redirect=true";
        } catch (AuthException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", e.getMessage()));
            return null;
        }
    }

    /**
     * Gets the user's name.
     *
     * @return user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name user's full name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's phone number.
     *
     * @return user's phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone user's phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the user's email address.
     *
     * @return user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email user's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password.
     *
     * @return user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the password confirmation value.
     *
     * @return confirmed password
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the password confirmation value.
     *
     * @param confirmPassword confirmed password
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}