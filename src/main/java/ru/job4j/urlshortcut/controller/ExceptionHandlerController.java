package ru.job4j.urlshortcut.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.job4j.urlshortcut.model.ErrorDetail;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/** Global exception handler controller. */
@ControllerAdvice
public class ExceptionHandlerController {

    /**
     * Global exception handler to process the Bean Validation exceptions.
     *
     * @param exception to handle
     * @param request original HTTP servlet request
     * @return HTTP REST response with error information
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> validationExceptionHandler(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String msg = exception.getFieldErrors().stream()
                .map(f -> String.format("%s : %s : actual value: %s",
                        f.getField(), f.getDefaultMessage(), f.getRejectedValue())
                ).collect(Collectors.joining("; "));
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorDetail detail = new ErrorDetail(httpStatus, request.getRequestURI(), msg);
        return new ResponseEntity<>(detail, httpStatus);
    }
}
