package com.healthcare.appointment.integration;

import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.exception.SlotAlreadyBookedException;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.SlotRepository;
import com.healthcare.appointment.repository.UserRepository;
import com.healthcare.appointment.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fires two concurrent booking requests at the exact same slot and asserts
 * that exactly one succeeds and the other is rejected as already booked -
 * proving the pessimistic-lock strategy in AppointmentService prevents a
 * race condition from double-booking a slot.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentBookingTest {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void twoUsersBookingSameSlotSimultaneously_onlyOneSucceeds() throws Exception {
        Slot slot = slotRepository.save(Slot.builder()
                .doctorName("Dr. Concurrency Test")
                .department("General Medicine")
                .slotDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        User userA = userRepository.save(User.builder()
                .fullName("Concurrent A").email("concurrent-a@example.com")
                .password(passwordEncoder.encode("password1")).build());
        User userB = userRepository.save(User.builder()
                .fullName("Concurrent B").email("concurrent-b@example.com")
                .password(passwordEncoder.encode("password1")).build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        Callable<Void> bookingTask = (() -> {
            readyLatch.countDown();
            startLatch.await();
            return null;
        });

        List<Future<Void>> results = new CopyOnWriteArrayList<>();

        Runnable bookAsUserA = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                appointmentService.createAppointment(userA.getId(), slot.getId());
                successCount.incrementAndGet();
            } catch (SlotAlreadyBookedException e) {
                conflictCount.incrementAndGet();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable bookAsUserB = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                appointmentService.createAppointment(userB.getId(), slot.getId());
                successCount.incrementAndGet();
            } catch (SlotAlreadyBookedException e) {
                conflictCount.incrementAndGet();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        };

        executor.submit(bookAsUserA);
        executor.submit(bookAsUserB);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // release both threads at (almost) the same instant

        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);

        Slot refreshed = slotRepository.findById(slot.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(SlotStatus.BOOKED);

        long bookedAppointmentsForSlot = appointmentRepository.findAll().stream()
                .filter(a -> a.getSlot().getId().equals(slot.getId()))
                .count();
        assertThat(bookedAppointmentsForSlot).isEqualTo(1);
    }
}
