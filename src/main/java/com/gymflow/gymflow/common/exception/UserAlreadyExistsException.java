package com.gymflow.gymflow.common.exception;

/**
 * Thrown when a user tries to register with an already existing email.
 */
public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("Email is already registered: " + email);
    }
}