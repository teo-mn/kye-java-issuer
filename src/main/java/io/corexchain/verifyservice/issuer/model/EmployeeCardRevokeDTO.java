package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;

@Getter
@Setter
public class EmployeeCardRevokeDTO extends EmployeeCardDTO {

    @NotEmpty(message = "[revokerName] Revoker name should not be empty")
    @Length(min = 2, max = 100)
    private String revokerName;

}
