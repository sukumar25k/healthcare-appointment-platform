package com.healthcare.appointment.exception;

/**
 * Thrown when a slot that is being booked is no longer AVAILABLE, whether
 * because another user booked it first (duplicate booking prevention) or a
 * concurrent request won the race for the same slot.
 */
public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(String message) {
        super(message);
    }
}
