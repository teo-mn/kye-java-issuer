package io.corexchain.verifyservice.issuer.model;

import org.hibernate.validator.constraints.Length;

public class EmployeeVerifyDTO extends EmployeeCardDTO {
    @Length(max = 100)
    public String sc;
}
