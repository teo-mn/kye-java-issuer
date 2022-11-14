package io.corexchain.verifyservice.issuer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.corexchain.verify4j.exceptions.AlreadyExistsException;
import io.corexchain.verifyservice.issuer.model.EmployeeCardIssueDTO;
import io.corexchain.verifyservice.issuer.model.EmployeeCardRevokeDTO;
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
        try {
            String result = service.issueJson(mapper.readValue(message, EmployeeCardIssueDTO.class));
            rabbitTemplate.convertAndSend(responseQueue, result);
        } catch (AlreadyExistsException e) {
            rabbitTemplate.convertAndSend(responseQueue, "{'error': 'Hash is already in Blockchain!', 'data':'"+message+"'}");
        } catch (SocketTimeoutException | NoSuchAlgorithmException | JsonProcessingException e) {
            logger.error(message, e);
        }
    }

    @RabbitListener(queues = "${verify.config.rbmq.queue.revoke}")
    public void revokeJson(String message) {
        logger.info(message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            service.revokeJson(mapper.readValue(message, EmployeeCardRevokeDTO.class));
        } catch (SocketTimeoutException | NoSuchAlgorithmException | JsonProcessingException e) {
            rabbitTemplate.convertAndSend(responseQueue, "{'error': '"+e.getMessage()+"', 'data':'"+message+"'}");
            throw new RuntimeException(e);
        }
    }
}
