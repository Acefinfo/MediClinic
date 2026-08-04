/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import dao.UserDao;
import entity.User;
import java.io.IOException;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import org.primefaces.model.file.UploadedFile;
import util.PhotoStorage;

/**
 *
 * @author acefonfo
 */
@ManagedBean(name = "profilePhotoBean")
public class ProfilePhotoBean {

    @EJB
    private UserDao userDao;

    @ManagedProperty(value = "#{loggedInUser}")
    private LoggedInUser loggedInUser;

    private UploadedFile file;

    /**
     * Handles the profile photo upload process.
     * 
     * Steps:
     * 1. Checks if a file was selected.
     * 2. Validates that the file is an image.
     * 3. Finds the logged-in user from the database.
     * 4. Saves the image file.
     * 5. Updates the user's photo path.
     * 6. Refreshes the logged-in user object.
     * 7. Displays a success message.
     */
    public void upload() {
        if (file == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Please choose an image first.");
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Only image files are allowed.");
            return;
        }
        try {
            User user = userDao.findById(loggedInUser.getUser().getId());
            String savedFileName = PhotoStorage.save(user.getId(), file.getFileName(), file.getInputStream());
            user.setPhotoPath(savedFileName);
            userDao.update(user);
            loggedInUser.setUser(user);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile photo updated.");
        } catch (IOException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Could not save the photo: " + e.getMessage());
        }
    }

    /**
     * Adds a JSF message that will be displayed on the webPage.
     * 
     * @param severity
     * @param summary
     * @param detail 
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public UploadedFile getFile() {
        return file;
    }
    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /**
     * Injects the logged in user object. 
     * 
     * @param loggedInUser 
     */
    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}