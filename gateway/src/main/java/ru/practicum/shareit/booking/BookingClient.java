package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.client.BaseClient;

@Component
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/api/bookings";

    public BookingClient(RestTemplate rest, @Value("${shareit.server.url}") String serverUrl) {
        super(rest);
        this.serverUrl = serverUrl + API_PREFIX;
    }

    private final String serverUrl;

    public ResponseEntity<Object> createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        return post(serverUrl, userId, bookingRequestDto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, Boolean approved, Long userId) {
        String url = serverUrl + "/" + bookingId + "?approved=" + approved;
        return patch(url, userId, null, null);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId, Long userId) {
        return get(serverUrl + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(Long userId, String state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(serverUrl, userId, parameters);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(serverUrl + "/owner", userId, parameters);
    }
}
