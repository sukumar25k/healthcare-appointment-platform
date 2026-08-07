package com.healthcare.appointment.dto.response;

import com.healthcare.appointment.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private SlotResponse slot;
    private AppointmentStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
}
