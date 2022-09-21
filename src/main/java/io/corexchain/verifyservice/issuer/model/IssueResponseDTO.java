package io.corexchain.verifyservice.issuer.model;

public class IssueResponseDTO {
    public String txHash;

    public IssueResponseDTO(String txHash) {
        this.txHash = txHash;
    }
}
