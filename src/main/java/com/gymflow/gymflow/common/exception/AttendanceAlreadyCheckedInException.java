package com.gymflow.gymflow.common.exception;

public class AttendanceAlreadyCheckedInException extends RuntimeException {
    public AttendanceAlreadyCheckedInException(String message) {
        super(message);
    }
}
