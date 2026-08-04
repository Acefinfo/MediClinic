/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.DoctorDao;
import dao.DoctorScheduleDao;
import entity.ActivityLog;
import entity.Doctor;
import entity.DoctorSchedule;
import entity.User;
import java.math.BigDecimal;
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
public class DoctorService {
    
    @EJB
    private DoctorDao doctorDao;
    
    @EJB
    private DoctorScheduleDao doctorScheduleDao;
    
    @EJB
    private ActivityLogDao activityLogDao;
    
    public Doctor findByUserId(Long userId){
        return doctorDao.findByUserId(userId);
    }
    
    public Doctor findById(Long id){
        return doctorDao.findById(id);
    }
    
    public List<Doctor> listAll(){
        return doctorDao.findAll();
    }
    
    /**
     * Allows doctor to update their own profile information.
     * 
     * @param actor
     * @param specialization
     * @param fee
     * @param bio
     * @throws AuthException 
     */
    public void updateOwnProfile(User actor, String specialization, BigDecimal fee, String bio) throws AuthException {
        Doctor doctor = requireDoctorForUser(actor.getId());
        doctor.setSpecialization(specialization);
        doctor.setFee(fee);
        doctor.setBio(bio);
        doctorDao.update(doctor);

        log(actor, "UPDATE_DOCTOR_PROFILE", "Doctor", doctor.getId(), "Doctor updated their own profile");
    }
    
    /**
     * Returns all schedules belonging to a specific doctor
     * @param doctorId
     * @return 
     */
    public List<DoctorSchedule> listSchedulesForDoctor(Long doctorId) {
        return doctorScheduleDao.findByDoctorId(doctorId);
    }

    /**
     * Returns every doctor schedule in the system
     * @return 
     */
    public List<DoctorSchedule> listAllSchedules() {
        return doctorScheduleDao.findAll();
    }
    
    /**
     * Create a new availability schedule for the doctor.
     * 
     * @param actor
     * @param dayOfWeek
     * @param startTime
     * @param endTime
     * @param maxPatients
     * @return
     * @throws AuthException 
     */
    public DoctorSchedule addSchedule(User actor, DoctorSchedule.DayOfWeek dayOfWeek, Date startTime, Date endTime, Integer maxPatients) throws AuthException {
        Doctor doctor = requireDoctorForUser(actor.getId());
        validateTimes(startTime, endTime);
        checkOverlap(doctor.getId(), dayOfWeek, startTime, endTime, null);
        
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setMaxPatients(maxPatients);
        doctorScheduleDao.create(schedule);
        
        log(actor, "ADD_SCHEDULE", "DoctorSchedule", schedule.getId(), "Added " + dayOfWeek + " availability for " + doctor.getName());
        return schedule; 
    }
    
    /**
     * Updates an existing schedule belonging to the logged-in doctor.
     * 
     * @param actor
     * @param scheduleId
     * @param dayOfWeek
     * @param startTime
     * @param endTime
     * @param maxPatients
     * @throws AuthException 
     */
    public void updateSchedule(User actor, Long scheduleId, DoctorSchedule.DayOfWeek dayOfWeek, Date startTime,Date endTime, Integer maxPatients) throws AuthException{
        
        Doctor doctor = requireDoctorForUser(actor.getId());
        DoctorSchedule schedule = doctorScheduleDao.findById(scheduleId);
        if (schedule == null || !schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new AuthException("Schedule entry not found.");
        }
        validateTimes(startTime, endTime);
        checkOverlap(doctor.getId(), dayOfWeek, startTime, endTime, scheduleId);
        
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setMaxPatients(maxPatients);
        doctorScheduleDao.update(schedule);

        log(actor, "UPDATE_SCHEDULE", "DoctorSchedule", scheduleId, "Updated availability slot");
    }
    
    /**
     * Deletes one of the doctor's schedules.
     * 
     * @param actor
     * @param scheduleId
     * @throws AuthException 
     */
    public void deleteSchedule(User actor, Long scheduleId) throws AuthException {
        Doctor doctor = requireDoctorForUser(actor.getId());
        DoctorSchedule schedule = doctorScheduleDao.findById(scheduleId);
        if (schedule == null || !schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new AuthException("Schedule entry not found.");
        }
        doctorScheduleDao.delete(scheduleId);

        log(actor, "DELETE_SCHEDULE", "DoctorSchedule", scheduleId, "Removed availability slot");
    }

    /**
     * Validates that both times exist and the start time is before the end time.
     * 
     * @param startTime
     * @param endTime
     * @throws AuthException 
     */
    private void validateTimes(Date startTime, Date endTime) throws AuthException{
        if (startTime == null || endTime == null) {
            throw new AuthException("Start and end time are required");
        }
        if (!startTime.before(endTime)) {
            throw new AuthException("Start time must be before end time.");
        }       
    }
    
    /**
     * Checks whether a new or updated schedule overlaps with any existing schedule on the same day.
     * @param doctorId
     * @param dayOfWeek
     * @param startTime
     * @param endTime
     * @param excludeScheduleId
     * @throws AuthException 
     */
    private void checkOverlap(Long doctorId, DoctorSchedule.DayOfWeek dayOfWeek, Date startTime, Date endTime, Long excludeScheduleId) throws AuthException{
        
        List<DoctorSchedule> existing = doctorScheduleDao.findByDoctorId(doctorId);
        
        for (DoctorSchedule s: existing){
            if (s.getDayOfWeek() != dayOfWeek){
                continue;
            }
            if (excludeScheduleId != null && s.getId().equals(excludeScheduleId)) {
                continue;
            }
            if (timeOfDay(startTime) < timeOfDay(s.getEndTime()) && timeOfDay(s.getStartTime()) < timeOfDay(endTime)){
                throw new AuthException("This time overlaps with an existing " + dayOfWeek + "slot.");
            }
        }
    }
    
    /**
     * Converts a Date object into minutes since midnight.
     * Used for comparing schedule times.
     * 
     * @param date
     * @return 
     */
    private int timeOfDay(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }
    
    /**
     * Retrieves the doctor profile associated with a user.
     * 
     * @param userId
     * @return
     * @throws AuthException 
     */
    private Doctor requireDoctorForUser(Long userId) throws AuthException{
        Doctor doctor = doctorDao.findByUserId(userId);
        if (doctor == null){
            throw new AuthException("No doctor profile  found for this account.");
        }
        return doctor;
    }
    
    /**\
     * Records an action in the system activity log.
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

