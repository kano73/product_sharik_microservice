package com.mary.product_microservice_sharik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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
        AddProductDTO newProductDTO;
        try {
            newProductDTO = objectMapper.readValue(message.value(), AddProductDTO.class);

        } catch (Exception e) {
            sendResponse(message, "exception while parsing data from request", true);
            return;
        }
        // Обрабатываем запрос
        productService.create(newProductDTO);
        sendResponse(message, true, false);
    }

    public void sendProductsByFilter(ConsumerRecord<String, String> message) throws JsonProcessingException {
        ProductSearchFilterDTO filter;
        try {
            filter = objectMapper.readValue(message.value(), ProductSearchFilterDTO.class);
            filter = filter == null ? ProductSearchFilterDTO.defaultFilter(): filter;
        } catch (Exception e) {
            sendResponse(message, "exception while parsing data from request", true);
            return;
        }
        // Обрабатываем запрос
        List<Product> products = productService.findProductsByFilterOnPage(filter);

        sendResponse(message, products, false);
    }

    public void sendProductById(ConsumerRecord<String, String> message) throws JsonProcessingException {
        String id;
        try {
            id = objectMapper.readValue(message.value(), String.class);

        } catch (Exception e) {
            sendResponse(message, "exception while parsing data from request",true);
            return;
        }

        // Обрабатываем запрос
        Product product = productService.findById(id);

        sendResponse(message, product, false);
    }

    public void sendProductsByIds(ConsumerRecord<String, String> message) throws JsonProcessingException {
        List<String> ids;
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
            ids = objectMapper.readValue(message.value(), listType);
        } catch (Exception e) {
            sendResponse(message, "exception while parsing data from request",true);
            return;
        }

        // Обрабатываем запрос
        List<Product> products = productService.findProductsByIds(ids);

        sendResponse(message, products, false);
    }

    public void setProductStatus(ConsumerRecord<String, String> message) throws JsonProcessingException {
        SetProductStatusDTO productStatusDTO;
        try {
            productStatusDTO = objectMapper.readValue(message.value(), SetProductStatusDTO.class);
        } catch (Exception e) {
            sendResponse(message, "exception while parsing data from request", true);
            return;
        }

        // Обрабатываем запрос
        productService.setProductStatus(productStatusDTO);

        sendResponse(message, true, false);
    }

    private <D> void sendResponse(ConsumerRecord<String, String> message, D data, boolean isError) throws JsonProcessingException {
        String resultJson = objectMapper.writeValueAsString(data);

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
