package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class EmployeeCardRequestDTO {
    protected EmployeeCardAction action;
    @NotEmpty(message = "[requestId] requestId should not be empty")
    protected String requestId;
}
