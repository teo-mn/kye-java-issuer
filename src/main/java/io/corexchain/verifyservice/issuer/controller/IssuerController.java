package io.corexchain.verifyservice.issuer.controller;

import io.corexchain.verifyservice.issuer.model.IssueRequestDTO;
import io.corexchain.verifyservice.issuer.model.IssueResponseDTO;
import io.corexchain.verifyservice.issuer.service.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.corexchain.verifyservice.issuer.exceptions.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/issuer")
public class IssuerController {
    private static final Logger logger = LoggerFactory.getLogger(IssuerController.class);

    private IssuerService service;
    public IssuerController(IssuerService service) {
        this.service = service;
    }

    @PostMapping("issue")
    public ResponseEntity<IssueResponseDTO> issue(@Valid @RequestBody IssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        IssueResponseDTO res = this.service.issue(body.sourcePath, body.destinationPath, body.expireDate, body.desc);
        return ResponseEntity.ok(res);
    }

    @PostMapping("issue-test")
    public ResponseEntity<IssueResponseDTO> issueTest(@Valid @RequestBody IssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        IssueResponseDTO res = this.service.issueTest(body.sourcePath, body.destinationPath, body.expireDate, body.desc);
        return ResponseEntity.ok(res);
    }

}
