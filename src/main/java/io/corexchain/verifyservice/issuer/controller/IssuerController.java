package io.corexchain.verifyservice.issuer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.corexchain.verifyservice.issuer.model.*;
import io.corexchain.verifyservice.issuer.service.EmployeeCardIssuerService;
import io.corexchain.verifyservice.issuer.service.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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
    private EmployeeCardIssuerService ecService;
    private RabbitTemplate rabbitTemplate;

    @Value("${verify.config.rbmq.queue}")
    private String queue;
    @Value("${verify.config.rbmq.enabled}")
    private Boolean rbmqEnabled;

    @Value("${verify.service.file.directory}")
    private String rootPath;

    public IssuerController(IssuerService service,
                            EmployeeCardIssuerService ecService,
                            RabbitTemplate rabbitTemplate) {
        this.service = service;
        this.ecService = ecService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<String> issueJson(@Valid @RequestBody EmployeeCardIssueDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            body.action = EmployeeCardAction.ADD;
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            return ResponseEntity.ok("{}");
        } else {
            return ResponseEntity.ok(this.ecService.issueJson(body));
        }
    }

    @PutMapping
    public ResponseEntity<String> updateJson(@Valid @RequestBody EmployeeCardIssueDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            body.action = EmployeeCardAction.REVOKE;
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            body.action = EmployeeCardAction.ADD;
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            return ResponseEntity.ok("{}");
        } else {
            EmployeeCardRevokeDTO revokeDTO = new EmployeeCardRevokeDTO();
            revokeDTO.pn = body.pn;
            revokeDTO.rn = body.rn;
            revokeDTO.revokerName = body.oid;
            this.ecService.revokeJson(revokeDTO);
            String qr = this.ecService.issueJson(body);
            return ResponseEntity.ok(qr);
        }
    }

    @PostMapping("revoke")
    public ResponseEntity<String> revokeJson(@Valid @RequestBody EmployeeCardRevokeDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
        } else {
            this.ecService.revokeJson(body);
        }
        return ResponseEntity.ok("{}");
    }

    @PostMapping("issue")
    public ResponseEntity<IssueResponseDTO> issue(@Valid @RequestBody IssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        IssueResponseDTO res = this.service.issue(this.rootPath + body.sourcePath,
                this.rootPath + body.destinationPath, body.expireDate, body.desc);
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
