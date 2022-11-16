package io.corexchain.verifyservice.issuer.controller;

import java.util.HashMap;
import java.util.Map;

import io.corexchain.verify4j.exceptions.AlreadyExistsException;
import io.corexchain.verify4j.exceptions.BlockchainNodeException;
import io.corexchain.verify4j.exceptions.InvalidCreditAmountException;
import io.corexchain.verify4j.exceptions.NotFoundException;
import io.corexchain.verifyservice.issuer.exceptions.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ValidationHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {

            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return new ResponseEntity<Object>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BadRequestException.class,
            NotFoundException.class,
            AlreadyExistsException.class,
            InvalidCreditAmountException.class,
            BlockchainNodeException.class})
    public ResponseEntity<Object> handleError(HttpServletRequest request, RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return new ResponseEntity<Object>(errors, HttpStatus.BAD_REQUEST);
    }

}