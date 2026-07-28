/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.ActivityLogDao;
import dao.DoctorDao;
import dao.RoleDao;
import dao.UserDao;
import entity.ActivityLog;
import entity.Doctor;
import entity.Role;
import entity.User;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import util.PasswordUtil;
import util.TokenUtil;

/**
 *
 * @author acefonfo
 */
@Stateless
public class AdminUserService {
    
    @EJB
    private UserDao userDao;
    
    @EJB
    private RoleDao roleDao;
    @EJB
    private DoctorDao doctorDao;
    
    @EJB
    private ActivityLogDao activityLogDao;
    
    
    public List<User> listAllStaff(){
        return userDao.findByRoleNames(Arrays.asList("DOCTOR", "RECEPTIONIST"));
    }
    
    public User createStaffUser(User actor, String email, String password, String name, String phone, String roleName) throws AuthException{
        if (!"DOCTOR".equals(roleName) && !"RECEPTIONIST".equals(roleName)) {
            throw new AuthException("Staff accounts must be DOCTOR or RECEPTIONIST.");
        }
        if (userDao.existsByEmail(email)) {
            throw new AuthException("An account with this email already exists.");
        }
        
        Role role = roleDao.findByName(roleName);
        if(role == null){
            throw new AuthException(roleName + "role is now configured");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.hash(password));
        user.setRole(role);
        user.setName(name);
        user.setPhone(phone);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userDao.create(user);
        
        if ("DOCTOR".equals(roleName)) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setName(name);
            doctor.setPhone(phone);
            doctorDao.create(doctor);
        }
        
        log(actor, "CREATE_STAFF", "User", user.getId(), "Created " + roleName + " account for " + email);
        return user;
    }
    
    public void deactivateUser(User actor, Long userId) throws AuthException{
        User target = requireUser(userId);
        target.setStatus(User.UserStatus.DEACTIVATED);
        userDao.update(target);
        log(actor, "DEACTIVATE_USER", "User", userId, "Deactivated account " + target.getEmail());
    }
    
    public void reactivateUser(User actor, Long userId) throws AuthException {
        User target = requireUser(userId);
        target.setStatus(User.UserStatus.ACTIVE);
        userDao.update(target);
        log(actor, "REACTIVATE_USER", "User", userId, "Reactivated account " + target.getEmail());
    }
    
    public String adminResetPassword(User actor, Long userId) throws AuthException {
        User target = requireUser(userId);
        String tempPassword = TokenUtil.generateToken().substring(0, 10);
        target.setPassword(PasswordUtil.hash(tempPassword));
        userDao.update(target);
        log(actor, "RESET_PASSWORD", "User", userId, "Admin reset password for " + target.getEmail());
        return tempPassword;
    }
    
    public void updateRole(User actor, Long userId, String newRoleName) throws AuthException {
        User target = requireUser(userId);
        Role newRole = roleDao.findByName(newRoleName);
        if (newRole == null) {
            throw new AuthException(newRoleName + " role is not configured.");
        }
        String oldRole = (target.getRole() != null) ? target.getRole().getName() : "NONE";
        target.setRole(newRole);
        userDao.update(target);
        log(actor, "CHANGE_ROLE", "User", userId, "Changed role of " + target.getEmail() + " from " + oldRole + " to " + newRoleName);
    }
    
     public void updateStaffProfile(User actor, Long userId, String name, String phone) throws AuthException {
        User target = requireUser(userId);
        target.setName(name);
        target.setPhone(phone);
        userDao.update(target);

        Doctor doctor = doctorDao.findByUserId(userId);
        if (doctor != null) {
            doctor.setName(name);
            doctor.setPhone(phone);
            doctorDao.update(doctor);
        }
        log(actor, "UPDATE_STAFF_PROFILE", "User", userId, "Updated profile for " + target.getEmail());
    }
    
    
    private User requireUser(Long userId) throws AuthException {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new AuthException("User not found.");
        }
        return user;
    }
    
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
