package io.corexchain.verifyservice.issuer.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

public class EmployeeCardIssueDTO extends EmployeeCardDTO {
    @NotEmpty(message = "[fn] First name should not be empty")
    public String fn;
    @NotEmpty(message = "[ln] Last name should not be empty")
    public String ln;
    @NotEmpty(message = "[oid] Organization should not be empty")
    public String oid;
    @NotEmpty(message = "[eid] Employee ID should not be empty")
    public String eid;
    @NotEmpty(message = "[im] Image url should not be empty")
    public String im;
    @NotEmpty(message = "[po] Position should not be empty")
    public String po;
    //
    public String requestID;

    public HashMap<String, String> toMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("fn", this.fn);
        result.put("ln", this.ln);
        result.put("oid", this.oid);
        result.put("rn", this.rn);
        result.put("pn", this.pn);
        result.put("eid", this.eid);
        result.put("im", this.im);
        result.put("po", this.po);
        return result;
    }

}
