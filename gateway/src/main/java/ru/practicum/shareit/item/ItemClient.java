package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> addNew(ItemRequestDto itemRequestDto, Long userId) {
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> patchUpdate(ItemRequestDto itemRequestDto, Long itemId, Long userId) {
        String path = "/" + itemId;
        return patch(path, userId, itemRequestDto);
    }

    public ResponseEntity<Object> getById(Long itemId, Long userId) {
        String path = "/" + itemId;
        return get(path, userId);
    }

    public ResponseEntity<Object> getAllOwnerItems(Long userId, Integer fromElement, Integer size) {
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }

        String path = "?from={from}&size={size}";
        Map<String, Object> parameters = Map.of(
                "from", fromElement,
                "size", size
        );
        return get(path, userId, parameters);
    }

    public ResponseEntity<Object> findAvailableByText(String text, Integer fromElement, Integer size) {
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        if (text.isBlank()) {
            return ResponseEntity.status(200).body(Collections.EMPTY_LIST);
        }

        String path = "/search?from={from}&size={size}&text={text}";
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", fromElement,
                "size", size
        );
        return get(path, null, parameters);
    }

    public ResponseEntity<Object> addNewComment(CommentRequestDto commentRequestDto, Long itemId, Long userId) {
        String path = "/" + itemId + "/comment";
        return post(path, userId, commentRequestDto);
    }
}
