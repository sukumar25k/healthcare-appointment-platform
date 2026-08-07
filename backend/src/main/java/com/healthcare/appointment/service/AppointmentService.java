package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.response.AppointmentResponse;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.exception.AppointmentNotCancellableException;
import com.healthcare.appointment.exception.ResourceNotFoundException;
import com.healthcare.appointment.exception.SlotAlreadyBookedException;
import com.healthcare.appointment.exception.UnauthorizedAccessException;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.SlotRepository;
import com.healthcare.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SlotService slotService;

    /**
     * Books a slot for a user.
     *
     * Duplicate-booking / concurrency safety: the slot row is fetched with a
     * PESSIMISTIC_WRITE lock, so if two requests race for the same slot the
     * second one blocks until the first transaction commits, then re-reads
     * the row and correctly sees it as BOOKED - it fails with
     * SlotAlreadyBookedException instead of double-booking the slot.
     */
    @Transactional
    public AppointmentResponse createAppointment(Long userId, Long slotId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotAlreadyBookedException("This slot is already booked");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .user(user)
                .slot(slot)
                .status(AppointmentStatus.BOOKED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not allowed to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentNotCancellableException("Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        // Free the slot back up, locking it to stay consistent with the booking path.
        Slot slot = slotRepository.findByIdForUpdate(appointment.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserIdOrderByBookedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .slot(slotService.toResponse(appointment.getSlot()))
                .status(appointment.getStatus())
                .bookedAt(appointment.getBookedAt())
                .cancelledAt(appointment.getCancelledAt())
                .build();
    }
}
