package com.civicfix.platform.complaints.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.dto.DuplicateLinkRequest;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.event.ComplaintCreatedEvent;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service providing duplicate complaint management and the integration point
 * for Member 4's AI duplicate detection engine.
 */
@Service
public class ComplaintDuplicateService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintDuplicateService.class);

    private final ComplaintRepository complaintRepository;
    private final AuditService auditService;

    public ComplaintDuplicateService(ComplaintRepository complaintRepository,
                                     AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.auditService = auditService;
    }

    /**
     * Integration hook for new complaints. When Member 4 connects the AI duplicate service,
     * this listener forwards complaint metadata to the Python FastAPI AI service.
     */
    @EventListener
    public void onComplaintCreated(ComplaintCreatedEvent event) {
        Complaint complaint = event.getComplaint();
        log.info("Complaint created event received for tracking number: {}. Ready for AI duplicate analysis.",
                complaint.getTrackingNumber());
    }

    @Transactional
    public ComplaintDto linkAsDuplicate(Long complaintId, DuplicateLinkRequest request, String performedBy) {
        if (complaintId.equals(request.getPrimaryComplaintId())) {
            throw new BadRequestException("A complaint cannot be marked as duplicate of itself");
        }

        Complaint duplicateComplaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        Complaint primaryComplaint = complaintRepository.findById(request.getPrimaryComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Primary Complaint", "id", request.getPrimaryComplaintId()));

        String duplicateGroupId = primaryComplaint.getDuplicateGroupId();
        if (duplicateGroupId == null || duplicateGroupId.isBlank()) {
            duplicateGroupId = "DUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            primaryComplaint.setDuplicateGroupId(duplicateGroupId);
            complaintRepository.save(primaryComplaint);
        }

        duplicateComplaint.setDuplicate(true);
        duplicateComplaint.setDuplicateOf(primaryComplaint);
        duplicateComplaint.setDuplicateGroupId(duplicateGroupId);
        duplicateComplaint.setDuplicateSimilarityScore(request.getSimilarityScore());
        duplicateComplaint.setDuplicateConfirmed(true);

        Complaint saved = complaintRepository.save(duplicateComplaint);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.UPDATE, performedBy,
                String.format("Linked as duplicate of %s (Group: %s). Reason: %s",
                        primaryComplaint.getTrackingNumber(), duplicateGroupId, request.getReason()));

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional
    public ComplaintDto unlinkDuplicate(Long complaintId, String performedBy) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        if (!complaint.isDuplicate() && complaint.getDuplicateOf() == null) {
            throw new BadRequestException("Complaint is not marked as duplicate");
        }

        String previousPrimaryTracking = complaint.getDuplicateOf() != null ?
                complaint.getDuplicateOf().getTrackingNumber() : "unknown";

        complaint.setDuplicate(false);
        complaint.setDuplicateOf(null);
        complaint.setDuplicateConfirmed(false);
        complaint.setDuplicateSimilarityScore(null);

        Complaint saved = complaintRepository.save(complaint);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.UPDATE, performedBy,
                String.format("Unlinked duplicate from %s", previousPrimaryTracking));

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> getDuplicatesOf(Long complaintId) {
        return complaintRepository.findByDuplicateOfId(complaintId).stream()
                .map(ComplaintDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> getDuplicateGroup(String duplicateGroupId) {
        return complaintRepository.findByDuplicateGroupId(duplicateGroupId).stream()
                .map(ComplaintDto::fromEntity)
                .collect(Collectors.toList());
    }
}
