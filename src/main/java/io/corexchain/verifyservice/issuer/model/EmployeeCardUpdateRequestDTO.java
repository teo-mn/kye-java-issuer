package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EmployeeCardUpdateRequestDTO extends EmployeeCardRequestDTO {
    @Valid()
    @NotNull
    protected EmployeeCardRevokeDTO oldData;
    @NotNull
    @Valid()
    protected EmployeeCardIssueDTO newData;
}
