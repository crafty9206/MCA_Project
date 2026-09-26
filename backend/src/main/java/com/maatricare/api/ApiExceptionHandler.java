package com.maatricare.api;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(NoSuchElementException exception) {
        return new ErrorResponse(exception.getMessage() == null ? "Resource not found" : exception.getMessage());
    }

    public record ErrorResponse(String message) {}
}