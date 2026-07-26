/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import service.AuthService;

/**
 * Managed bean responsible for handling password reset requests.
 *
 * Allows users to submit their email address to receive a password
 * reset link through the authentication service.
 *
 * @author acefonfo
 */
@ManagedBean(name = "forgotPasswordBean")
@RequestScoped
public class ForgotPasswordBean {

    private String email;
    private boolean submitted;

    @EJB
    private AuthService authService;

    /**
     * Submits a password reset request for the provided email address.
     *
     * Calls the authentication service to generate and send a reset link,
     * then updates the submitted status to indicate that the request
     * has been processed.
     *
     * @return null to remain on the current page.
     */
    public String submit() {
        authService.requestPasswordReset(email);
        submitted = true;
        return null;
    }

    /**
     * Retrieves the email address entered by the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the user's email address.
     *
     * @param email The email address provided by the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks whether the password reset request has been submitted.
     *
     * @return true if the request has been submitted; otherwise false.
     */
    public boolean isSubmitted() {
        return submitted;
    }
}