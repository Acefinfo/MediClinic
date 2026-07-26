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

    public String submit() {
        authService.requestPasswordReset(email);
        submitted = true;
        return null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSubmitted() {
        return submitted;
    }
}
