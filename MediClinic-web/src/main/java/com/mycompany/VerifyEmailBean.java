/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import service.AuthException;
import service.AuthService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "verifyEmailBean")
@RequestScoped
public class VerifyEmailBean {

    private String token;
    private String resultMessage;
    private boolean success;

    @EJB
    private AuthService authService;

    public void verify() {
        try {
            authService.verifyEmail(token);
            success = true;
            resultMessage = "Your email has been verified. You can now log in.";
        } catch (AuthException e) {
            success = false;
            resultMessage = e.getMessage();
        }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getResultMessage() { return resultMessage; }
    public boolean isSuccess() { return success; }
}
