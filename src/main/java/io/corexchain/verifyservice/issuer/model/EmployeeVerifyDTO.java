package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class EmployeeVerifyDTO extends EmployeeCardDTO {
    @Length(max = 100)
    private String sc;
}
