package com.civicfix.platform.sla.service;

import com.civicfix.platform.sla.entity.BusinessCalendar;
import com.civicfix.platform.sla.entity.BusinessHoliday;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class BusinessHoursCalculator {

    /**
     * Calculates the target deadline by adding the given duration in business minutes,
     * taking into account working days, working hours, and official municipal holidays.
     */
    public Instant calculateDeadline(Instant startInstant, long durationMinutes,
                                    BusinessCalendar calendar, List<BusinessHoliday> holidays) {
        if (durationMinutes <= 0) {
            return startInstant;
        }

        if (calendar == null || calendar.isIs24x7()) {
            return startInstant.plus(Duration.ofMinutes(durationMinutes));
        }

        ZoneId zone = calendar.getZoneId();
        ZonedDateTime current = startInstant.atZone(zone);
        long remainingMinutes = durationMinutes;

        while (remainingMinutes > 0) {
            LocalDate currentDate = current.toLocalDate();
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Check if today is a working day and not a holiday
            if (!calendar.isWorkingDay(dayOfWeek) || isHoliday(currentDate, holidays)) {
                // Advance to start of next day
                current = current.plusDays(1).with(calendar.getStartTime());
                continue;
            }

            LocalTime currentTime = current.toLocalTime();
            LocalTime workStart = calendar.getStartTime();
            LocalTime workEnd = calendar.getEndTime();

            // If before work hours, fast-forward to start of work
            if (currentTime.isBefore(workStart)) {
                current = current.with(workStart);
                currentTime = workStart;
            }

            // If at or after work hours, advance to next day start
            if (!currentTime.isBefore(workEnd)) {
                current = current.plusDays(1).with(workStart);
                continue;
            }

            // Inside working window for today
            long minutesLeftToday = Duration.between(currentTime, workEnd).toMinutes();

            if (remainingMinutes <= minutesLeftToday) {
                // Deadline falls within today's working window
                current = current.plusMinutes(remainingMinutes);
                remainingMinutes = 0;
            } else {
                // Consume the rest of today and move to start of next working day
                remainingMinutes -= minutesLeftToday;
                current = current.plusDays(1).with(workStart);
            }
        }

        return current.toInstant();
    }

    public boolean isWorkingTime(Instant instant, BusinessCalendar calendar, List<BusinessHoliday> holidays) {
        if (calendar == null || calendar.isIs24x7()) return true;
        ZoneId zone = calendar.getZoneId();
        ZonedDateTime zdt = instant.atZone(zone);
        LocalDate date = zdt.toLocalDate();

        if (!calendar.isWorkingDay(date.getDayOfWeek())) return false;
        if (isHoliday(date, holidays)) return false;

        return calendar.isWithinWorkingHours(zdt.toLocalTime());
    }

    public boolean isHoliday(LocalDate date, List<BusinessHoliday> holidays) {
        if (holidays == null || holidays.isEmpty() || date == null) return false;
        return holidays.stream().anyMatch(h -> h.matches(date));
    }
}
