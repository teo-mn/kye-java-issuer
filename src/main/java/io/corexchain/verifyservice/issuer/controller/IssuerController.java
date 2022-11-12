package io.corexchain.verifyservice.issuer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.corexchain.verifyservice.issuer.model.EmployeeCardIssueDTO;
import io.corexchain.verifyservice.issuer.model.IssueRequestDTO;
import io.corexchain.verifyservice.issuer.model.IssueResponseDTO;
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
    private RabbitTemplate rabbitTemplate;

    @Value("${verify.config.rbmq.queue}")
    private String queue;
    @Value("${verify.config.rbmq.enabled}")
    private Boolean rbmqEnabled;

    @Value("${verify.service.file.directory}")
    private String rootPath;

    public IssuerController(IssuerService service,
                            RabbitTemplate rabbitTemplate) {
        this.service = service;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<IssueResponseDTO> issueJson(@Valid @RequestBody EmployeeCardIssueDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");

        if (rbmqEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            rabbitTemplate.convertAndSend(queue, mapper.writeValueAsString(body));
        } else {

        }
        return ResponseEntity.ok(new IssueResponseDTO(null));
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
