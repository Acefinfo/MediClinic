/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author acefonfo
 */
public class TokenUtil {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate a secure random token
     * @return  a URL safe random string.
     */
    public static String generateToken(){
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return DatatypeConverter.printBase64Binary(bytes)
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "");
    }
    
}
