package io.corexchain.verifyservice.issuer.model;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

public class IssueRequestDTO {
    @NotEmpty(message = "Source path should not be empty")
    public String sourcePath;
    @NotEmpty(message = "Destination path should not be empty")
    public String destinationPath;
    public Date expireDate;
    public String desc = "";
}
