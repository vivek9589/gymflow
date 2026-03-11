package com.gymflow.gymflow.common.exception;

/**
 * Thrown when login credentials are invalid.
 */
public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}