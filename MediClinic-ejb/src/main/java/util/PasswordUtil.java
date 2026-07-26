/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author acefonfo
 */
public class PasswordUtil {
    
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Hashes a plain text password.
     * @param plainPassword
     * @return A String in the format::  iterations:salt:hash
     */
    public static String hash(String plainPassword){
        try{
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return ITERATIONS + ":" + DatatypeConverter.printBase64Binary(salt) + ":" + DatatypeConverter.printBase64Binary(hash);
            
        }catch(Exception e){
            throw new RuntimeException("Could not hash password", e);
        }
        
    }
    
    /**
     * Verifies whether the entered password matches the stored hash.
     * 
     * @param plainPassword Password stored during login
     * @param storedHash Password hash retrieved from the database
     * @return true if password matches else false
     */
    public static boolean matches(String plainPassword, String storedHash){
        try{
            String[] parts = storedHash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = DatatypeConverter.parseBase64Binary(parts[1]);
            byte[] hash = DatatypeConverter.parseBase64Binary(parts[2]);
            byte[] testHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, hash.length * 8);
            return slowEquals(hash, testHash);
        } catch (Exception e){
            return false;
        }
    }
    
    /**
     * Generates a PBKDF2 hash.
     * 
     * @param password User password as characters
     * @param salt Random salt
     * @param iterations Number of hash iterations.
     * @param keyLength Desired hash length in bits
     * @return Generated hash bytes.
     * @throws Exception 
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws Exception{
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
    }
    
    /**
     * Computes two bytes arrays in constant time
     * @param a First byte array
     * @param b Second byte array
     * @return  true if both array are identical. 
     */
    private static boolean slowEquals(byte[] a, byte[] b){
        int diff = a.length^b.length;
        for(int i = 0; i < a.length && i < b.length; i++){
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
