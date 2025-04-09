package com.mary.product_microservice_sharik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.product_microservice_sharik.exception.NoDataFoundException;
import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestProcessingServiceTest {

    @InjectMocks
    private RequestProcessingService requestProcessingService;

    @Mock
    private ProductService productService;

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    private ConsumerRecord<String, String> consumerRecord;
    private final String replyTopic = "reply-topic";
    private final String correlationId = "correlation-123";
    private TypeFactory typeFactory;

    @BeforeEach
    void setUp() {
        typeFactory = mock(TypeFactory.class);
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
        headers.add(new RecordHeader(KafkaHeaders.CORRELATION_ID, correlationId.getBytes()));
        
        consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", "value");

        try {
            java.lang.reflect.Field headersField = ConsumerRecord.class.getDeclaredField("headers");
            headersField.setAccessible(true);
            headersField.set(consumerRecord, headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set headers", e);
        }
    }

    @Test
    void createProduct_success() throws JsonProcessingException {
        // Arrange
        AddProductDTO dto = new AddProductDTO();
        when(objectMapper.readValue(eq("value"), eq(AddProductDTO.class))).thenReturn(dto);
        when(objectMapper.writeValueAsString(eq(true))).thenReturn("true");
        
        // Act
        requestProcessingService.createProduct(consumerRecord);
        
        // Assert
        verify(productService).create(dto);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("true", sentMessage.getPayload());
        assertEquals(replyTopic,
                new String((byte[]) Objects.requireNonNull(sentMessage.getHeaders().get(KafkaHeaders.TOPIC))));
        assertEquals(correlationId,
                new String((byte[]) Objects.requireNonNull(sentMessage.getHeaders().get(KafkaHeaders.CORRELATION_ID))));
        assertNull(sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void createProduct_exception() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(eq("value"), eq(AddProductDTO.class)))
                .thenThrow(new RuntimeException("Test exception"));
        when(objectMapper.writeValueAsString(contains("Unable to get products")))
                .thenReturn("\"Unable to get products: Test exception\"");
        
        // Act
        requestProcessingService.createProduct(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("\"Unable to get products: Test exception\"", sentMessage.getPayload());
        assertEquals("\"Unable to get products: Test exception\"", sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void sendProductsByFilter_success() throws JsonProcessingException {
        // Arrange
        ProductSearchFilterDTO filter = new ProductSearchFilterDTO();
        List<Product> products = List.of(new Product());
        
        when(objectMapper.readValue(eq("value"), eq(ProductSearchFilterDTO.class))).thenReturn(filter);
        when(productService.findProductsByFilterOnPage(filter)).thenReturn(products);
        when(objectMapper.writeValueAsString(products)).thenReturn("[{\"product\":\"data\"}]");
        
        // Act
        requestProcessingService.sendProductsByFilter(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("[{\"product\":\"data\"}]", sentMessage.getPayload());
        assertNull(sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }
    
    @Test
    void sendProductsByFilter_nullFilter_usesDefaultFilter() throws JsonProcessingException {
        // Arrange
        List<Product> products = List.of(new Product());
        
        when(objectMapper.readValue(eq("value"), eq(ProductSearchFilterDTO.class))).thenReturn(null);
        when(productService.findProductsByFilterOnPage(any(ProductSearchFilterDTO.class))).thenReturn(products);
        when(objectMapper.writeValueAsString(products)).thenReturn("[{\"product\":\"data\"}]");
        
        // Act
        requestProcessingService.sendProductsByFilter(consumerRecord);
        
        // Assert
        verify(productService).findProductsByFilterOnPage(any(ProductSearchFilterDTO.class));
        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void sendProductsByFilter_exception() throws JsonProcessingException {
        // Arrange
        String errorMessage = "Filter error";
        when(objectMapper.readValue(eq("value"), eq(ProductSearchFilterDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));
        when(objectMapper.writeValueAsString(contains("Unable to get products")))
                .thenReturn("\"Unable to get products: " + errorMessage + "\"");
        
        // Act
        requestProcessingService.sendProductsByFilter(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getPayload().contains(errorMessage));
        assertEquals(sentMessage.getPayload(), sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void sendProductById_success() throws JsonProcessingException {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        
        when(objectMapper.readValue(eq("value"), eq(String.class))).thenReturn("product-id");
        when(productService.findById("product-id")).thenReturn(product);
        when(objectMapper.writeValueAsString(product)).thenReturn("{\"name\":\"Test Product\"}");
        
        // Act
        requestProcessingService.sendProductById(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("{\"name\":\"Test Product\"}", sentMessage.getPayload());
        assertNull(sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void sendProductById_productNotFound() throws JsonProcessingException {
        // Arrange
        String errorMessage = "Product not found";
        when(objectMapper.readValue(eq("value"), eq(String.class))).thenReturn("invalid-id");
        when(productService.findById("invalid-id")).thenThrow(new NoDataFoundException(errorMessage));
        when(objectMapper.writeValueAsString(contains("Unable to get product")))
                .thenReturn("\"Unable to get product: " + errorMessage + "\"");
        
        // Act
        requestProcessingService.sendProductById(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getPayload().contains(errorMessage));
        assertEquals(sentMessage.getPayload(), sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void sendProductsByIds_success() throws JsonProcessingException {
        // Arrange
        List<String> ids = List.of("id1", "id2");
        List<Product> products = List.of(new Product(), new Product());
        CollectionType listType = mock(CollectionType.class);

        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(listType);

        when(objectMapper.readValue(eq("value"), eq(listType))).thenReturn(ids);
        when(productService.findProductsByIds(ids)).thenReturn(products);
        when(objectMapper.writeValueAsString(products)).thenReturn("[{\"product1\":\"data\"},{\"product2\":\"data\"}]");
        
        // Act
        requestProcessingService.sendProductsByIds(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());

        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("[{\"product1\":\"data\"},{\"product2\":\"data\"}]", sentMessage.getPayload());
        assertNull(sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void sendProductsByIds_exception() throws JsonProcessingException {
        // Arrange
        String errorMessage = "JSON parsing error";
        CollectionType listType = mock(CollectionType.class);

        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(listType);

        when(objectMapper.readValue(eq("value"), eq(listType))).thenThrow(new JsonProcessingException(errorMessage) {});
        when(objectMapper.writeValueAsString(contains("Unable to get products")))
                .thenReturn("\"Unable to get products: " + errorMessage + "\"");

        // Act
        requestProcessingService.sendProductsByIds(consumerRecord);

        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());

        Message<String> sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getPayload().contains(errorMessage));
        assertEquals(sentMessage.getPayload(), sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void setProductStatus_success() throws JsonProcessingException {
        // Arrange
        SetProductStatusDTO statusDTO = new SetProductStatusDTO();
        statusDTO.setProductId("123");
        statusDTO.setStatus(true);
        
        when(objectMapper.readValue(eq("value"), eq(SetProductStatusDTO.class))).thenReturn(statusDTO);
        when(objectMapper.writeValueAsString(eq(true))).thenReturn("true");
        
        // Act
        requestProcessingService.setProductStatus(consumerRecord);
        
        // Assert
        verify(productService).setProductStatus(statusDTO);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertEquals("true", sentMessage.getPayload());
        assertNull(sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }

    @Test
    void setProductStatus_exception() throws JsonProcessingException {
        // Arrange
        String errorMessage = "Product status update failed";
        when(objectMapper.readValue(eq("value"), eq(SetProductStatusDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));
        when(objectMapper.writeValueAsString(contains("Unable to set product status")))
                .thenReturn("\"Unable to set product status: " + errorMessage + "\"");
        
        // Act
        requestProcessingService.setProductStatus(consumerRecord);
        
        // Assert
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<String> sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getPayload().contains(errorMessage));
        assertEquals(sentMessage.getPayload(), sentMessage.getHeaders().get(KafkaHeaders.EXCEPTION_MESSAGE));
    }
}