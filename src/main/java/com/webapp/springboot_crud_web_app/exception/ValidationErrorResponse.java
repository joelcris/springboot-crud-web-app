package com.webapp.springboot_crud_web_app.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for validation errors with field-specific details.
 */
@Getter
@Setter
@NoArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
    private List<FieldError> errors = new ArrayList<>();
    
    public ValidationErrorResponse(int status, String message, String path, 
                                   LocalDateTime timestamp, Map<String, String> fieldErrors) {
        super(status, message, path, timestamp);
        if (fieldErrors != null) {
            fieldErrors.forEach((field, errorMessage) -> 
                this.errors.add(new FieldError(field, errorMessage)));
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
} 