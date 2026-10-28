package com.civicfix.platform.sla.dto;

import java.time.Instant;

public class SlaEvaluationResultDto {

    private int evaluatedCount;
    private int activeCount;
    private int warningCount;
    private int overdueCount;
    private int breachedCount;
    private int escalatedCount;
    private Instant evaluatedAt;

    public SlaEvaluationResultDto() {
        this.evaluatedAt = Instant.now();
    }

    public SlaEvaluationResultDto(int evaluatedCount, int activeCount, int warningCount,
                                  int overdueCount, int breachedCount, int escalatedCount) {
        this.evaluatedCount = evaluatedCount;
        this.activeCount = activeCount;
        this.warningCount = warningCount;
        this.overdueCount = overdueCount;
        this.breachedCount = breachedCount;
        this.escalatedCount = escalatedCount;
        this.evaluatedAt = Instant.now();
    }

    public int getEvaluatedCount() {
        return evaluatedCount;
    }

    public void setEvaluatedCount(int evaluatedCount) {
        this.evaluatedCount = evaluatedCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(int overdueCount) {
        this.overdueCount = overdueCount;
    }

    public int getBreachedCount() {
        return breachedCount;
    }

    public void setBreachedCount(int breachedCount) {
        this.breachedCount = breachedCount;
    }

    public int getEscalatedCount() {
        return escalatedCount;
    }

    public void setEscalatedCount(int escalatedCount) {
        this.escalatedCount = escalatedCount;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
