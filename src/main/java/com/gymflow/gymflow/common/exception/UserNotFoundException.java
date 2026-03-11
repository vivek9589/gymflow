package com.gymflow.gymflow.common.exception;

/**
 * Thrown when a requested user account is not found.
 */
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String email) {
        super("User account not found with email: " + email);
    }
}