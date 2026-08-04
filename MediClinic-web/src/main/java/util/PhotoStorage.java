/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 * @author acefonfo
 */
public class PhotoStorage {
    
    /**
     * Directory where uploaded images will be stored.
     * 
     * First check if directory exist, if directory exists it will be used.
     * 
     * Otherwise, the temporary directory tempdir\mediclinic-uploads be used 
     */
    private static final String UPLOAD_DIR = System.getProperty("mediclinic.upload.dir",  System.getProperty("java.io.tmpdir") + File.separator + "mediclinic-uploads");
    
    
    /**
     * Saves an uploaded photo 
     * 
     * @param userId
     * @param originalFileName
     * @param data
     * @return
     * @throws IOException 
     */
    public static String save(Long userId, String originalFileName, InputStream data) throws IOException{
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()){
            dir.mkdir();
        }
        
        String ext = extensionOf(originalFileName);
        String fileName = "user_"+userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        File target = new File(dir, fileName);
        
        try(OutputStream out = new FileOutputStream(target)){
            byte[] buffer = new byte[8192];
            int read;
            while ((read = data.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        }
        return fileName;
    }
    
    /**
     * Returns the file object for a stored image.
     * @param fileName
     * @return 
     */
    public static File resolve(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        return new File(UPLOAD_DIR, fileName);
    }
    
    /**
     * Extracts extension of a file name.
     * @param fileName
     * @return 
     */
    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return (dot >= 0) ? fileName.substring(dot).toLowerCase() : "";
    }

}
