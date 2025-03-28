package com.mary.product_microservice_sharik.config;

import com.mary.product_microservice_sharik.model.enums.KafkaTopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic createAdminTopic() {
        return new NewTopic(KafkaTopicEnum.ADMIN_TOPIC.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic createProductTopic() {
        return new NewTopic(KafkaTopicEnum.PRODUCT_TOPIC.name(), 1, (short) 2);
    }
}