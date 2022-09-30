package io.corexchain.verifyservice.issuer.controller;

import io.corexchain.verifyservice.issuer.model.IssueRequestDTO;
import io.corexchain.verifyservice.issuer.model.IssueResponseDTO;
import io.corexchain.verifyservice.issuer.service.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.corexchain.verifyservice.issuer.exceptions.*;

import javax.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/issuer")
public class IssuerController {
    private static final Logger logger = LoggerFactory.getLogger(IssuerController.class);

    private IssuerService service;

    public IssuerController(IssuerService service) {
        this.service = service;
    }


    @Value("${verify.service.file.directory}")
    private String rootPath;

    @PostMapping("issue")
    public ResponseEntity<IssueResponseDTO> issue(@Valid @RequestBody IssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        Path rootPath = Paths.get(this.rootPath);
        IssueResponseDTO res = this.service.issue(rootPath.resolve(body.sourcePath).toString(),
                rootPath.resolve(body.destinationPath).toString(), body.expireDate, body.desc);
        return ResponseEntity.ok(res);
    }

    @PostMapping("issue-test")
    public ResponseEntity<IssueResponseDTO> issueTest(@Valid @RequestBody IssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        IssueResponseDTO res = this.service.issueTest(this.rootPath + body.sourcePath,
                this.rootPath + body.destinationPath, body.expireDate, body.desc);
        return ResponseEntity.ok(res);
    }

}
