package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.util.Map;

@Component
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/api/requests";

    public ItemRequestClient(RestTemplate rest, @Value("${shareit.server.url}") String serverUrl) {
        super(rest);
        this.serverUrl = serverUrl + API_PREFIX;
    }

    private final String serverUrl;

    public ResponseEntity<Object> createRequest(ItemRequestInputDto requestDto, Long userId) {
        return post(serverUrl, userId, requestDto);
    }

    public ResponseEntity<Object> getUserRequests(Long userId) {
        return get(serverUrl, userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get(serverUrl + "/all", userId, parameters);
    }

    public ResponseEntity<Object> getRequestById(Long requestId, Long userId) {
        return get(serverUrl + "/" + requestId, userId);
    }
}