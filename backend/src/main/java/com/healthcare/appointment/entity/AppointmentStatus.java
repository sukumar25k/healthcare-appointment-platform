package com.healthcare.appointment.entity;

/**
 * Lifecycle status of an appointment. Rows are never deleted so that
 * appointment history is preserved.
 */
public enum AppointmentStatus {
    BOOKED,
    CANCELLED
}
