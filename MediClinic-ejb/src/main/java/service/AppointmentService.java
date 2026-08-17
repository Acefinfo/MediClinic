/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.AppointmentDao;
import dao.DoctorDao;
import dao.DoctorScheduleDao;
import dao.PatientDao;
import entity.ActivityLog;
import entity.Appointment;
import entity.Appointment.Status;
import entity.Doctor;
import entity.DoctorSchedule;
import entity.Patient;
import entity.User;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class AppointmentService {

    @EJB
    private AppointmentDao appointmentDao;
    @EJB
    private DoctorDao doctorDao;
    @EJB
    private DoctorScheduleDao doctorScheduleDao;
    @EJB
    private PatientDao patientDao;
    @EJB
    private ActivityLogDao activityLogDao;

    public List<Doctor> listDoctors() {
        return doctorDao.findAll();
    }

    public List<DoctorSchedule> listSchedulesForDoctor(Long doctorId) {
        return doctorScheduleDao.findByDoctorId(doctorId);
    }

    /**
     * Books a new appointment for a patient. Steps: 1. Verify patient profile.
     * 2. Validate doctor and schedule. 3. Validate date/time. 4. Check schedule
     * capacity. 5. Save appointment. 6. Record activity log.
     *
     * @param actor
     * @param doctorId
     * @param scheduleId
     * @param appointmentDate
     * @param appointmentTime
     * @param reason
     * @return
     * @throws AuthException
     */
    public Appointment bookAppointment(User actor, Long doctorId, Long scheduleId, Date appointmentDate,
            Date appointmentTime, String reason) throws AuthException {

        Patient patient = requirePatientForUser(actor.getId());
        Doctor doctor = doctorDao.findById(doctorId);
        if (doctor == null) {
            throw new AuthException("Doctor not found.");
        }
        DoctorSchedule schedule = doctorScheduleDao.findById(scheduleId);
        if (schedule == null || !schedule.getDoctor().getId().equals(doctorId)) {
            throw new AuthException("Availability slot not found for this doctor.");
        }
        validateSlot(schedule, appointmentDate, appointmentTime);
        ensureCapacity(schedule, appointmentDate, null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSchedule(schedule);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setReason(reason);
        appointment.setStatus(Status.REQUESTED);
        appointmentDao.create(appointment);

        log(actor, "BOOK_APPOINTMENT", "Appointment", appointment.getId(), "Booked with Dr. " + doctor.getName());
        return appointment;
    }

    /**
     * Approves a pending appointment. Only appointments with REQIESTED status
     * cane be approved.
     *
     * @param actor
     * @param appointmentId
     * @throws AuthException
     */
    public void approveAppointment(User actor, Long appointmentId) throws AuthException {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment.getStatus() != Status.REQUESTED) {
            throw new AuthException("Only pending appointments can be approved.");
        }
        appointment.setStatus(Status.APPROVED);
        appointmentDao.update(appointment);
        log(actor, "APPROVE_APPOINTMENT", "Appointment", appointmentId, "Appointment approved");
    }

    public Appointment bookAppointmentForPaitent(User actor, Long patientId, Long doctorId, Long scheduleId, Date appointmentDate, Date appointmentTime, String reason) throws AuthException {

        Patient patient = patientDao.findById(patientId);
        if (patient == null) {
            throw new AuthException("Patient not found.");
        }
        Doctor doctor = doctorDao.findById(doctorId);
        if (doctor == null) {
            throw new AuthException("Doctor not found.");
        }
        DoctorSchedule schedule = doctorScheduleDao.findById(scheduleId);
        if (schedule == null || !schedule.getDoctor().getId().equals(doctorId)) {
            throw new AuthException("Availability slot not found for this doctor.");
        }
        validateSlot(schedule, appointmentDate, appointmentTime);
        ensureCapacity(schedule, appointmentDate, null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSchedule(schedule);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setReason(reason);
        appointment.setStatus(Status.REQUESTED);
        appointmentDao.create(appointment);

        log(actor, "BOOK_APPOINTMENT_FOR_PATIENT", "Appointment", appointment.getId(), "Booked appointment for " + patient.getName() + " with Dr. " + doctor.getName());
        return appointment;

    }

    /**
     * Cancels an appointment.
     *
     * Patients are only allowed to cancel their own appointments. Cancelled or
     * completed appointments cannot be cancelled again.
     *
     * @param actor
     * @param appointmentId
     * @throws AuthException
     */
    public void cancelAppointment(User actor, Long appointmentId) throws AuthException {
        Appointment appointment = requireAppointment(appointmentId);
        if ("PATIENT".equals(roleName(actor))) {
            Patient patient = requirePatientForUser(actor.getId());
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new AuthException("You can only cancel your own appointments.");
            }
        }
        if (appointment.getStatus() == Status.CANCELLED || appointment.getStatus() == Status.COMPLETED) {
            throw new AuthException("This appointment can no longer be cancelled.");
        }
        appointment.setStatus(Status.CANCELLED);
        appointmentDao.update(appointment);
        log(actor, "CANCEL_APPOINTMENT", "Appointment", appointmentId, "Appointment cancelled");
    }

    /**
     * Reschedules an existing appointment.
     *
     * @param actor
     * @param appointmentId
     * @param newScheduleId
     * @param newDate
     * @param newTime
     * @throws AuthException
     */
    public void rescheduleAppointment(User actor, Long appointmentId, Long newScheduleId, Date newDate, Date newTime)
            throws AuthException {

        Appointment appointment = requireAppointment(appointmentId);
        if ("PATIENT".equals(roleName(actor))) {
            Patient patient = requirePatientForUser(actor.getId());
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new AuthException("You can only reschedule your own appointments.");
            }
        }
        if (appointment.getStatus() == Status.CANCELLED || appointment.getStatus() == Status.COMPLETED) {
            throw new AuthException("This appointment can no longer be rescheduled.");
        }

        DoctorSchedule schedule = doctorScheduleDao.findById(newScheduleId);
        if (schedule == null || !schedule.getDoctor().getId().equals(appointment.getDoctor().getId())) {
            throw new AuthException("Availability slot not found for this doctor.");
        }
        validateSlot(schedule, newDate, newTime);

        // Exclude this appointment's own current booking from the capacity count
        // if it's staying on the same slot/date, since it already occupies that spot.
        boolean sameSlotSameDate = appointment.getSchedule() != null
                && appointment.getSchedule().getId().equals(newScheduleId)
                && sameDay(appointment.getAppointmentDate(), newDate);
        ensureCapacity(schedule, newDate, sameSlotSameDate ? appointmentId : null);

        appointment.setSchedule(schedule);
        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(Status.REQUESTED);
        appointmentDao.update(appointment);

        log(actor, "RESCHEDULE_APPOINTMENT", "Appointment", appointmentId, "Appointment rescheduled");
    }

    public List<Appointment> listForPatient(Long patientId) {
        return appointmentDao.findByPatientId(patientId);
    }

    public List<Appointment> listForDoctor(Long doctorId) {
        return appointmentDao.findByDoctorId(doctorId);
    }

    public List<Appointment> listAll() {
        return appointmentDao.findAll();
    }

    public List<Appointment> listPending() {
        return appointmentDao.findByStatus(Status.REQUESTED);
    }

    // ---- helpers ----
    /**
     * Validates appointment date , day, time
     *
     * @param schedule
     * @param date
     * @param time
     * @throws AuthException
     */
    private void validateSlot(DoctorSchedule schedule, Date date, Date time) throws AuthException {
        if (date == null || time == null) {
            throw new AuthException("Please choose a date and time.");
        }
        if (isPastDate(date)) {
            throw new AuthException("You cannot book an appointment in the past.");
        }
        DoctorSchedule.DayOfWeek requestedDay = dayOfWeekOf(date);
        if (schedule.getDayOfWeek() != requestedDay) {
            throw new AuthException("The doctor is not available on that day for the selected slot.");
        }
        int t = timeOfDay(time);
        if (t < timeOfDay(schedule.getStartTime()) || t >= timeOfDay(schedule.getEndTime())) {
            throw new AuthException("The selected time is outside the doctor's available hours.");
        }
    }

    /**
     * Ensures the selected schedule has not reached its maximum capacity.
     *
     * @param schedule
     * @param date
     * @param excludeAppointmentId
     * @throws AuthException
     */
    private void ensureCapacity(DoctorSchedule schedule, Date date, Long excludeAppointmentId) throws AuthException {
        long booked = appointmentDao.countActiveBookings(schedule.getId(), date);
        if (excludeAppointmentId != null) {
            booked -= 1;
        }
        if (booked >= schedule.getMaxPatients()) {
            throw new AuthException("This slot is fully booked on the selected date. Please choose another date or time.");
        }
    }

    /**
     * Checks weather the supplied date is before today.
     *
     * @param date
     * @return
     */
    private boolean isPastDate(Date date) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return date.before(today.getTime());
    }

    /**
     * Converts a date into the application's DayOfWeek enum.
     *
     * @param date
     * @return
     */
    private DoctorSchedule.DayOfWeek dayOfWeekOf(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday ... 7=Saturday
        return DoctorSchedule.DayOfWeek.values()[day - 1];
    }

    /**
     * Determines weather two dates falls on the same calender day.
     *
     * @param a
     * @param b
     * @return
     */
    private boolean sameDay(Date a, Date b) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Converts Date object into minutes since midnight
     *
     * @param date
     * @return
     */
    private int timeOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    /**
     * Returns the role name of the logged-in user.
     *
     * @param user
     * @return
     */
    private String roleName(User user) {
        return (user.getRole() != null) ? user.getRole().getName() : null;
    }

    /**
     * Retrieves the patient profile associated with the user account
     *
     * @param userId
     * @return
     * @throws AuthException
     */
    private Patient requirePatientForUser(Long userId) throws AuthException {
        Patient patient = patientDao.findByUserId(userId);
        if (patient == null) {
            throw new AuthException("No patient profile found for this account.");
        }
        return patient;
    }

    /**
     * Retrieves appointment by Id or throws exception
     *
     * @param appointmentId
     * @return
     * @throws AuthException
     */
    private Appointment requireAppointment(Long appointmentId) throws AuthException {
        Appointment appointment = appointmentDao.findById(appointmentId);
        if (appointment == null) {
            throw new AuthException("Appointment not found.");
        }
        return appointment;
    }

    /**
     * Creates an audit log for appointment related actions.
     *
     * @param actor
     * @param action
     * @param entityName
     * @param entityId
     * @param details
     */
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
