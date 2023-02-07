package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class EmployeeCardIssueRequestDTO extends EmployeeCardRequestDTO {

    @Valid()
    protected EmployeeCardIssueDTO data;
}
