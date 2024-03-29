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
            logger.info("[" + request.getRequestId()+  "]" + " New request: Action: " + request.getAction());
            if (EmployeeCardAction.ADD.equals(request.getAction())) {
                String result = service.issueJson(mapper.readValue(message, EmployeeCardIssueRequestDTO.class));
                rabbitTemplate.convertAndSend(responseQueue, result);
                logger.info("[" + request.getRequestId()+  "]" + " Issue success");
            } else if (EmployeeCardAction.REVOKE.equals(request.getAction())) {
                EmployeeCardRevokeRequestDTO revokeDTO = mapper.readValue(message, EmployeeCardRevokeRequestDTO.class);
                if (Objects.isNull(revokeDTO.getData().getRevokerName()))
                    revokeDTO.getData().setRevokerName("system");
                String result = service.revokeJson(revokeDTO);
                rabbitTemplate.convertAndSend(responseQueue, result);
                logger.info("[" + request.getRequestId()+  "]" + " Revoke success");
            } else if (EmployeeCardAction.UPDATE.equals(request.getAction())) {
                logger.info("[" + request.getRequestId()+  "]" + " Update process starting...");
                EmployeeCardUpdateRequestDTO updateDTO = mapper.readValue(message, EmployeeCardUpdateRequestDTO.class);
                if (Objects.isNull(updateDTO.getNewData()) || Objects.isNull(updateDTO.getOldData())) {
                    rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"" + "Error occurred during deserialization of input JSON" + "\", \"data\":" + message + "}");
                }
                EmployeeCardRevokeRequestDTO revokeDTO = new EmployeeCardRevokeRequestDTO();
                revokeDTO.setAction(EmployeeCardAction.REVOKE);
                revokeDTO.setRequestId(updateDTO.getRequestId());
                revokeDTO.setData(updateDTO.getOldData());
                if (Objects.isNull(revokeDTO.getData().getRevokerName()))
                    revokeDTO.getData().setRevokerName("system");
                service.revokeJson(revokeDTO);

                logger.info("[" + request.getRequestId()+  "]" + " Revoke success");

                EmployeeCardIssueRequestDTO requestIssue = new EmployeeCardIssueRequestDTO();
                requestIssue.setAction(EmployeeCardAction.UPDATE);
                requestIssue.setData(updateDTO.getNewData());
                requestIssue.setRequestId(updateDTO.getRequestId());
                String qr = service.issueJson(requestIssue);
                rabbitTemplate.convertAndSend(responseQueue, qr);

                logger.info("[" + request.getRequestId()+  "]" + " Add success");
                logger.info("[" + request.getRequestId()+  "]" + " Update process done.");

            }
        } catch (AlreadyExistsException e) {
            logger.error(e.getMessage());
            rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"Certification hash already existed in the smart contract.\", \"data\":" + message + "}");
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"" + "Error occurred during deserialization of input JSON" + "\", \"data\":" + message + "}");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rabbitTemplate.convertAndSend(responseQueue, "{\"error\": \"" + "Unknown error occurred" + "\", \"data\":" + message + "}");
        }
    }
}
