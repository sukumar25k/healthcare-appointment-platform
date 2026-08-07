package com.healthcare.appointment.config;

import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import com.healthcare.appointment.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Seeds a handful of demo appointment slots on startup so the
 * "Fetch Available Slots" API has data to return out of the box.
 * This does not add any new feature/API beyond what is specified;
 * it only pre-populates the schema the assignment already requires.
 * Disabled under the "test" profile so automated tests control their own data.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SlotRepository slotRepository;

    @Override
    public void run(String... args) {
        if (slotRepository.count() > 0) {
            return;
        }

        String[][] providers = {
                {"Dr.  SUSHANTH", "Neurologist"},
                {"Dr.  P.VALLY", "Cardiology"},
                {"Dr.  K.SUKUMAR", "Dermatology"},
                {"Dr.  P.SHASHRA", "Orthopedics"}
        };

        LocalTime[] times = {
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalTime.of(14, 0), LocalTime.of(15, 0)
        };

        for (int day = 0; day < 3; day++) {
            LocalDate date = LocalDate.now().plusDays(day + 1);
            for (String[] provider : providers) {
                for (LocalTime start : times) {
                    slotRepository.save(Slot.builder()
                            .doctorName(provider[0])
                            .department(provider[1])
                            .slotDate(date)
                            .startTime(start)
                            .endTime(start.plusMinutes(30))
                            .status(SlotStatus.AVAILABLE)
                            .build());
                }
            }
        }
    }
}
