package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.response.AppointmentResponse;
import com.healthcare.appointment.entity.*;
import com.healthcare.appointment.exception.AppointmentNotCancellableException;
import com.healthcare.appointment.exception.ResourceNotFoundException;
import com.healthcare.appointment.exception.SlotAlreadyBookedException;
import com.healthcare.appointment.exception.UnauthorizedAccessException;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.SlotRepository;
import com.healthcare.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SlotService slotService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User user;
    private Slot availableSlot;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Jane Doe").email("jane@example.com").password("hash").build();
        availableSlot = Slot.builder()
                .id(10L)
                .doctorName("Dr. Asha Rao")
                .department("General Medicine")
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        lenient().when(slotService.toResponse(any(Slot.class))).thenAnswer(inv -> {
            Slot s = inv.getArgument(0);
            return com.healthcare.appointment.dto.response.SlotResponse.builder()
                    .id(s.getId()).doctorName(s.getDoctorName()).department(s.getDepartment())
                    .slotDate(s.getSlotDate()).startTime(s.getStartTime()).endTime(s.getEndTime())
                    .status(s.getStatus()).build();
        });
    }

    @Test
    void createAppointment_withAvailableSlot_booksSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(slotRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(availableSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        AppointmentResponse response = appointmentService.createAppointment(1L, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
        assertThat(availableSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(slotRepository).save(availableSlot);
    }

    @Test
    void createAppointment_withAlreadyBookedSlot_throwsSlotAlreadyBookedException() {
        availableSlot.setStatus(SlotStatus.BOOKED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(slotRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(availableSlot));

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 10L))
                .isInstanceOf(SlotAlreadyBookedException.class)
                .hasMessageContaining("already booked");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_withUnknownSlot_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(slotRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelAppointment_ownedAndBooked_cancelsAndFreesSlot() {
        Slot bookedSlot = Slot.builder().id(10L).status(SlotStatus.BOOKED)
                .doctorName("Dr. Asha Rao").department("General Medicine")
                .slotDate(LocalDate.now().plusDays(1)).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30))
                .build();
        Appointment appointment = Appointment.builder()
                .id(100L).user(user).slot(bookedSlot).status(AppointmentStatus.BOOKED).build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(slotRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(bookedSlot));

        AppointmentResponse response = appointmentService.cancelAppointment(1L, 100L);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(bookedSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void cancelAppointment_notOwnedByUser_throwsUnauthorizedAccessException() {
        User otherUser = User.builder().id(2L).build();
        Appointment appointment = Appointment.builder()
                .id(100L).user(otherUser).slot(availableSlot).status(AppointmentStatus.BOOKED).build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 100L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void cancelAppointment_alreadyCancelled_throwsAppointmentNotCancellableException() {
        Appointment appointment = Appointment.builder()
                .id(100L).user(user).slot(availableSlot).status(AppointmentStatus.CANCELLED).build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 100L))
                .isInstanceOf(AppointmentNotCancellableException.class);
    }
}
