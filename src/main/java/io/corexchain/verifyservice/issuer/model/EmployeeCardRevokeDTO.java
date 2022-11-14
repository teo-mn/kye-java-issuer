package io.corexchain.verifyservice.issuer.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;

public class EmployeeCardRevokeDTO extends EmployeeCardDTO {

    @NotEmpty(message = "[revokerName] Revoker name should not be empty")
    public String revokerName;

}
