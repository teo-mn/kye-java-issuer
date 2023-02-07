package io.corexchain.verifyservice.issuer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.corexchain.verify4j.exceptions.AlreadyExistsException;
import io.corexchain.verify4j.exceptions.BlockchainNodeException;
import io.corexchain.verifyservice.issuer.model.*;
import io.corexchain.verifyservice.issuer.service.EmployeeCardIssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
@ConditionalOnProperty(value = "verify.config.rbmq.enabled", havingValue = "true", matchIfMissing = false)
public class IssuerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(IssuerConsumer.class);
    private RabbitTemplate rabbitTemplate;
    EmployeeCardIssuerService service;

    private String responseQueue;

    IssuerConsumer(EmployeeCardIssuerService service, RabbitTemplate rabbitTemplate, @Value("${verify.config.rbmq.queue}") String queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.responseQueue = queue + "_response";
        this.service = service;
    }

    @RabbitListener(queues = "${verify.config.rbmq.queue}")
    public void readJson(String message) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        EmployeeCardRequestDTO request = null;
        try {
            request = mapper.readValue(message, EmployeeCardRequestDTO.class);
            if (EmployeeCardAction.ADD.equals(request.getAction())) {
                String result = service.issueJson(mapper.readValue(message, EmployeeCardIssueRequestDTO.class));
                rabbitTemplate.convertAndSend(responseQueue, result);
            } else if (EmployeeCardAction.REVOKE.equals(request.getAction())) {
                EmployeeCardRevokeRequestDTO revokeDTO = mapper.readValue(message, EmployeeCardRevokeRequestDTO.class);
                if(Objects.isNull(revokeDTO.getData().getRevokerName()))
                    revokeDTO.getData().setRevokerName("system");
                String result = service.revokeJson(revokeDTO);
                rabbitTemplate.convertAndSend(responseQueue, result);
            }
        } catch (AlreadyExistsException e) {
            logger.error(e.getMessage());
            rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"Certification hash already existed in the smart contract.\", \"data\":\"" + message + "\"}");
        } catch (SocketTimeoutException | NoSuchAlgorithmException | JsonProcessingException | BlockchainNodeException e) {
            logger.error(e.getMessage(), e);
            rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"" + e.getMessage() + "\", \"data\":\"" + message + "\"}");
        }
    }
}
