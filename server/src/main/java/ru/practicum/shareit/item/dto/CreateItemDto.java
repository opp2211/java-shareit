package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Data
public class CreateItemDto {
    @NotBlank
    @Length(max = 255)
    private String name;

    @NotBlank
    @Length(max = 2000)
    private String description;

    @NotNull
    private Boolean available;

    private Long requestId;
}
