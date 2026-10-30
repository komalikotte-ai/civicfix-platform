package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.maintenance.entity.MaintenanceReminder;
import com.civicfix.platform.maintenance.enums.ReminderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MaintenanceReminderRepository extends JpaRepository<MaintenanceReminder, Long> {

    List<MaintenanceReminder> findByRecipientIdAndStatus(Long recipientId, ReminderStatus status);

    List<MaintenanceReminder> findByStatusAndDueDateLessThanEqual(ReminderStatus status, Instant dueThreshold);

    Page<MaintenanceReminder> findByRecipientIdOrderByDueDateAsc(Long recipientId, Pageable pageable);

    List<MaintenanceReminder> findByScheduleId(Long scheduleId);
}
