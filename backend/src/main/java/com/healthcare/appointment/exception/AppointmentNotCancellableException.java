package com.healthcare.appointment.exception;

public class AppointmentNotCancellableException extends RuntimeException {
    public AppointmentNotCancellableException(String message) {
        super(message);
    }
}
