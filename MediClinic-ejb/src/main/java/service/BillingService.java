/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.InvoiceDao;
import dao.PatientDao;
import dao.PaymentDao;
import entity.ActivityLog;
import entity.Consultation;
import entity.Invoice;
import entity.Invoice.Status;
import entity.Patient;
import entity.Payment;
import entity.Payment.Method;
import entity.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author acefonfo
 */

@Stateless
@PermitAll
public class BillingService {
    
    public static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    
    @EJB
    private InvoiceDao invoiceDao;
    @EJB
    private PaymentDao paymentDao;
    @EJB
    private PatientDao patientDao;
    @EJB
    private ActivityLogDao activityLogDao;
    
    
    /**
     * Auto generates an invoice newly completed consultation.
     * 
     * @param consultation
     * @return 
     */
    public Invoice generateInvoiceForConsultation(Consultation consultation){
        
        Invoice existing = invoiceDao.findByConsultationId(consultation.getId());
        if (existing != null){
            return existing;
        }
        
        BigDecimal fee = consultation.getAppointment().getDoctor().getFee();
        if (fee == null){
            fee = BigDecimal.ZERO;
        }
        
        Invoice invoice = new Invoice();
        invoice.setConsultation(consultation);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        applyFee(invoice, fee);
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setIssuedDate(new Date());
        
        invoiceDao.create(invoice);
        log(null, "GENERATE_INVOICE", "Invoice", invoice.getId(), "Invoice " + invoice.getInvoiceNumber() + " generated for consultation #" + consultation.getId());
        return invoice;
        
    }
    
    /**
     * Recalculates tax and total fee from the consultation fee.
     * @param invoice
     * @param fee 
     */
    private void applyFee(Invoice invoice, BigDecimal fee){
        BigDecimal tax = fee.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        invoice.setConsultationFee(fee);
        invoice.setTax(tax);
        invoice.setTotalAmount(fee.add(tax));
    }
    
    /**
     * Builds a sequential invoice invoice number
     * @return 
     */
    private String generateInvoiceNumber(){
        String prefix = "INV-" + new SimpleDateFormat("yyyyMM").format(new Date()) + "-";
        long count = invoiceDao.countAll() + 1;
        return prefix + String.format("%04d", count);
    }
    
    public Invoice findById(Long id) {
        return invoiceDao.findById(id);
    }

    public List<Invoice> findAllInvoices() {
        return invoiceDao.findAll();
    }

    public List<Invoice> findInvoicesForPatient(Long patientId) {
        return invoiceDao.findByPatientId(patientId);
    }

    public List<Payment> findPaymentsForInvoice(Long invoiceId) {
        return paymentDao.findByInvoiceId(invoiceId);
    }
    
    /**
     * Edits the consultation fee on an invoice. Admin / receptionist only.
     * Blocked once the invoice is PAID or VOID.
     * 
     * @param actor
     * @param invoiceId
     * @param newFee
     * @return
     * @throws AuthException 
     */
    public Invoice updateInvoice(User actor, Long invoiceId, BigDecimal newFee) throws AuthException{
        requireStaff(actor);
        
        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null){
            throw new AuthException("Invoice not found");
        }
        
        if(invoice.getStatus() == Status.PAID || invoice.getStatus() == Status.VOID){
            throw new AuthException("Apaid or void invoice cannot be edited");
        }
        
        if (newFee == null || newFee.compareTo(BigDecimal.ZERO) < 0){
            throw new AuthException("Fee must be positive amount.");
        }
        
        applyFee(invoice, newFee);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);

        log(actor, "UPDATE_INVOICE", "Invoice", invoice.getId(), "Invoice " + invoice.getInvoiceNumber() + " fee updated to " + newFee);
        return invoice;
    }
    
    /**
     * Voids an invoice.
     * Blocked once any payment exists.
     * 
     * @param actor
     * @param invoiceId
     * @return
     * @throws AuthException 
     */
    public Invoice voidInvoice(User actor, Long invoiceId) throws AuthException{
        requireStaff(actor);
        
        Invoice invoice = invoiceDao.findById(invoiceId);
        
        if (invoice == null){
            throw new AuthException("Invoice not fouund.");
        }
        
        if (invoice.getStatus() == Status.VOID){
            throw new AuthException("Invoice is already VOID");
        }
        
        if (!paymentDao.findByInvoiceId(invoiceId).isEmpty()){
            throw new AuthException("An invoice with a recorded payment canot be voided");
        }
        
        invoice.setStatus(Status.VOID);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);

        log(actor, "VOID_INVOICE", "Invoice", invoice.getId(),"Invoice " + invoice.getInvoiceNumber() + " voided");
        return invoice;
        
    }
    
    /**
     * Records a cash payment. Receptionist only. Pays the invoice in full.
     * @param actor
     * @param invoiceId
     * @param transactionRef
     * @return
     * @throws AuthException 
     */
    public Payment payCash(User actor, Long invoiceId, String transactionRef) throws AuthException {
        if (actor == null || actor.getRole() == null || !"RECEPTIONIST".equals(actor.getRole().getName())) {
            throw new AuthException("Only a receptionist can record a cash payment.");
        }
        return recordPayment(actor, invoiceId, Method.CASH, transactionRef);
    }
    
    /**
     * Records online payment made by the payment for their own invoice.
     * 
     * @param actor
     * @param invoiceId
     * @return
     * @throws AuthException 
     */
    public Payment payOnline(User actor, Long invoiceId) throws AuthException {
        if (actor == null || actor.getRole() == null || !"PATIENT".equals(actor.getRole().getName())) {
            throw new AuthException("Only a patient can make an online payment.");
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }

        Patient patient = patientDao.findByUserId(actor.getId());
        Long owningPatientId = invoice.getConsultation().getAppointment().getPatient().getId();
        if (patient == null || !owningPatientId.equals(patient.getId())) {
            throw new AuthException("You can only pay your own invoices.");
        }

        return recordPayment(actor, invoiceId, Method.ONLINE, "ONLINE-" + System.currentTimeMillis());
    }
    
    /**
     * Shared payment recording logic used by both payCash and payOnline
     * @param actor
     * @param invoiceId
     * @param method
     * @param transactionRef
     * @return
     * @throws AuthException 
     */
    private Payment recordPayment(User actor, Long invoiceId, Method method, String transactionRef) throws AuthException {
        
        Invoice invoice = invoiceDao.findById(invoiceId);
        
        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }
        if (invoice.getStatus() == Status.VOID) {
            throw new AuthException("Cannot pay a voided invoice.");
        }
        if (invoice.getStatus() == Status.PAID) {
            throw new AuthException("Invoice is already paid.");
        }
        
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentDate(new Date());
        payment.setTransactionRef(transactionRef);
        paymentDao.create(payment);
        
        invoice.setStatus(Status.PAID);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);
        
         log(actor, "RECORD_PAYMENT", "Payment", payment.getId(),  method + " payment of " + invoice.getTotalAmount() + " recorded for invoice " + invoice.getInvoiceNumber());

        return payment;
        
        
    }

    /**
     * True once the invoice is tied to consultation has been fully paid.
     * This signal is used to unlock patient's consultation and prescription view for patients.
     * @param consultationId
     * @return 
     */
    public boolean isConsultationUnlockedForPatient(Long consultationId) {
        Invoice invoice = invoiceDao.findByConsultationId(consultationId);
        return invoice != null && invoice.getStatus() == Status.PAID;
    }
    
    
    private void requireStaff(User actor) throws AuthException {
        String role = (actor != null && actor.getRole() != null) ? actor.getRole().getName() : "";
        if (!"ADMIN".equals(role) && !"RECEPTIONIST".equals(role)) {
            throw new AuthException("Only Admin or Receptionist can perform this action.");
        }
    }
    
    
    private void log(User actor, String action, String entityName, Long entityId, String details) {
        ActivityLog entry = new ActivityLog();
        entry.setUser(actor);
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        activityLogDao.create(entry);
    }
}
