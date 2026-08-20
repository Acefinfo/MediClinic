/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import java.io.Serializable;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language = "en";

    public String getLanguage() {
        return language;
    }

    public Locale getLocale() {
        return "ne".equals(language) ? new Locale("ne") : Locale.ENGLISH;
    }

    public void switchLanguage(String lang) {
        this.language = lang;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());
    }

    public void toggleLanguage() {
        switchLanguage("ne".equals(language) ? "en" : "ne");
    }

    public void toEnglish() {
        switchLanguage("en");
    }

    public void toNepali() {
        switchLanguage("ne");
    }
    
  
    public String getToggleLabel() {
        return "ne".equals(language) ? "EN" : "ने";
    }
    
    
}