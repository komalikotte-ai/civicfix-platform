package com.civicfix.platform.workorders.dto;

import java.math.BigDecimal;

public class CompleteWorkOrderRequest {

    private String completionNotes;

    private BigDecimal actualCost;

    private String evidenceMetadata;

    public CompleteWorkOrderRequest() {
    }

    public CompleteWorkOrderRequest(String completionNotes, BigDecimal actualCost, String evidenceMetadata) {
        this.completionNotes = completionNotes;
        this.actualCost = actualCost;
        this.evidenceMetadata = evidenceMetadata;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public String getEvidenceMetadata() {
        return evidenceMetadata;
    }

    public void setEvidenceMetadata(String evidenceMetadata) {
        this.evidenceMetadata = evidenceMetadata;
    }
}
