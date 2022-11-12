package io.corexchain.verifyservice.issuer.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class EmployeeCardIssueDTO {
    @NotEmpty(message = "[fn] First name should not be empty")
    public String fn;
    @NotEmpty(message = "[ln] Last name should not be empty")
    public String ln;
    @NotEmpty(message = "[oid] Organization should not be empty")
    public String oid;
    @NotEmpty(message = "[rn] Citizen registration number should not be empty")
    public String rn;
    @NotEmpty(message = "[pn] Phone number should not be empty")
    @Length(min = 8, max = 20)
    public String pn;
    @NotEmpty(message = "[eid] Employee ID should not be empty")
    public String eid;
    @NotEmpty(message = "[im] Image url should not be empty")
    public String im;
    @NotEmpty(message = "[po] Position should not be empty")
    public String po;
}
