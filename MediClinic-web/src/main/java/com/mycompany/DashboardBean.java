/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.ActivityLog;
import entity.Appointment;
import entity.Doctor;
import entity.Patient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import service.DoctorService;
import service.PatientService;
import service.ReportService;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ReportService reportService;
    @EJB
    private DoctorService doctorService;
    @EJB
    private PatientService patientService;

    
    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    // Admin / Receptionist widgets
    private long todayAppointments;
    private long pendingApprovals;
    private long totalPatients;
    private long totalDoctors;
    private BigDecimal revenueThisMonth = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long unpaidInvoices;
    private List<ActivityLog> recentActivity = Collections.emptyList();

    // Doctor widgets
    private List<Appointment> myTodaysAppointments = Collections.emptyList();
    private long myUpcomingAppointments;
    private long myCompletedConsultations;

    // Patient widgets
    private long myUpcomingAppointmentsAsPatient;
    private long myUnpaidInvoices;

    /**
     * Initializes the dashboard when the bean is created.
     * This method checks the user's role and loads only the the 
     * information required for that particular role.
     */
    @PostConstruct
    public void init() {
        if (loggedInUser == null || !loggedInUser.isLoggedIn()) {
            return;
        }
        String role = loggedInUser.getRoleName();


        if ("ADMIN".equals(role) || "RECEPTIONIST".equals(role)) {
            todayAppointments = reportService.countTodayAppointments();
            pendingApprovals = reportService.countPendingApprovals();
            totalPatients = reportService.countTotalPatients();
            totalDoctors = reportService.countTotalDoctors();
            revenueThisMonth = reportService.revenueThisMonth();
            totalRevenue = reportService.totalRevenue();
            unpaidInvoices = reportService.countUnpaidInvoices();
            recentActivity = reportService.recentActivity(8);
        } else if ("DOCTOR".equals(role)) {
            Doctor doctor = doctorService.findByUserId(loggedInUser.getUser().getId());
            if (doctor != null) {
                myTodaysAppointments = reportService.todaysAppointmentsForDoctor(doctor.getId());
                myUpcomingAppointments = reportService.countUpcomingForDoctor(doctor.getId());
                myCompletedConsultations = reportService.countCompletedConsultations(doctor.getId());
            }
        } else if ("PATIENT".equals(role)) {
            Patient patient = patientService.findByUserId(loggedInUser.getUser().getId());
            if (patient != null) {
                myUpcomingAppointmentsAsPatient = reportService.countUpcomingForPatient(patient.getId());
                myUnpaidInvoices = reportService.countUnpaidInvoicesForPatient(patient.getId());
            }
        }
    }

    // getters (no setters needed — read-only widgets)
    public long getTodayAppointments() {
        return todayAppointments;
    }

    public long getPendingApprovals() {
        return pendingApprovals;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public BigDecimal getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getUnpaidInvoices() {
        return unpaidInvoices;
    }

    public List<ActivityLog> getRecentActivity() {
        return recentActivity;
    }

    public List<Appointment> getMyTodaysAppointments() {
        return myTodaysAppointments;
    }

    public long getMyUpcomingAppointments() {
        return myUpcomingAppointments;
    }

    public long getMyCompletedConsultations() {
        return myCompletedConsultations;
    }

    public long getMyUpcomingAppointmentsAsPatient() {
        return myUpcomingAppointmentsAsPatient;
    }

    public long getMyUnpaidInvoices() {
        return myUnpaidInvoices;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
