package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByBookedAtDesc(Long userId);

    Optional<Appointment> findBySlotIdAndStatus(Long slotId, AppointmentStatus status);

    boolean existsBySlotIdAndStatus(Long slotId, AppointmentStatus status);
}
