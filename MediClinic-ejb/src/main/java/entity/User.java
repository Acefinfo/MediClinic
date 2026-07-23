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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public enum UserStatus {
        PENDING,
        ACTIVE,
        DEACTIVATED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "email", nullable=false, unique = true, length = 150)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.PENDING;
    
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();
    
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password; 
    }

    public Role getRole() { 
        return role; 
    }
    public void setRole(Role role) { 
        this.role = role; 
    }

    public UserStatus getStatus() { 
        return status; 
    }
    public void setStatus(UserStatus status) { 
        this.status = status; 
    }

    public boolean isEmailVerified() { 
        return emailVerified; 
    }
    public void setEmailVerified(boolean emailVerified) { 
        this.emailVerified = emailVerified; 
    }

    public Date getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Date createdAt) { 
        this.createdAt = createdAt; 
    }
    
}