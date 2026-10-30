package com.civicfix.platform.procurement;

import com.civicfix.platform.procurement.controller.ProcurementController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Authorization annotation tests for ProcurementController.
 * Verifies that RBAC @PreAuthorize annotations are properly applied
 * without needing a Spring context.
 */
@DisplayName("ProcurementController Authorization Tests")
class ProcurementAuthorizationTest {

    @Test
    @DisplayName("ProcurementController class has default role guard")
    void testClassLevelPreAuthorize() {
        PreAuthorize annotation = ProcurementController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "Class should have @PreAuthorize");
        String value = annotation.value();
        assertTrue(value.contains("ROLE_ADMIN") || value.contains("ADMIN"),
                "Class-level @PreAuthorize should include ADMIN role");
    }

    @Test
    @DisplayName("registerVendor requires elevated role")
    void testRegisterVendor_requiresRole() {
        Method m = findMethod("registerVendor");
        assertNotNull(m, "registerVendor method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "registerVendor must have @PreAuthorize");
        assertTrue(ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER"),
                "registerVendor must require elevated role");
    }

    @Test
    @DisplayName("verifyVendor requires ADMIN or DEPARTMENT_MANAGER")
    void testVerifyVendor_requiresAdminOrManager() {
        Method m = findMethod("verifyVendor");
        assertNotNull(m, "verifyVendor method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "verifyVendor must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("ADMIN") || value.contains("DEPARTMENT_MANAGER"),
                "verifyVendor must require ADMIN or DEPARTMENT_MANAGER");
    }

    @Test
    @DisplayName("createContract requires elevated role")
    void testCreateContract_requiresRole() {
        Method m = findMethod("createContract");
        assertNotNull(m, "createContract method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "createContract must have @PreAuthorize");
        assertTrue(ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER"),
                "createContract must require elevated role");
    }

    @Test
    @DisplayName("approvePurchaseOrder requires ADMIN or DEPARTMENT_MANAGER")
    void testApprovePO_requiresManagerRole() {
        Method m = findMethod("approvePurchaseOrder");
        assertNotNull(m, "approvePurchaseOrder method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "approvePurchaseOrder must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("ADMIN") || value.contains("DEPARTMENT_MANAGER"),
                "approvePurchaseOrder must require manager role");
    }

    @Test
    @DisplayName("rejectPurchaseOrder requires ADMIN or DEPARTMENT_MANAGER")
    void testRejectPO_requiresManagerRole() {
        Method m = findMethod("rejectPurchaseOrder");
        assertNotNull(m, "rejectPurchaseOrder method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "rejectPurchaseOrder must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("ADMIN") || value.contains("DEPARTMENT_MANAGER"),
                "rejectPurchaseOrder must require manager role");
    }

    @Test
    @DisplayName("issuePurchaseOrder requires ADMIN or DEPARTMENT_MANAGER")
    void testIssuePO_requiresManagerRole() {
        Method m = findMethod("issuePurchaseOrder");
        assertNotNull(m, "issuePurchaseOrder method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "issuePurchaseOrder must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("ADMIN") || value.contains("DEPARTMENT_MANAGER"),
                "issuePurchaseOrder must require manager role");
    }

    @Test
    @DisplayName("receiveDelivery is accessible to EMPLOYEE and DEPT_ADMIN")
    void testReceiveDelivery_requiresAnyStaffRole() {
        Method m = findMethod("receiveDelivery");
        assertNotNull(m, "receiveDelivery method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "receiveDelivery must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("EMPLOYEE") || value.contains("DEPT_ADMIN") || value.contains("ADMIN"),
                "receiveDelivery must allow staff-level access");
    }

    @Test
    @DisplayName("evaluateVendor requires elevated role")
    void testEvaluateVendor_requiresElevatedRole() {
        Method m = findMethod("evaluateVendor");
        assertNotNull(m, "evaluateVendor method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "evaluateVendor must have @PreAuthorize");
        assertTrue(ann.value().contains("ADMIN") || ann.value().contains("DEPARTMENT_MANAGER") || ann.value().contains("DEPT_ADMIN"),
                "evaluateVendor must require elevated role");
    }

    @Test
    @DisplayName("acceptQuotation requires ADMIN or DEPARTMENT_MANAGER")
    void testAcceptQuotation_requiresManagerRole() {
        Method m = findMethod("acceptQuotation");
        assertNotNull(m, "acceptQuotation method must exist");
        PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
        assertNotNull(ann, "acceptQuotation must have @PreAuthorize");
        String value = ann.value();
        assertTrue(value.contains("ADMIN") || value.contains("DEPARTMENT_MANAGER"),
                "acceptQuotation must require manager role");
    }

    @Test
    @DisplayName("searchVendors is accessible to all authorized roles")
    void testSearchVendors_publicToAuthorizedRoles() {
        Method m = findMethod("searchVendors");
        assertNotNull(m, "searchVendors must exist");
        // searchVendors has no method-level annotation — inherits class-level guard
        PreAuthorize methodAnn = m.getAnnotation(PreAuthorize.class);
        // It's fine to be null — class-level guard covers it
        // But class must have the guard
        PreAuthorize classAnn = ProcurementController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(classAnn, "Class-level @PreAuthorize must be present to protect searchVendors");
    }

    @Test
    @DisplayName("All expected endpoints are present on the controller")
    void testExpectedEndpointsExist() {
        String[] expectedMethods = {
                "registerVendor", "searchVendors", "getVendorById", "verifyVendor",
                "createContract", "searchContracts", "getContractById",
                "submitQuotation", "searchQuotations", "acceptQuotation",
                "createPurchaseOrder", "searchPurchaseOrders", "getPurchaseOrderById",
                "submitForApproval", "approvePurchaseOrder", "rejectPurchaseOrder", "issuePurchaseOrder",
                "receiveDelivery", "getDeliveryById", "getDeliveriesByPurchaseOrder",
                "evaluateVendor", "getVendorEvaluations",
                "getProcurementHistoryForVendor"
        };

        for (String methodName : expectedMethods) {
            Method m = findMethod(methodName);
            assertNotNull(m, "Controller must have method: " + methodName);
        }
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private Method findMethod(String name) {
        return Arrays.stream(ProcurementController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
