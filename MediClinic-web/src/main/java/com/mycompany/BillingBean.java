/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Invoice;
import entity.Payment;
import java.io.Serializable;
import java.math.BigDecimal;
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

@ManagedBean(name = "billingBean")
@ViewScoped
public class BillingBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<Invoice> invoices;
    
    private Invoice selectedInvoice;
    private List<Payment> selectedPayments;
    private BigDecimal editFee;
    private String cashTransactionRef;
    
    @EJB
    private BillingService billingService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    @PostConstruct
    public void init() {
        loadAll();
    }

    public void loadAll() {
        invoices = billingService.findAllInvoices();
    }
    
    
        /**
         * Selects an invoice and loads its related information.
         * @param invoice 
         */
        public void select(Invoice invoice) {
        selectedInvoice = invoice;
        editFee = invoice.getConsultationFee();
        cashTransactionRef = null;
        selectedPayments = billingService.findPaymentsForInvoice(invoice.getId());
    }

    /**
     * Saves the updated consultation fee for the selected invoice
     * 
     * The logged-in user's information is passed to the service so that the authorization can be checked.
     */
    public void saveFee() {
        try {
            selectedInvoice = billingService.updateInvoice(loggedInUser.getUser(), selectedInvoice.getId(), editFee);
            loadAll();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Invoice updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Voids the selected invoice
     * Avoid invoice is no longer treated as an active invoice.
     */
    public void voidSelected() {
        try {
            selectedInvoice = billingService.voidInvoice(loggedInUser.getUser(), selectedInvoice.getId());
            loadAll();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Invoice voided.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

   
    /**
     * Records a cash payment for the selected invoice.
     */
    public void payCash() {
        try {
            billingService.payCash(loggedInUser.getUser(), selectedInvoice.getId(), cashTransactionRef);
            select(billingService.findById(selectedInvoice.getId()));
            loadAll();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Cash payment recorded. Invoice marked PAID.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }
    
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    public List<Payment> getSelectedPayments() {
        return selectedPayments;
    }

    public BigDecimal getEditFee() {
        return editFee;
    }

    public void setEditFee(BigDecimal editFee) {
        this.editFee = editFee;
    }

    public String getCashTransactionRef() {
        return cashTransactionRef;
    }

    public void setCashTransactionRef(String cashTransactionRef) {
        this.cashTransactionRef = cashTransactionRef;
    }

    /**
     * Injects the currently logged-in user bean.
     * @param loggedInUser 
     */
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
