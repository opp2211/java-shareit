package ru.practicum.shareit.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class CommonHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFound(NotFoundException e) {
        log.debug(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ExceptionResponse handleAlreadyExist(AlreadyExistException e) {
//        log.debug(e.getMessage(), e);
//        return new ExceptionResponse(e.getMessage());
//    }

    @ExceptionHandler({ValidationException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleValidation(Exception e) {
        log.debug(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleAccessDenied(AccessDeniedException e) {
        log.debug(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception e) {
        log.debug(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }
}