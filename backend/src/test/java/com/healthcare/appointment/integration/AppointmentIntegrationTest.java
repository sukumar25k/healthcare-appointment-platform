package com.healthcare.appointment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment.dto.request.CreateAppointmentRequest;
import com.healthcare.appointment.dto.request.RegisterRequest;
import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import com.healthcare.appointment.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SlotRepository slotRepository;

    private Slot slot;

    @BeforeEach
    void setUp() {
        slot = slotRepository.save(Slot.builder()
                .doctorName("Dr. Test Physician")
                .department("General Medicine")
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(SlotStatus.AVAILABLE)
                .build());
    }

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", email, "securePass1");
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void fetchAvailableSlots_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/slots/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fetchAvailableSlots_withToken_returnsSlots() throws Exception {
        String token = registerAndGetToken("slots-user@example.com");

        mockMvc.perform(get("/api/slots/available")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + slot.getId() + ")]").exists());
    }

    @Test
    void bookAppointment_thenFetchHistory_thenCancel_fullWorkflow() throws Exception {
        String token = registerAndGetToken("workflow-user@example.com");

        CreateAppointmentRequest bookRequest = new CreateAppointmentRequest(slot.getId());

        String bookResponseBody = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andReturn().getResponse().getContentAsString();

        Long appointmentId = objectMapper.readTree(bookResponseBody).get("id").asLong();

        // Appointment now shows up in history
        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(appointmentId))
                .andExpect(jsonPath("$[0].status").value("BOOKED"));

        // Slot no longer appears in available slots
        mockMvc.perform(get("/api/slots/available")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + slot.getId() + ")]").doesNotExist());

        // Cancel it
        mockMvc.perform(patch("/api/appointments/{id}/cancel", appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // History still contains the (now cancelled) appointment - preserved for history
        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));

        // Slot is available again
        mockMvc.perform(get("/api/slots/available")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + slot.getId() + ")]").exists());
    }

    @Test
    void bookAppointment_onAlreadyBookedSlot_returns409_duplicateBookingPrevention() throws Exception {
        String tokenA = registerAndGetToken("user-a@example.com");
        String tokenB = registerAndGetToken("user-b@example.com");

        CreateAppointmentRequest bookRequest = new CreateAppointmentRequest(slot.getId());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This slot is already booked"));
    }

    @Test
    void cancelAppointment_belongingToAnotherUser_returns403() throws Exception {
        String tokenA = registerAndGetToken("owner@example.com");
        String tokenB = registerAndGetToken("intruder@example.com");

        CreateAppointmentRequest bookRequest = new CreateAppointmentRequest(slot.getId());
        String bookResponseBody = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long appointmentId = objectMapper.readTree(bookResponseBody).get("id").asLong();

        mockMvc.perform(patch("/api/appointments/{id}/cancel", appointmentId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookAppointment_withInvalidToken_returns401() throws Exception {
        CreateAppointmentRequest bookRequest = new CreateAppointmentRequest(slot.getId());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bookAppointment_withMissingSlotId_returns400() throws Exception {
        String token = registerAndGetToken("validation-user@example.com");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.slotId").exists());
    }

    @Test
    void bookAppointment_withNonExistentSlot_returns404() throws Exception {
        String token = registerAndGetToken("notfound-user@example.com");
        CreateAppointmentRequest bookRequest = new CreateAppointmentRequest(999999L);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isNotFound());
    }
}
