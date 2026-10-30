package com.civicfix.platform.maintenance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job architecture for recurring preventive maintenance.
 * Periodically scans for due maintenance schedules and generates
 * corresponding work orders and reminders automatically.
 */
@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private final PreventiveMaintenanceService maintenanceService;

    public MaintenanceScheduler(PreventiveMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /**
     * Runs daily at midnight to process recurring preventive maintenance schedules.
     */
    @Scheduled(cron = "${app.maintenance.cron:0 0 0 * * ?}")
    public void processDueMaintenanceSchedules() {
        log.info("Starting scheduled preventive maintenance job...");
        try {
            int generated = maintenanceService.generateDueMaintenanceWorkOrders();
            log.info("Preventive maintenance job completed. Generated {} work orders.", generated);
        } catch (Exception e) {
            log.error("Error executing preventive maintenance scheduled job: {}", e.getMessage(), e);
        }
    }
}
