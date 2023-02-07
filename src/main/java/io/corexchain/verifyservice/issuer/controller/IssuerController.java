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

    private IssuerService service;
    private EmployeeCardIssuerService ecService;
    private RabbitTemplate rabbitTemplate;

    @Value("${verify.config.rbmq.queue}")
    private String queue;
    @Value("${verify.config.rbmq.enabled}")
    private Boolean rbmqEnabled;

    @Value("${verify.service.file.directory}")
    private String rootPath;

    @Value("${verify.service.blockchain.contract.address}")
    private String smartContractAddress;

    public IssuerController(IssuerService service,
                            EmployeeCardIssuerService ecService,
                            RabbitTemplate rabbitTemplate) {
        this.service = service;
        this.ecService = ecService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<String> issueJson(@Valid @RequestBody EmployeeCardIssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        if (Objects.isNull(body.getData())) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            body.setAction(EmployeeCardAction.ADD);
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            return ResponseEntity.ok("{\"sc\":\""+smartContractAddress+"\"}");
        } else {
            return ResponseEntity.ok(this.ecService.issueJson(body));
        }
    }

    @PutMapping
    public ResponseEntity<String> updateJson(@Valid @RequestBody EmployeeCardIssueRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            body.setAction(EmployeeCardAction.REVOKE);
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            body.setAction(EmployeeCardAction.ADD);
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
            return ResponseEntity.ok("{\"sc\":\""+smartContractAddress+"\"}");
        } else {
            EmployeeCardRevokeDTO revokeDTO = new EmployeeCardRevokeDTO();
            revokeDTO.setPn(body.getData().getPn());
            revokeDTO.setRn(body.getData().getRn());
            revokeDTO.setEid(body.getData().getEid());
            EmployeeCardRevokeRequestDTO request = new EmployeeCardRevokeRequestDTO();
            request.setData(revokeDTO);
            request.setRequestId(body.getRequestId());
            request.setAction(EmployeeCardAction.REVOKE);
            this.ecService.revokeJson(request);
            String qr = this.ecService.issueJson(body);
            return ResponseEntity.ok(qr);
        }
    }

    @DeleteMapping()
    public ResponseEntity<String> revokeJson(@Valid @RequestBody EmployeeCardRevokeRequestDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            body.setAction(EmployeeCardAction.REVOKE);
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
        } else {
            this.ecService.revokeJson(body);
        }
        return ResponseEntity.ok("{\"sc\":\""+smartContractAddress+"\"}");
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
