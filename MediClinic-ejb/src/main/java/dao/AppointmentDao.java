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
import javax.persistence.TemporalType;

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
     *
     * @param appointment
     */
    public void create(Appointment appointment) {
        em.persist(appointment);
    }

    /**
     * Updates an existing appointment.
     *
     * @param appointment
     * @return
     */
    public Appointment update(Appointment appointment) {
        return em.merge(appointment);
    }

    /**
     * Finds an appointment by its primary key.
     *
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
     *
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
     *
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
     *
     * @return
     */
    public List<Appointment> findAll() {
        return em.createQuery(
                "SELECT a FROM Appointment a ORDER BY a.appointmentDate DESC, a.appointmentTime DESC",
                Appointment.class)
                .getResultList();
    }

    /**
     * Retrieves all appointments with a specific status (e.g. the approval
     * queue).
     *
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
     * availability slot on a given date. Used to enforce the slot's
     * max-patients capacity.
     *
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
                .setParameter("date", appointmentDate, TemporalType.DATE)
                .setParameter("statuses", activeStatuses)
                .getSingleResult();
    }

    /**
     * Counts appoints currently in a given status.
     *
     * @param status
     * @return
     */
    public long countByStatus(Status status) {
        return em.createQuery("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    /**
     * All appointment on one calender date.
     *
     * @param date
     * @return
     */
    public List<Appointment> findByDate(Date date) {
        return em.createQuery("SELECT a FROM Appointment a WHERE a.appointmentDate = :date ORDER BY a.appointmentTime", Appointment.class)
                .setParameter("date", date, TemporalType.DATE)
                .getResultList();
    }

    /**
     * Counts a doctor's still upcoming (approved/ rescheduled) appointment from
     * given date onwards.
     *
     * @param doctorId
     * @param fromDate
     * @return
     */
    public long countUpcomingByDoctor(Long doctorId, Date fromDate) {
        List<Status> activeStatuses = Arrays.asList(Status.APPROVED, Status.RESCHEDULED);
        return em.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :fromDate AND a.status IN :statuses", Long.class)
                .setParameter("doctorId", doctorId)
                .setParameter("fromDate", fromDate, TemporalType.DATE)
                .setParameter("statuses", activeStatuses)
                .getSingleResult();
    }

    /**
     * Counts a patients still upcoming appointment from given date.
     *
     * @param patientId
     * @param fromDate
     * @return
     */
    public long countUpcomingByPatient(Long patientId, Date fromDate) {
        List<Status> activeStatuses = Arrays.asList(Status.APPROVED, Status.RESCHEDULED);
        return em.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :fromDate AND a.status IN :statuses", Long.class)
                .setParameter("patientId", patientId)
                .setParameter("fromDate", fromDate, TemporalType.DATE)
                .setParameter("statuses", activeStatuses)
                .getSingleResult();
    }

    /**
     * Completed appointment count per doctor for the doctor performance report.
     * Each row is doctor and count
     *
     * @return
     */
    public List<Object[]> countCompletedGroupedByDoctor() {
        return em.createQuery(
                "SELECT a.doctor, COUNT(a) FROM Appointment a WHERE a.status = :status GROUP BY a.doctor",
                Object[].class)
                .setParameter("status", Status.COMPLETED)
                .getResultList();
    }

    /**
     * A single doctor's appointments on one calender date
     *
     * @param doctorId
     * @param date
     * @return
     */
    public List<Appointment> findByDoctorIdAndDate(Long doctorId, Date date) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date ORDER BY a.appointmentTime",
                Appointment.class)
                .setParameter("doctorId", doctorId)
                .setParameter("date", date, TemporalType.DATE)
                .getResultList();
    }

}
