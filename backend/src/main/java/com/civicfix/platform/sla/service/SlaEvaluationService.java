package com.civicfix.platform.sla.service;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.sla.dto.SlaEvaluationResultDto;
import com.civicfix.platform.sla.enums.SlaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SlaEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(SlaEvaluationService.class);

    private final ComplaintRepository complaintRepository;
    private final EscalationService escalationService;

    // Active complaints eligible for SLA monitoring
    private static final List<ComplaintStatus> ACTIVE_STATUSES = List.of(
            ComplaintStatus.NEW,
            ComplaintStatus.SUBMITTED,
            ComplaintStatus.ACKNOWLEDGED,
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.ON_HOLD,
            ComplaintStatus.REOPENED
    );

    public SlaEvaluationService(ComplaintRepository complaintRepository,
                                EscalationService escalationService) {
        this.complaintRepository = complaintRepository;
        this.escalationService = escalationService;
    }

    /**
     * Periodic background evaluation job. Runs every 15 minutes by default.
     * Can also be triggered directly or via REST endpoint.
     */
    @Scheduled(fixedDelayString = "${civicfix.sla.evaluation-interval-ms:900000}", initialDelay = 60000)
    public void scheduledSlaEvaluation() {
        log.info("Starting scheduled SLA and escalation evaluation run...");
        SlaEvaluationResultDto result = evaluateAllActiveComplaints();
        log.info("SLA evaluation complete: {} evaluated, {} in warning, {} breached, {} escalated.",
                result.getEvaluatedCount(), result.getWarningCount(), result.getBreachedCount(), result.getEscalatedCount());
    }

    @Transactional
    public SlaEvaluationResultDto evaluateAllActiveComplaints() {
        Instant now = Instant.now();
        List<Complaint> activeComplaints = complaintRepository.findByStatusIn(ACTIVE_STATUSES);

        int evaluated = 0;
        int active = 0;
        int warning = 0;
        int overdue = 0;
        int breached = 0;
        int escalated = 0;

        for (Complaint complaint : activeComplaints) {
            evaluated++;
            boolean wasEscalated = escalationService.evaluateAndEscalate(complaint, now);
            if (wasEscalated) {
                escalated++;
            }

            SlaState state = complaint.getSlaState();
            if (state == SlaState.ACTIVE) active++;
            else if (state == SlaState.WARNING) warning++;
            else if (state == SlaState.OVERDUE) overdue++;
            else if (state == SlaState.BREACHED) breached++;
        }

        return new SlaEvaluationResultDto(evaluated, active, warning, overdue, breached, escalated);
    }
}
