package io.corexchain.verifyservice.issuer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.corexchain.verifyservice.issuer.exceptions.BadRequestException;
import io.corexchain.verifyservice.issuer.model.*;
import io.corexchain.verifyservice.issuer.service.EmployeeCardIssuerService;
import io.corexchain.verifyservice.issuer.service.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/verifier")
public class VerifierController {
    private static final Logger logger = LoggerFactory.getLogger(VerifierController.class);

    private EmployeeCardIssuerService ecService;

    @Value("${verify.service.blockchain.contract.address}")
    private String smartContractAddress;

    public VerifierController(EmployeeCardIssuerService ecService) {
        this.ecService = ecService;
    }

    @PostMapping
    public ResponseEntity<Boolean> verify(@Valid @RequestBody EmployeeVerifyDTO body) throws Exception {
        if (Objects.isNull(body)) throw new BadRequestException("Дата хоосон байж болохгүй");
        return ResponseEntity.ok(this.ecService.isValid(body, body.sc));
    }
}