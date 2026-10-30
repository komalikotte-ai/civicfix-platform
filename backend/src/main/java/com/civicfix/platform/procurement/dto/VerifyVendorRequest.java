package com.civicfix.platform.procurement.dto;

import com.civicfix.platform.procurement.enums.VendorVerificationStatus;
import jakarta.validation.constraints.NotNull;

public class VerifyVendorRequest {

    @NotNull(message = "Verification status is required")
    private VendorVerificationStatus status;

    private String verificationNotes;
    private String bankAccountNo;
    private String bankIfsc;
    private String vendorType;

    public VerifyVendorRequest() {}

    public VendorVerificationStatus getStatus() { return status; }
    public void setStatus(VendorVerificationStatus status) { this.status = status; }
    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
    public String getBankAccountNo() { return bankAccountNo; }
    public void setBankAccountNo(String bankAccountNo) { this.bankAccountNo = bankAccountNo; }
    public String getBankIfsc() { return bankIfsc; }
    public void setBankIfsc(String bankIfsc) { this.bankIfsc = bankIfsc; }
    public String getVendorType() { return vendorType; }
    public void setVendorType(String vendorType) { this.vendorType = vendorType; }
}
