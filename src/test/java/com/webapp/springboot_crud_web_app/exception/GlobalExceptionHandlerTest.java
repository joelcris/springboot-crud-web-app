package com.webapp.springboot_crud_web_app.exception;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webRequest.getDescription(false)).thenReturn("test/uri");
    }

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFoundResponse() {
        // Arrange
        String errorMessage = "Product not found with id: '1'";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("test/uri", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleBusinessRuleViolationException_ShouldReturnBadRequestResponse() {
        // Arrange
        String errorMessage = "Cannot place order: insufficient stock";
        BusinessRuleViolationException ex = new BusinessRuleViolationException(errorMessage);

        // Act
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleBusinessRuleViolationException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("test/uri", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleValidationExceptions_ShouldReturnValidationErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<org.springframework.validation.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("orderDTO", "customerName", "Customer name is required"));
        fieldErrors.add(new FieldError("orderDTO", "customerEmail", "Email must be valid"));
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // Act
        ResponseEntity<ValidationErrorResponse> responseEntity = exceptionHandler.handleValidationExceptions(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ValidationErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Validation failed", errorResponse.getMessage());
        assertEquals("test/uri", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(2, errorResponse.getErrors().size());
        
        // Verify field errors
        List<ValidationErrorResponse.FieldError> responseErrors = errorResponse.getErrors();
        assertEquals("customerName", responseErrors.get(0).getField());
        assertEquals("Customer name is required", responseErrors.get(0).getMessage());
        assertEquals("customerEmail", responseErrors.get(1).getField());
        assertEquals("Email must be valid", responseErrors.get(1).getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturnBadRequestResponse() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid JSON format");

        // Act
        ResponseEntity<ValidationErrorResponse> responseEntity = exceptionHandler.handleHttpMessageNotReadableException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ValidationErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Invalid request format. Please ensure the request body is valid JSON and data types are correct.", 
                    errorResponse.getMessage());
        assertEquals("test/uri", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
        // Expect empty errors map
        assertEquals(0, errorResponse.getErrors().size());
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerErrorResponse() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertEquals("test/uri", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }
} 