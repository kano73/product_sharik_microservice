package com.mary.product_microservice_sharik.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "example-topic", groupId = "my-group-id")
    public void consumeMessage(String message) {

        System.out.println("Получено сообщение: " + message);
    }
}