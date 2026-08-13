/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Invoice;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
public class InvoiceDao {

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new invoice to the database
     *
     * @param invoice
     */
    public void create(Invoice invoice) {
        em.persist(invoice);
    }

    /**
     * UPdates an existing invoice.
     *
     * @param invoice
     * @return
     */
    public Invoice update(Invoice invoice) {
        return em.merge(invoice);
    }

    /**
     * Finds an invoice by its primary key.
     *
     * @param id
     * @return
     */
    public Invoice findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Invoice.class, id);
    }

    /**
     * Finds the invoice tied to a specific consultation, if one exists.
     *
     * @param consultationId
     * @return
     */
    public Invoice findByConsultationId(Long consultationId) {
        try {
            return em.createQuery("SELECT i FROM Invoice i WHERE i.consultation.id = :cid", Invoice.class)
                    .setParameter("cid", consultationId)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the list of all the invoices
     *
     * @return
     */
    public List<Invoice> findAll() {
        try {
            return em.createQuery("SELECT i FROM Invoice i ORDER BY i.issuedDate DESC", Invoice.class)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retrieves all the invoices  belonging to the specific patient
     *
     * @param patientId
     * @return
     */
    public List<Invoice> findByPatientId(Long patientId) {
        return em.createQuery("SELECT i FROM Invoice i WHERE i.consultation.appointment.patient.id = :pid ORDER BY i.issuedDate DESC", Invoice.class)
                .setParameter("pid", patientId)
                .getResultList();
    }

    /**
     * Counts all the invoice ever recorded.
     *
     * @return
     */
    public Long countAll() {
        return em.createQuery("SELECT COUNT(i) FROM Invoice i", Long.class)
                .getSingleResult();
    }

    /**
     * All invoices in given status .
     * 
     * @param status
     * @return 
     */
    public List<Invoice> findByStatus(Invoice.Status status) {
        return em.createQuery("SELECT i FROM Invoice i WHERE i.status = :status ORDER BY i.issuedDate DESC", Invoice.class)
                .setParameter("status", status)
                .getResultList();
    }

    public long countByStatus(Invoice.Status status) {
        return em.createQuery("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    /**
     * Paid revenue per doctor for doctor performance report.
     * Each row is [Doctor, Total amount]
     * 
     * @return 
     */
    public List<Object[]> sumRevenueGroupedByDoctor() {
        return em.createQuery(
                "SELECT c.appointment.doctor, SUM(i.totalAmount) FROM Invoice i JOIN i.consultation c "
                + "WHERE i.status = :status GROUP BY c.appointment.doctor",
                Object[].class)
                .setParameter("status", Invoice.Status.PAID)
                .getResultList();
    }
}
