package ru.practicum.shareit.item.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class CommentRequestDto {
    @NotBlank
    @Length(max = 2000)
    private String text;
}
