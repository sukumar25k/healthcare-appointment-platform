package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.response.SlotResponse;
import com.healthcare.appointment.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Fetch available appointment slots")
@SecurityRequirement(name = "bearerAuth")
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/available")
    @Operation(summary = "Fetch all currently available appointment slots")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots() {
        return ResponseEntity.ok(slotService.getAvailableSlots());
    }
}
