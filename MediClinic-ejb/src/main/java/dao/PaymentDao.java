/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Payment;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

/**
 *
 * @author acefonfo
 */
@Stateless
public class PaymentDao {
    
    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;
    
    /**
     * Saves a new payment to the database.
     * @param payment 
     */
    public void create(Payment payment){
        em.persist(payment);
    }
    
    /**
     * Finds payment by its primary key.
     * @param id
     * @return 
     */
    public Payment findById(Long id){
        if(id == null){
            return null;
        }
        return em.find(Payment.class, id);
    }
    
    /**
     * Retrieves all payment recorded against specific invoice
     * @param invoiceId
     * @return 
     */
    public List<Payment> findByInvoiceId(Long invoiceId){
        return em.createQuery("SELECT p FROM Payment p WHERE p.invoice.id = :iid ORDER BY p.paymentDate DESC", Payment.class)
                .setParameter("iid", invoiceId)
                .getResultList();
                
    }
    
    /**
     * Retrieves every payment in the system.
     * @return 
     */
    public List<Payment> findAll() {
        return em.createQuery("SELECT p FROM Payment p ORDER BY p.paymentDate DESC", Payment.class)
                .getResultList();
    }

    /**
     * Finds all payment made on or after specified start date 
     * The returned payments are ordered by their payment date in ascending order.
     * 
     * @param start
     * @return 
     */
    public List<Payment> findSince(Date start) {
        return em.createQuery("SELECT p FROM Payment p WHERE p.paymentDate >= :start ORDER BY p.paymentDate", Payment.class)
                .setParameter("start", start, TemporalType.TIMESTAMP)
                .getResultList();
    }

    /**
     * Calculates the total amount of payment made between specified start and end date
     * 
     * @param start
     * @param end
     * @return 
     */
    public BigDecimal sumAmountBetween(Date start, Date end) {
        BigDecimal sum = em.createQuery(
                "SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end", BigDecimal.class)
                .setParameter("start", start, TemporalType.TIMESTAMP)
                .setParameter("end", end, TemporalType.TIMESTAMP)
                .getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    /**
     * Calculates the total amount of all payments.
     * 
     * @return 
     */
    public BigDecimal sumAmountAll() {
        BigDecimal sum = em.createQuery("SELECT SUM(p.amount) FROM Payment p", BigDecimal.class)
                .getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

}
