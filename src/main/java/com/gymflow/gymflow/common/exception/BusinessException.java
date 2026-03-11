package com.gymflow.gymflow.common.exception;

/**
 * Base class for all business logic exceptions.
 * Extend this for specific error scenarios.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}