package com.civicfix.platform.complaints.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.users.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "complaint_status_histories")
public class ComplaintStatusHistory extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ComplaintStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private ComplaintStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by_user_id")
    private User actionByUser;

    @Column(name = "action_by_username", length = 100)
    private String actionByUsername;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "transition_time", nullable = false)
    private Instant transitionTime = Instant.now();

    public ComplaintStatusHistory() {
    }

    public ComplaintStatusHistory(Complaint complaint, ComplaintStatus fromStatus, ComplaintStatus toStatus,
                                  User actionByUser, String actionByUsername, String remarks) {
        this.complaint = complaint;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actionByUser = actionByUser;
        this.actionByUsername = actionByUsername;
        this.remarks = remarks;
        this.transitionTime = Instant.now();
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public User getActionByUser() {
        return actionByUser;
    }

    public void setActionByUser(User actionByUser) {
        this.actionByUser = actionByUser;
    }

    public String getActionByUsername() {
        return actionByUsername;
    }

    public void setActionByUsername(String actionByUsername) {
        this.actionByUsername = actionByUsername;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(Instant transitionTime) {
        this.transitionTime = transitionTime;
    }
}
