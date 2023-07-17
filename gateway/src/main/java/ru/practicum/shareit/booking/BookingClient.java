package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import javax.validation.ValidationException;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        if (!requestDto.getStart().isBefore(requestDto.getEnd())) {
            throw new ValidationException("Invalid booking datetime!");
        }
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> confirmBooking(long bookingId, boolean approved, long userId) {
        String path = "/" + bookingId + "?approved={approved}";
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch(path, userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookings(long userId, String state, Integer fromElement, Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", fromElement,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllByOwnerIdAndState(long userId, String state, Integer fromElement, Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", fromElement,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}
