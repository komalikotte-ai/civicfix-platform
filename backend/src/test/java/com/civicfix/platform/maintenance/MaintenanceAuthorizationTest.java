package com.civicfix.platform.maintenance;

import com.civicfix.platform.maintenance.controller.MaintenanceController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaintenanceController Authorization Tests")
class MaintenanceAuthorizationTest {

    @Test
    @DisplayName("MaintenanceController class has default role guard")
    void testClassLevelPreAuthorize() {
        PreAuthorize annotation = MaintenanceController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "Class should have @PreAuthorize");
        String value = annotation.value();
        assertTrue(value.contains("ADMIN") && value.contains("SUPERVISOR"),
                "Class-level @PreAuthorize should include ADMIN and SUPERVISOR");
    }

    @Test
    @DisplayName("createInspectionPlan requires manager/admin role")
    void testCreateInspectionPlan_requiresManagerRole() {
        Method m = findMethod("createInspectionPlan");
        assertNotNull(m);
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertTrue(ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER"));
    }

    @Test
    @DisplayName("createMaintenanceSchedule requires supervisor or admin")
    void testCreateMaintenanceSchedule_requiresRole() {
        Method m = findMethod("createMaintenanceSchedule");
        assertNotNull(m);
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertTrue(ann.value().contains("SUPERVISOR") || ann.value().contains("ADMIN"));
    }

    @Test
    @DisplayName("triggerDueMaintenanceJobs requires manager/admin")
    void testTriggerDueMaintenanceJobs_requiresAdminOrManager() {
        Method m = findMethod("triggerDueMaintenanceJobs");
        assertNotNull(m);
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertTrue(ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER"));
    }

    @Test
    @DisplayName("recordConditionAssessment is accessible to field staff")
    void testRecordConditionAssessment_allowsFieldStaff() {
        Method m = findMethod("recordConditionAssessment");
        assertNotNull(m);
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertTrue(ann.value().contains("FIELD_WORKER") || ann.value().contains("SUPERVISOR"));
    }

    @Test
    @DisplayName("convertRecommendationToWorkOrder requires supervisor/manager/admin")
    void testConvertRecommendationToWorkOrder_requiresRole() {
        Method m = findMethod("convertRecommendationToWorkOrder");
        assertNotNull(m);
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertTrue(ann.value().contains("SUPERVISOR") || ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER"));
    }

    @Test
    @DisplayName("All expected maintenance endpoints exist on controller")
    void testAllExpectedEndpointsExist() {
        String[] expected = {
                "createInspectionPlan", "getInspectionPlanById", "searchInspectionPlans",
                "createMaintenanceSchedule", "getMaintenanceScheduleById", "searchMaintenanceSchedules",
                "pauseMaintenanceSchedule", "resumeMaintenanceSchedule", "triggerDueMaintenanceJobs",
                "recordConditionAssessment", "getConditionAssessmentById", "getConditionAssessmentsForAsset",
                "createRepairRecommendation", "getRepairRecommendationById", "getRecommendationsForAsset",
                "approveRepairRecommendation", "convertRecommendationToWorkOrder",
                "logAssetFailure", "getAssetFailureLogsForAsset",
                "getMyReminders", "acknowledgeReminder"
        };

        for (String name : expected) {
            Method m = findMethod(name);
            assertNotNull(m, "MaintenanceController must have method: " + name);
        }
    }

    private Method findMethod(String name) {
        return Arrays.stream(MaintenanceController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
