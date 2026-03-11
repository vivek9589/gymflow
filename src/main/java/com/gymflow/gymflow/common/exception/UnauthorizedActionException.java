package com.gymflow.gymflow.common.exception;

/**
 * Thrown when a user tries to perform an action they are not authorized for.
 */
public class UnauthorizedActionException extends BusinessException {
    public UnauthorizedActionException(String action) {
        super("You are not authorized to perform this action: " + action);
    }
}