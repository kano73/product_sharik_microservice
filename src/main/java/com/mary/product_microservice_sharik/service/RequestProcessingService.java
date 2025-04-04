package com.mary.product_microservice_sharik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RequestProcessingService {
    private final ProductService productService;
    private final KafkaTemplate<?, ?> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void createProduct(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            AddProductDTO newProductDTO = objectMapper.readValue(message.value(), AddProductDTO.class);

            productService.create(newProductDTO);
        } catch (Exception e) {
            sendResponse(message, "Unable to get products: " + e.getMessage(),true);
            return;
        }

        sendResponse(message, true, false);
    }

    public void sendProductsByFilter(ConsumerRecord<String, String> message) throws JsonProcessingException {
        List<Product> products;
        try {
            ProductSearchFilterDTO filter = objectMapper.readValue(message.value(), ProductSearchFilterDTO.class);
            if(filter==null) filter = ProductSearchFilterDTO.defaultFilter();

            products = productService.findProductsByFilterOnPage(filter);
        } catch (Exception e) {
            sendResponse(message, "Unable to get products: " + e.getMessage(),true);
            return;
        }
        sendResponse(message, products, false);
    }

    public void sendProductById(ConsumerRecord<String, String> message) throws JsonProcessingException {
        Product product;
        try {
            String id = objectMapper.readValue(message.value(), String.class);

            product = productService.findById(id);
        } catch (Exception e) {
            sendResponse(message, "Unable to get product: " + e.getMessage(),true);
            return;
        }
        sendResponse(message, product, false);
    }

    public void sendProductsByIds(ConsumerRecord<String, String> message) throws JsonProcessingException {
        List<Product> products;
        try {
            log.info("gonna parse ids");
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
            List<String> ids = objectMapper.readValue(message.value(), listType);
            log.info("ids: {}", ids);

            log.info("gonna read from db");
            products = productService.findProductsByIds(ids);

            log.info("products: {}", products);
        } catch (Exception e) {
            log.error("Unable to get products: {}", e.getMessage(), e);

            sendResponse(message, "Unable to get products: " + e.getMessage(),true);
            return;
        }
        sendResponse(message, products, false);
    }

    public void setProductStatus(ConsumerRecord<String, String> message) throws JsonProcessingException {
        try {
            SetProductStatusDTO productStatusDTO = objectMapper.readValue(message.value(), SetProductStatusDTO.class);

            productService.setProductStatus(productStatusDTO);
        } catch (Exception e) {
            sendResponse(message, "Unable to set product status: " + e.getMessage(),true);
            return;
        }
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
