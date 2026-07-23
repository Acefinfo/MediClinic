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
public class DoctorSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum DayOfWeek { 
        SUNDAY ,
        MONDAY, 
        TUESDAY, 
        WEDNESDAY, 
        THURSDAY, 
        FRIDAY, 
        SATURDAY, 
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Temporal(TemporalType.TIME)
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @Column(name = "max_patients", nullable = false)
    private Integer maxPatients;

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Doctor getDoctor() { 
        return doctor; 
    }
    public void setDoctor(Doctor doctor) { 
        this.doctor = doctor; 
    }

    public DayOfWeek getDayOfWeek() { 
        return dayOfWeek; 
    }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { 
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
}