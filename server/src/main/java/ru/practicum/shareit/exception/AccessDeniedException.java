package ru.practicum.shareit.exception;

public class AccessDeniedException extends RuntimeException {
//    public AccessDeniedException() {
//    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
