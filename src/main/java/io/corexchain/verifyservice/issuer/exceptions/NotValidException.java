package io.corexchain.verifyservice.issuer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotValidException extends RuntimeException {
    public NotValidException() {
        super("Not valid in blockchain.");
    }
    public NotValidException(String msg) {
        super(msg);
    }
}
