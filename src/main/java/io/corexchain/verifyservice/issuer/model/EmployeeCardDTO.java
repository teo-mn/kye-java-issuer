package io.corexchain.verifyservice.issuer.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;

public class EmployeeCardDTO {

    @NotEmpty(message = "[rn] Citizen registration number should not be empty")
    @Length(min = 8, max = 20)
    public String rn;
    @NotEmpty(message = "[pn] Phone number should not be empty")
    @Length(min = 8, max = 20)
    public String pn;
    @Length(min = 2, max = 50)
    public String eid;
    @NotEmpty(message = "[im] Image url should not be empty")

    public HashMap<String, String> getPhoneRegnumMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("pn", this.pn);
        result.put("rn", this.rn);
        return result;
    }

    public HashMap<String, String> getCertNumMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("rn", this.rn);
        result.put("eid", this.eid);
        return result;
    }
}
