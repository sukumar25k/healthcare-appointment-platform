package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.request.CreateAppointmentRequest;
import com.healthcare.appointment.dto.response.AppointmentResponse;
import com.healthcare.appointment.security.CurrentUserProvider;
import com.healthcare.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Create, cancel, and fetch appointments")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @Operation(summary = "Book an appointment for an available slot")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request,
                                                                   Authentication authentication) {
        Long userId = currentUserProvider.getCurrentUserId(authentication);
        AppointmentResponse response = appointmentService.createAppointment(userId, request.getSlotId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an existing appointment")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id,
                                                                   Authentication authentication) {
        Long userId = currentUserProvider.getCurrentUserId(authentication);
        AppointmentResponse response = appointmentService.cancelAppointment(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Fetch the authenticated user's appointment history")
    public ResponseEntity<List<AppointmentResponse>> getUserAppointments(Authentication authentication) {
        Long userId = currentUserProvider.getCurrentUserId(authentication);
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }
}
