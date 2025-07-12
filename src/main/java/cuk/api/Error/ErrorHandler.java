package cuk.api.Error;

import cuk.api.ResponseEntities.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ResponseMessage> handleAllException(Exception e) {
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.BAD_REQUEST);
        resp.setMessage(e.getMessage());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String fallback() {
        return "forward:/fe/index.html";
    }
}
