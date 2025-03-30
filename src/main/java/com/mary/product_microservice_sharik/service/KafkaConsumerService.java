package com.mary.product_microservice_sharik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class KafkaConsumerService {

    private final ProductService productService;
    private final KafkaTemplate<?, ?> kafkaTemplate;

    @KafkaListener(
            topics = "#{T(com.mary.product_microservice_sharik.model.enums.KafkaTopicEnum).PRODUCT_BY_FILTER_TOPIC.name()}",
            groupId = "product_group")
    public void productsByFilter(ConsumerRecord<String,String> message) throws JsonProcessingException {
        try {
            System.out.println("message: "+message);

            // Извлекаем данные из запроса
            String filterJson = message.value();

            System.out.println("filterJson: "+filterJson);

            ProductSearchFilterDTO filter = new ObjectMapper().readValue(filterJson, ProductSearchFilterDTO.class);

            if(filter==null){
                filter = ProductSearchFilterDTO.defaultFilter();
            }

            // Обрабатываем запрос
            List<Product> products = productService.findProductsByFilterOnPage(filter);

            System.out.println("products: "+products);

            // Сериализуем результат в JSON
            String resultJson = new ObjectMapper().writeValueAsString(products);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = new ObjectMapper().writeValueAsString("External error");
            sendResponse(message, resultJson,true);
        }
    }

    @KafkaListener(
            topics = "#{T(com.mary.product_microservice_sharik.model.enums.KafkaTopicEnum).PRODUCT_BY_ID_TOPIC.name()}",
            groupId = "product_group")
    public void productById(ConsumerRecord<String,String> message) throws JsonProcessingException {
        try {
            System.out.println("message: "+message);

            // Извлекаем данные из запроса
            String idJson = message.value();

            System.out.println("idJson: "+idJson);

            String id = new ObjectMapper().readValue(idJson, String.class);

            System.out.println("id: "+id);

            // Обрабатываем запрос
            Product product = productService.findById(id);

            System.out.println("product: "+product);

            // Сериализуем результат в JSON
            String resultJson = new ObjectMapper().writeValueAsString(product);

            System.out.println("resultJson: "+resultJson);

            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = new ObjectMapper().writeValueAsString("External error");
            sendResponse(message, resultJson,true);
        }
    }

    @KafkaListener(
            topics = "#{T(com.mary.product_microservice_sharik.model.enums.KafkaTopicEnum).PRODUCT_SET_STATUS_TOPIC.name()}",
            groupId = "product_group")
    public void setProductStatus(ConsumerRecord<String,String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String prodStatJson = message.value();
            SetProductStatusDTO productStatusDTO = new ObjectMapper().readValue(prodStatJson, SetProductStatusDTO.class);

            // Обрабатываем запрос
            productService.setProductStatus(productStatusDTO);

            // Сериализуем результат в JSON
            String resultJson = new ObjectMapper().writeValueAsString(true);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = new ObjectMapper().writeValueAsString("External error");
            sendResponse(message, resultJson, true);
        }
    }

    @KafkaListener(
            topics = "#{T(com.mary.product_microservice_sharik.model.enums.KafkaTopicEnum).PRODUCT_CREATE_TOPIC.name()}",
            groupId = "product_group")
    public void createProduct(ConsumerRecord<String,String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String newProductJson = message.value();
            AddProductDTO newProductDTO = new ObjectMapper().readValue(newProductJson, AddProductDTO.class);

            // Обрабатываем запрос
            productService.create(newProductDTO);

            // Сериализуем результат в JSON
            String resultJson = new ObjectMapper().writeValueAsString(true);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = new ObjectMapper().writeValueAsString("External error");
            sendResponse(message, resultJson, true);
        }
    }

    private void sendResponse(ConsumerRecord<String, String> message, String resultJson, boolean isError){
        // Создаем ответное сообщение
        Message<String> reply;
        if(isError){
            reply = MessageBuilder
                    .withPayload(resultJson)
                    .setHeader(KafkaHeaders.EXCEPTION_MESSAGE, resultJson)
                    .setHeader(KafkaHeaders.TOPIC, message.headers().lastHeader(KafkaHeaders.REPLY_TOPIC).value())
                    .setHeader(KafkaHeaders.CORRELATION_ID, message.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value())
                    .build();

            System.out.println("it is an error");
        }else{
            reply = MessageBuilder
                    .withPayload(resultJson)
                    .setHeader(KafkaHeaders.TOPIC, message.headers().lastHeader(KafkaHeaders.REPLY_TOPIC).value())
                    .setHeader(KafkaHeaders.CORRELATION_ID, message.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value())
                    .build();
        }

        System.out.println("reply: "+reply);

        // Отправляем ответ в reply-topic
        kafkaTemplate.send(reply);
    }
}