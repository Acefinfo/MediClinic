/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Payment;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
    public List<Payment> findAll(){
        return em.createQuery("SELECT p FROM Payment P ORDER BY  p.paymentDate DESC")
                .getResultList();
    }
    
    
}
