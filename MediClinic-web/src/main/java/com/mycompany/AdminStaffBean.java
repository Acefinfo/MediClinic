/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import entity.User;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import service.AdminUserService;
import service.AuthException;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "adminStaffBean")
@ViewScoped
public class AdminStaffBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Stores all staff accounts displayed on the page
    private List<User> staffList;

    // Fields used when creating a new staff account
    private String name;
    private String phone;
    private String email;
    private String password;
    private String roleName = "RECEPTIONIST";

    // Feilds used when editing an existing staff account
    private Long editUserId;
    private String editName;
    private String editPhone;
    private String editRoleName;

    @EJB
    private AdminUserService adminUserService;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    /**
     * Initializes the managed bean after construction. Loads all staff members
     * into the table.
     */
    @PostConstruct
    public void init() {
        loadStaff();
    }

    /**
     * Retrieves all staff account from the database
     */
    public void loadStaff() {
        staffList = adminUserService.listAllStaff();
    }

    /**
     * Creates new staff account using the entered from data. Clears the form
     * and refreshes the staff list if successful
     */
    public void createStaff() {
        try {
            adminUserService.createStaffUser(loggedInUser.getUser(), email, password, name, phone, roleName);
            name = null;
            phone = null;
            email = null;
            password = null;
            roleName = "RECEPTIONIST";
            loadStaff();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff account created.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Loads the selected staff members details into edit form.
     *
     * @param user
     */
    public void startEdit(User user) {
        editUserId = user.getId();
        editName = user.getName();
        editPhone = user.getPhone();
        editRoleName = user.getRole().getName();
    }

    /**
     * Saves changes made to a staff member's profile and role.
     */
    public void saveEdit() {
        try {
            adminUserService.updateStaffProfile(loggedInUser.getUser(), editUserId, editName, editPhone);
            adminUserService.updateRole(loggedInUser.getUser(), editUserId, editRoleName);
            loadStaff();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff account updated.");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Activates or deactivates a staff account depending on its current status.
     *
     * @param user
     */
    public void toggleStatus(User user) {
        try {
            if (user.getStatus() == User.UserStatus.DEACTIVATED) {
                adminUserService.reactivateUser(loggedInUser.getUser(), user.getId());
            } else {
                adminUserService.deactivateUser(loggedInUser.getUser(), user.getId());
            }
            loadStaff();
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Resets the selected staff member's password. The generated password is
     * displayed once to the administrator.
     *
     * @param user
     */
    public void resetPassword(User user) {
        try {
            String newPassword = adminUserService.adminResetPassword(loggedInUser.getUser(), user.getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Password Reset",
                    "New password for " + user.getEmail() + ": " + newPassword
                    + " (share this with them securely -- it will not be shown again)");
        } catch (AuthException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
        }
    }

    /**
     * Display a JSF FacesMessage on the page
     *
     * @param severity
     * @param summary
     * @param detail
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getter methods
    public List<User> getStaffList() {
        return staffList;
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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEditName() {
        return editName;
    }

    public void setEditName(String editName) {
        this.editName = editName;
    }

    public String getEditPhone() {
        return editPhone;
    }

    public void setEditPhone(String editPhone) {
        this.editPhone = editPhone;
    }

    public String getEditRoleName() {
        return editRoleName;
    }

    public void setEditRoleName(String editRoleName) {
        this.editRoleName = editRoleName;
    }

    /**
     * Injects the currently logged-in user
     *
     * @param loggedInUser
     */
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
