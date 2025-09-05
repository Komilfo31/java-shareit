package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Component
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/api/items";

    public ItemClient(RestTemplate rest, @Value("${shareit.server.url}") String serverUrl) {
        super(rest);
        this.serverUrl = serverUrl + API_PREFIX;
    }

    private final String serverUrl;

    public ResponseEntity<Object> createItem(ItemDto itemDto, Long ownerId) {
        return post(serverUrl, ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        return patch(serverUrl + "/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        return get(serverUrl + "/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long ownerId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get(serverUrl, ownerId, parameters);
    }

    public ResponseEntity<Object> searchItems(String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get(serverUrl + "/search", null, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, CommentDto commentDto, Long userId) {
        return post(serverUrl + "/" + itemId + "/comment", userId, commentDto);
    }
}