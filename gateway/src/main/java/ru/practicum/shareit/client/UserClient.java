package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/api/users";

    public UserClient(RestTemplate rest, @Value("${shareit.server.url}") String serverUrl) {
        super(rest);
        this.serverUrl = serverUrl + API_PREFIX;
    }

    private final String serverUrl;

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post(serverUrl, userDto);
    }

    public ResponseEntity<Object> updateUser(Long id, UserDto userDto) {
        return patch(serverUrl + "/" + id, userDto);
    }

    public ResponseEntity<Object> getUserById(Long id) {
        return get(serverUrl + "/" + id);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(serverUrl);
    }

    public ResponseEntity<Object> deleteUser(Long id) {
        return delete(serverUrl + "/" + id);
    }
}
