/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author acefonfo
 */
@Entity
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { REQUESTED, APPROVED, CANCELLED, RESCHEDULED, COMPLETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private DoctorSchedule schedule;

    @Temporal(TemporalType.DATE)
    @Column(name = "appointment_date", nullable = false)
    private Date appointmentDate;

    @Temporal(TemporalType.TIME)
    @Column(name = "appointment_time", nullable = false)
    private Date appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.REQUESTED;

    @Column(name = "reason", length = 500)
    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Patient getPatient() { 
        return patient; 
    }
    public void setPatient(Patient patient) { 
        this.patient = patient; 
    }

    public Doctor getDoctor() { 
        return doctor; 
    }
    public void setDoctor(Doctor doctor) { 
        this.doctor = doctor; 
    }

    public DoctorSchedule getSchedule() { 
        return schedule; 
    }
    public void setSchedule(DoctorSchedule schedule) { 
        this.schedule = schedule; 
    }

    public Date getAppointmentDate() {
        return appointmentDate; 
    }
    public void setAppointmentDate(Date appointmentDate) { 
        this.appointmentDate = appointmentDate; 
    }

    public Date getAppointmentTime() { 
        return appointmentTime; 
    }
    public void setAppointmentTime(Date appointmentTime) { 
        this.appointmentTime = appointmentTime; 
    }

    public Status getStatus() { 
        return status; 
    }
    public void setStatus(Status status) { 
        this.status = status; 
    }

    public String getReason() { 
        return reason; }
    public void setReason(String reason) { 
        this.reason = reason; 
    }

    public Date getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Date createdAt) { 
        this.createdAt = createdAt; 
    }
}