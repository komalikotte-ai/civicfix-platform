package com.civicfix.platform.complaints.repository;

import com.civicfix.platform.complaints.entity.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, Long> {

    List<ComplaintAttachment> findByComplaintId(Long complaintId);
}
