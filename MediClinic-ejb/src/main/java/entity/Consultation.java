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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author acefonfo
 */
@Entity
public class Consultation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "symptoms", length = 1000)
    private String symptoms;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "consultation_date", nullable = false)
    private Date consultationDate = new Date();

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Appointment getAppointment() { 
        return appointment; 
    }
    public void setAppointment(Appointment appointment) { 
        this.appointment = appointment; 
    }

    public String getSymptoms() { 
        return symptoms; 
    }
    public void setSymptoms(String symptoms) { 
        this.symptoms = symptoms; 
    }

    public String getDiagnosis() { 
        return diagnosis; 
    }
    public void setDiagnosis(String diagnosis) { 
        this.diagnosis = diagnosis; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    public Date getConsultationDate() { 
        return consultationDate; 
    }
    public void setConsultationDate(Date consultationDate) { 
        this.consultationDate = consultationDate; 
    }
}
