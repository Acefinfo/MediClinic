/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.Doctor;
import entity.DoctorSchedule;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AuthException;
import service.DoctorService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "doctorScheduleBean")
@ViewScoped
public class DoctorScheduleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Doctor doctor;
    private List<DoctorSchedule> schedules;

    // Add form fields
    private DoctorSchedule.DayOfWeek dayOfWeek;
    private Date startTime;
    private Date endTime;
    private Integer maxPatients = 1;

    // Edit form fields
    private Long editScheduleId;
    private DoctorSchedule.DayOfWeek editDayOfWeek;
    private Date editStartTime;
    private Date editEndTime;
    private Integer editMaxPatients;

    @EJB
    private DoctorService doctorService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the bean after dependency injection.
     * Retrieves the doctor's profile and loads existing schedules.
     */
    @PostConstruct
    public void init() {
        doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
        loadSchedules();
    }

    /**
     * Loads all availability schedules for the logged-in doctor.
     */
    public void loadSchedules() {
        if (doctor != null) {
            schedules = doctorService.listSchedulesForDoctor(doctor.getId());
        }
    }

    /**
     * Adds a new availability schedule.
     * Clears the form and reloads the schedule list after success.
     */
    public void addSchedule() {
        try {
            doctorService.addSchedule(loggedInUser.getUser(), dayOfWeek, startTime, endTime, maxPatients);
            dayOfWeek = null;
            startTime = null;
            endTime = null;
            maxPatients = 1;
            loadSchedules();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Availability slot added.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Populates the edit from with the selected schedule's data.
     * @param schedule 
     */
    public void startEdit(DoctorSchedule schedule) {
        editScheduleId = schedule.getId();
        editDayOfWeek = schedule.getDayOfWeek();
        editStartTime = schedule.getStartTime();
        editEndTime = schedule.getEndTime();
        editMaxPatients = schedule.getMaxPatients();
    }

    /**
     * Saves the changes made to an existing schedule.
     */
    public void saveEdit() {
        try {
            doctorService.updateSchedule(loggedInUser.getUser(), editScheduleId, editDayOfWeek, editStartTime,
                    editEndTime, editMaxPatients);
            loadSchedules();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Availability slot updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }
    /**
     * Deletes the selected availability schedule
     * 
     * @param schedule 
     */
    public void deleteSchedule(DoctorSchedule schedule) {
        try {
            doctorService.deleteSchedule(loggedInUser.getUser(), schedule.getId());
            loadSchedules();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Availability slot removed.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Displays a JSF FacesMessage on the page.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Doctor getDoctor() {
        return doctor;
    }
    public List<DoctorSchedule> getSchedules() {
        return schedules;
    }

    public DoctorSchedule.DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
    public void setDayOfWeek(DoctorSchedule.DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxPatients() {
        return maxPatients;
    }
    public void setMaxPatients(Integer maxPatients) {
        this.maxPatients = maxPatients;
    }

    public Long getEditScheduleId() {
        return editScheduleId;
    }
    public void setEditScheduleId(Long editScheduleId) {
        this.editScheduleId = editScheduleId;
    }

    public DoctorSchedule.DayOfWeek getEditDayOfWeek() {
        return editDayOfWeek;
    }
    public void setEditDayOfWeek(DoctorSchedule.DayOfWeek editDayOfWeek) {
        this.editDayOfWeek = editDayOfWeek;
    }

    public Date getEditStartTime() {
        return editStartTime;
    }
    public void setEditStartTime(Date editStartTime) {
        this.editStartTime = editStartTime;
    }

    public Date getEditEndTime() {
        return editEndTime;
    }
    public void setEditEndTime(Date editEndTime) {
        this.editEndTime = editEndTime;
    }

    public Integer getEditMaxPatients() {
        return editMaxPatients;
    }
    public void setEditMaxPatients(Integer editMaxPatients) {
        this.editMaxPatients = editMaxPatients;
    }

    /**
     * Injects the logged-in user bean.
     */
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
