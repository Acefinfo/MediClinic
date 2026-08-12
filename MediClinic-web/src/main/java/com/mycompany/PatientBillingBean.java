/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.PatientDao;
import entity.Invoice;
import entity.Patient;
import entity.Payment;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.BillingService;

/**
 *
 * @author acefonfo
 */

@ManagedBean(name = "patientBillingBean")
@ViewScoped
public class PatientBillingBean implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private List<Invoice> myInvoices;
    private Invoice selectedInvoice;
    private List<Payment> selectedPayments;

    @EJB
    private BillingService billingService;
    @EJB
    private PatientDao patientDao;
    
    @ManagedProperty(value =  "#{loggedInUser}")
    private LoggedInUser loggedInUser;
    
    @PostConstruct
    public void init(){
        loadMyInvoices();
    }
    
    public void loadMyInvoices(){
//        Patient patient = patientDao.findById(loggedInUser.getUser().getId());
        Patient patient = patientDao.findByUserId(loggedInUser.getUser().getId());
        
        if (patient != null) {
            myInvoices = billingService.findInvoicesForPatient(patient.getId());
        }
    }
    
    public void select(Invoice invoice) {
        selectedInvoice = invoice;
        selectedPayments = billingService.findPaymentsForInvoice(invoice.getId());
    }
    
    public void payOnline(Invoice invoice){
        try{
            billingService.payOnline(loggedInUser.getUser(), invoice.getId());
            loadMyInvoices();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Payment successful.");
        }catch (AuthException e){
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }
    
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    public List<Invoice> getMyInvoices() {
        return myInvoices;
    }

    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    public List<Payment> getSelectedPayments() {
        return selectedPayments;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    
    
}
