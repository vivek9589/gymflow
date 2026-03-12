package com.gymflow.gymflow.common.exception;

public class MembershipExpiredException extends RuntimeException {
    public MembershipExpiredException(String message) {
        super(message);
    }
}
