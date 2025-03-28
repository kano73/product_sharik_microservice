package com.mary.product_microservice_sharik.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(
            topics = "#{T(com.example.KafkaTopicEnum).PRODUCT_BY_FILTER_TOPIC.name()}",
            groupId = "product_group")
    public void productsByFilter(ConsumerRecord<String,String> message) {

        System.out.println("Получено сообщение: " + message);
    }

    @KafkaListener(
            topics = "#{T(com.example.KafkaTopicEnum).PRODUCT_BY_ID_TOPIC.name()}",
            groupId = "product_group")
    public void productById(ConsumerRecord<String,String> message) {

        System.out.println("Получено сообщение: " + message);
    }

    @KafkaListener(
            topics = "#{T(com.example.KafkaTopicEnum).PRODUCT_SET_STATUS_TOPIC.name()}",
            groupId = "product_group")
    public void setProductStatus(ConsumerRecord<String,String> message) {

        System.out.println("Получено сообщение: " + message);
    }

    @KafkaListener(
            topics = "#{T(com.example.KafkaTopicEnum).PRODUCT_CREATE_TOPIC.name()}",
            groupId = "product_group")
    public void createProduct(ConsumerRecord<String,String> message) {

        System.out.println("Получено сообщение: " + message);
    }
}