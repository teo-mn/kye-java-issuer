package io.corexchain.verifyservice.issuer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class IssuerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(IssuerConsumer.class);

    @RabbitListener(queues = "${verify.config.rbmq.queue}")
    public void readJson(String message) {
        logger.info(message);
    }
}
