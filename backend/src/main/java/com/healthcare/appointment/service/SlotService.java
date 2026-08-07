package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.response.SlotResponse;
import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import com.healthcare.appointment.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;

    @Transactional(readOnly = true)
    public List<SlotResponse> getAvailableSlots() {
        return slotRepository.findByStatusOrderBySlotDateAscStartTimeAsc(SlotStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SlotResponse toResponse(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .doctorName(slot.getDoctorName())
                .department(slot.getDepartment())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .build();
    }
}
