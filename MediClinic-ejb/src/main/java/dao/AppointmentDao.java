/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Appointment;
import entity.Appointment.Status;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class AppointmentDao {

    @PersistenceContext(unitName = "um_mediclinicdb")
    private EntityManager em;

    /**
     * Saves a new appointment to the database.
     * @param appointment
     */
    public void create(Appointment appointment) {
        em.persist(appointment);
    }

    /**
     * Updates an existing appointment.
     * @param appointment
     * @return
     */
    public Appointment update(Appointment appointment) {
        return em.merge(appointment);
    }

    /**
     * Finds an appointment by its primary key.
     * @param id
     * @return
     */
    public Appointment findById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Appointment.class, id);
    }

    /**
     * Retrieves all appointments belonging to a specific patient.
     * @param patientId
     * @return
     */
    public List<Appointment> findByPatientId(Long patientId) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.patient.id = :patientId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC", Appointment.class)
                .setParameter("patientId", patientId)
                .getResultList();
    }

    /**
     * Retrieves all appointments belonging to a specific doctor.
     * @param doctorId
     * @return
     */
    public List<Appointment> findByDoctorId(Long doctorId) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC", Appointment.class)
                .setParameter("doctorId", doctorId)
                .getResultList();
    }

    /**
     * Retrieves every appointment in the system (Admin/Receptionist view).
     * @return
     */
    public List<Appointment> findAll() {
        return em.createQuery(
                "SELECT a FROM Appointment a ORDER BY a.appointmentDate DESC, a.appointmentTime DESC",
                Appointment.class)
                .getResultList();
    }

    /**
     * Retrieves all appointments with a specific status (e.g. the approval queue).
     * @param status
     * @return
     */
    public List<Appointment> findByStatus(Status status) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentDate, a.appointmentTime",
                Appointment.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * Counts active (non-cancelled, non-completed) bookings against a specific
     * availability slot on a given date. Used to enforce the slot's max-patients capacity.
     * @param scheduleId
     * @param appointmentDate
     * @return
     */
    public long countActiveBookings(Long scheduleId, Date appointmentDate) {
        List<Status> activeStatuses = Arrays.asList(Status.REQUESTED, Status.APPROVED, Status.RESCHEDULED);
        return em.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.schedule.id = :scheduleId AND a.appointmentDate = :date AND a.status IN :statuses",
                Long.class)
                .setParameter("scheduleId", scheduleId)
                .setParameter("date", appointmentDate)
                .setParameter("statuses", activeStatuses)
                .getSingleResult();
    }
}
