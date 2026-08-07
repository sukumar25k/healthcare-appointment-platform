package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Slot;
import com.healthcare.appointment.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByStatusOrderBySlotDateAscStartTimeAsc(SlotStatus status);

    /**
     * Locks the slot row for the duration of the booking transaction so that
     * two concurrent requests for the same slot cannot both see it as
     * AVAILABLE. The second request blocks until the first transaction
     * commits/rolls back, then re-reads the (now updated) row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}
