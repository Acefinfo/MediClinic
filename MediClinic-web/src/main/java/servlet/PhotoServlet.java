/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.UserDao;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.PhotoStorage;

/**
 *
 * @author acefonfo
 */
@WebServlet("/photo/*")
public class PhotoServlet extends HttpServlet{
    
    @EJB 
    private UserDao userDao;
    
    /***
     * Handles HTTP GET request
     * 
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
       String path = req.getPathInfo();
        Long userId = null;
        if (path != null && path.length() > 1) {
            try {
                userId = Long.parseLong(path.substring(1));
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        
        User user = (userId != null) ? userDao.findById(userId) : null;
        File photo = (user != null) ? PhotoStorage.resolve(user.getPhotoPath()) : null;
        
        if (photo == null || !photo.exists()){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        resp.setContentType(guessContentType(photo.getName()));
        resp.setHeader("Cache-Control", "no-cache");
        try(OutputStream out = resp.getOutputStream()){
            Files.copy(photo.toPath(), out);
        }    
    }
    
    /**
     * Determines the MIME type
     * based on the file extension.
     * 
     * @param fileName
     * @return 
     */
    private String guessContentType(String fileName){
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")){
            return "image/png";
        }
        
        if (lower.endsWith(".gif")){
            return "image/gif";
        }
        return "image/jpeg";
    }
    
}
