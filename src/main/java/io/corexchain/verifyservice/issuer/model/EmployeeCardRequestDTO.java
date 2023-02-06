package io.corexchain.verifyservice.issuer.model;

import javax.validation.constraints.NotEmpty;

public class EmployeeCardRequestDTO {
    public EmployeeCardAction action;
    @NotEmpty(message = "[requestId] requestId should not be empty")
    public String requestId;
}
