package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemWithIdResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class ItemRequestWithItemsDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemWithIdResponseDto> items;
}
