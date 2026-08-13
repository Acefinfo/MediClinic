/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.AppointmentDao;
import dao.ConsultationDao;
import dao.DoctorDao;
import dao.InvoiceDao;
import dao.PatientDao;
import dao.PaymentDao;
import entity.ActivityLog;
import entity.Appointment;
import entity.Doctor;
import entity.Invoice;
import entity.Patient;
import entity.Payment;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class ReportService {

    @EJB
    private AppointmentDao appointmentDao;
    @EJB
    private InvoiceDao invoiceDao;
    @EJB
    private PaymentDao paymentDao;
    @EJB
    private PatientDao patientDao;
    @EJB
    private DoctorDao doctorDao;
    @EJB
    private ConsultationDao consultationDao;
    @EJB
    private ActivityLogDao activityLogDao;

    // ---------- date helpers ----------
    
    /**
     * Returns today's data with the time removed
     * @return 
     */
    private Date today() {
        return truncate(new Date());
    }

    /**
     * Removes the time position of a Date object.
     * This method sets hour, minute, seconds and milli-second to zero
     * @param date
     * @return 
     */
    private Date truncate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns the first day of the current month at midnight.
     * 
     * @return 
     */
    private Date startOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * Calculates the first dat of the earliest month in a request period.
     * The current month is included in the calculation.
     * 
     * @param months
     * @return 
     */
    private Date monthsAgo(int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -(months - 1));
        return cal.getTime();
    }

    // ---------- Admin / Receptionist dashboard ----------
    
    public long countTodayAppointments() {
        return appointmentDao.findByDate(today()).size();
    }

    public long countPendingApprovals() {
        return appointmentDao.countByStatus(Appointment.Status.REQUESTED);
    }

    public long countTotalPatients() {
        return patientDao.countAll();
    }

    public long countTotalDoctors() {
        return doctorDao.countAll();
    }

    public BigDecimal revenueThisMonth() {
        return paymentDao.sumAmountBetween(startOfMonth(), new Date());
    }

    public BigDecimal totalRevenue() {
        return paymentDao.sumAmountAll();
    }

    public long countUnpaidInvoices() {
        return invoiceDao.countByStatus(Invoice.Status.UNPAID)
                + invoiceDao.countByStatus(Invoice.Status.PARTIALLY_PAID);
    }

    public List<ActivityLog> recentActivity(int max) {
        return activityLogDao.findRecent(max);
    }

    // ---------- Doctor dashboard ----------
    
    public List<Appointment> todaysAppointmentsForDoctor(Long doctorId) {
        return appointmentDao.findByDoctorIdAndDate(doctorId, today());
    }

    public long countUpcomingForDoctor(Long doctorId) {
        return appointmentDao.countUpcomingByDoctor(doctorId, today());
    }

    public long countCompletedConsultations(Long doctorId) {
        return consultationDao.countByDoctorId(doctorId);
    }

    // ---------- Patient dashboard ----------
    
    public long countUpcomingForPatient(Long patientId) {
        return appointmentDao.countUpcomingByPatient(patientId, today());
    }

    public long countUnpaidInvoicesForPatient(Long patientId) {
        long count = 0;
        for (Invoice inv : invoiceDao.findByPatientId(patientId)) {
            if (inv.getStatus() == Invoice.Status.UNPAID || inv.getStatus() == Invoice.Status.PARTIALLY_PAID) {
                count++;
            }
        }
        return count;
    }

    // ---------- Admin reports ----------
    
    /**
     * Generates a monthly revenue report.
     * 
     * The report contains the revenue collected each month, starting from the oldest month 
     * and ending with the current month.
     * 
     * June 2026  -> 5000
     * July 2026  -> 7500
     * August 2026 -> 6200
     *
     * Months with no revenue are still included with a value of zero.
     * 
     * @param monthsBack
     * @return 
     */
    public LinkedHashMap<String, BigDecimal> monthlyRevenue(int monthsBack) {
        Date start = monthsAgo(monthsBack);
        SimpleDateFormat labelFmt = new SimpleDateFormat("MMM yyyy");

        LinkedHashMap<String, BigDecimal> result = new LinkedHashMap<>();
        Calendar iter = Calendar.getInstance();
        iter.setTime(start);
        for (int i = 0; i < monthsBack; i++) {
            result.put(labelFmt.format(iter.getTime()), BigDecimal.ZERO);
            iter.add(Calendar.MONTH, 1);
        }

        for (Payment p : paymentDao.findSince(start)) {
            Calendar pc = Calendar.getInstance();
            pc.setTime(p.getPaymentDate());
            pc.set(Calendar.DAY_OF_MONTH, 1);
            String label = labelFmt.format(pc.getTime());
            BigDecimal current = result.get(label);
            if (current != null) {
                result.put(label, current.add(p.getAmount()));
            }
        }
        return result;
    }

    /**
     * Generates a report showing the number of new patient
     * registrations for each month.
     *
     * The months are ordered from oldest to newest.
     *
     * Months with no registrations are included with a count of zero.
     * 
     * @param monthsBack
     * @return 
     */
    public LinkedHashMap<String, Long> monthlyNewPatients(int monthsBack) {
        Date start = monthsAgo(monthsBack);
        SimpleDateFormat labelFmt = new SimpleDateFormat("MMM yyyy");

        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        Calendar iter = Calendar.getInstance();
        iter.setTime(start);
        for (int i = 0; i < monthsBack; i++) {
            result.put(labelFmt.format(iter.getTime()), 0L);
            iter.add(Calendar.MONTH, 1);
        }

        for (Date regDate : patientDao.findRegistrationDatesSince(start)) {
            Calendar rc = Calendar.getInstance();
            rc.setTime(regDate);
            rc.set(Calendar.DAY_OF_MONTH, 1);
            String label = labelFmt.format(rc.getTime());
            Long current = result.get(label);
            if (current != null) {
                result.put(label, current + 1);
            }
        }
        return result;
    }

    /**
     * Generates a patient count grouped by gender.
     *
     * The DAO returns each result as an Object array:
     *
     * row[0] = gender
     * row[1] = number of patients
     * 
     * @return 
     */
    public LinkedHashMap<String, Long> patientGenderBreakdown() {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : patientDao.countGroupedByGender()) {
            Patient.Gender gender = (Patient.Gender) row[0];
            Long count = (Long) row[1];
            result.put(gender != null ? gender.toString() : "UNSPECIFIED", count);
        }
        return result;
    }

    /**
     * Generates a performance report for every doctor.
     *
     * Each row contains:
     * - Doctor name
     * - Number of completed appointments
     * - Revenue generated
     */
    public List<DoctorPerformance> doctorPerformanceReport() {
        Map<Long, DoctorPerformance> byId = new LinkedHashMap<>();

        for (Doctor d : doctorDao.findAll()) {
            DoctorPerformance dp = new DoctorPerformance();
            dp.setDoctorName(d.getName());
            byId.put(d.getId(), dp);
        }

        for (Object[] row : appointmentDao.countCompletedGroupedByDoctor()) {
            Doctor doctor = (Doctor) row[0];
            Long count = (Long) row[1];
            DoctorPerformance dp = byId.get(doctor.getId());
            if (dp != null) {
                dp.setCompletedAppointments(count);
            }
        }

        for (Object[] row : invoiceDao.sumRevenueGroupedByDoctor()) {
            Doctor doctor = (Doctor) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            DoctorPerformance dp = byId.get(doctor.getId());
            if (dp != null) {
                dp.setRevenue(sum != null ? sum : BigDecimal.ZERO);
            }
        }

        return new ArrayList<>(byId.values());
    }

    /**
     * Simple read-only data object used to represent
     * one doctor's performance information.
     *
     * It contains:
     * - Doctor name
     * - Number of completed appointments
     * - Revenue generated
     */
    public static class DoctorPerformance implements Serializable {

        private static final long serialVersionUID = 1L;

        private String doctorName;
        private long completedAppointments = 0;
        private BigDecimal revenue = BigDecimal.ZERO;

        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public long getCompletedAppointments() {
            return completedAppointments;
        }

        public void setCompletedAppointments(long completedAppointments) {
            this.completedAppointments = completedAppointments;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }
}
