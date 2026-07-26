/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

/**
 *
 * @author acefonfo
 */
public class AuthException extends Exception {
    
    /**
     *  Create new Auth Exception with the specified message.
     * @param message 
     */
    public AuthException(String message) {
        super(message);
    }
}
