package ru.practicum.shareit.exception.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExceptionResponse {
    @JsonProperty("error")
    private final String exceptionMessage;
}