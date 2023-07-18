package ru.practicum.shareit.request.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class ItemRequestCreateDto {
    @NotBlank
    @Length(max = 2000)
    private String description;
}
