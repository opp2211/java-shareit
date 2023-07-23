package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public abstract class CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    public abstract CommentResponseDto toCommentResponseDto(Comment comment);

//    @Mapping(target = "authorName", source = "author.name")
//    public abstract List<CommentResponseDto> toCommentResponseDtos(List<Comment> comments);

    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    public abstract Comment toComment(CommentRequestDto comment);
}
