package com.civicfix.platform.workflow.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.workflow.dto.WorkflowTransitionDto;
import com.civicfix.platform.workflow.service.WorkflowEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final WorkflowEngine workflowEngine;

    public WorkflowController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @GetMapping("/complaints/{complaintId}/history")
    public ApiResponse<List<WorkflowTransitionDto>> getHistory(@PathVariable Long complaintId) {
        return ApiResponse.ok(workflowEngine.getTransitionsForComplaint(complaintId));
    }
}
