package io.corexchain.verifyservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "verify.config.rbmq.enabled", havingValue = "true", matchIfMissing = false)
public class RBMQConfig {

    String queue;

    public RBMQConfig(@Value("${verify.config.rbmq.queue}") String queue) {
        this.queue = queue;
    }

    @Bean
    public Queue issuerQueue() {
        return new Queue(this.queue);
    }

    @Bean
    public Queue issuerQueueResponse() {
        return new Queue(this.queue + "_response");
    }
}
