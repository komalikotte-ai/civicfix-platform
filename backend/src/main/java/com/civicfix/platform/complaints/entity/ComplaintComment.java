package com.civicfix.platform.complaints.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.users.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "complaint_comments")
public class ComplaintComment extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "author_name", nullable = false, length = 150)
    private String authorName;

    @Column(name = "author_role", length = 50)
    private String authorRole;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "internal_only", nullable = false)
    private boolean internalOnly = false;

    public ComplaintComment() {
    }

    public ComplaintComment(Complaint complaint, User author, String authorName, String authorRole, String comment, boolean internalOnly) {
        this.complaint = complaint;
        this.author = author;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.comment = comment;
        this.internalOnly = internalOnly;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }
}
