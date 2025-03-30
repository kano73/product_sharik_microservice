package com.mary.product_microservice_sharik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RequestProcessingService {
    private final ProductService productService;
    private final KafkaTemplate<?, ?> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void createProduct(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String newProductJson = message.value();
            AddProductDTO newProductDTO = objectMapper.readValue(newProductJson, AddProductDTO.class);

            // Обрабатываем запрос
            productService.create(newProductDTO);

            // Сериализуем результат в JSON
            String resultJson = objectMapper.writeValueAsString(true);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = objectMapper.writeValueAsString("External error");
            sendResponse(message, resultJson, true);
        }
    }

    public void sendProductsByFilter(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String filterJson = message.value();

            ProductSearchFilterDTO filter = objectMapper.readValue(filterJson, ProductSearchFilterDTO.class);

            if (filter == null) {
                filter = ProductSearchFilterDTO.defaultFilter();
            }

            // Обрабатываем запрос
            List<Product> products = productService.findProductsByFilterOnPage(filter);

            // Сериализуем результат в JSON
            String resultJson = objectMapper.writeValueAsString(products);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = objectMapper.writeValueAsString("External error");
            sendResponse(message, resultJson, true);
        }
    }

    public void sendProductById(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String idJson = message.value();
            String id = objectMapper.readValue(idJson, String.class);

            // Обрабатываем запрос
            Product product = productService.findById(id);

            // Сериализуем результат в JSON
            String resultJson = objectMapper.writeValueAsString(product);

            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = objectMapper.writeValueAsString("External error");
            sendResponse(message, resultJson,true);
        }
    }

    public void setProductStatus(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            // Извлекаем данные из запроса
            String prodStatJson = message.value();
            SetProductStatusDTO productStatusDTO = objectMapper.readValue(prodStatJson, SetProductStatusDTO.class);

            // Обрабатываем запрос
            productService.setProductStatus(productStatusDTO);

            // Сериализуем результат в JSON
            String resultJson = objectMapper.writeValueAsString(true);
            sendResponse(message, resultJson, false);
        } catch (Exception e) {
            String resultJson = objectMapper.writeValueAsString("External error");
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
        }else{
            reply = MessageBuilder
                    .withPayload(resultJson)
                    .setHeader(KafkaHeaders.TOPIC, message.headers().lastHeader(KafkaHeaders.REPLY_TOPIC).value())
                    .setHeader(KafkaHeaders.CORRELATION_ID, message.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value())
                    .build();
        }
        // Отправляем ответ в reply-topic
        kafkaTemplate.send(reply);
    }
}
