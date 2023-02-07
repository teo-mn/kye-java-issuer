package io.corexchain.verifyservice.issuer.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;

@Getter
@Setter
public class EmployeeCardIssueDTO extends EmployeeCardDTO {
    @NotEmpty(message = "[fn] First name should not be empty")
    @Length(min = 2, max = 120)
    protected String fn;
    @NotEmpty(message = "[ln] Last name should not be empty")
    @Length(min = 2, max = 120)
    protected String ln;
    @NotEmpty(message = "[oid] Organization should not be empty")
    @Length(min = 2, max = 50)
    protected String oid;
    @NotEmpty(message = "[im] Image url should not be empty")
    @Length(min = 2, max = 1000)
    protected String im;
    @NotEmpty(message = "[po] Position should not be empty")
    @Length(min = 2, max = 25)
    protected String po;

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
