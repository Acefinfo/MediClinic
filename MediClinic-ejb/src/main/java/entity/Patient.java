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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author acefonfo
 */
@Entity
@Table(name = "patients")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Gender { MALE, FEMALE, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "chronic_conditions", length = 500)
    private String chronicConditions;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public User getUser() { 
        return user; 
    }
    public void setUser(User user) { 
        this.user = user; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getPhone() { 
        return phone; 
    }
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    public Date getDateOfBirth() { 
        return dateOfBirth; 
    }
    public void setDateOfBirth(Date dateOfBirth) { 
        this.dateOfBirth = dateOfBirth; 
    }

    public Gender getGender() { 
        return gender; 
    }
    public void setGender(Gender gender) { 
        this.gender = gender; 
    }

    public String getAddress() { 
        return address; 
    }
    public void setAddress(String address) { 
        this.address = address; 
    }

    public String getAllergies() { 
        return allergies; 
    }
    public void setAllergies(String allergies) { 
        this.allergies = allergies; 
    }

    public String getChronicConditions() { 
        return chronicConditions; 
    }
    public void setChronicConditions(String chronicConditions) { 
        this.chronicConditions = chronicConditions; 
    }

    public String getEmergencyContactName() { 
        return emergencyContactName; 
    }
    public void setEmergencyContactName(String emergencyContactName) { 
        this.emergencyContactName = emergencyContactName; 
    }

    public String getEmergencyContactPhone() { 
        return emergencyContactPhone; 
    }
    public void setEmergencyContactPhone(String emergencyContactPhone) { 
        this.emergencyContactPhone = emergencyContactPhone; 
    }
}