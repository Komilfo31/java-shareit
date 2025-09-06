package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;


class UserMapperTest {

    @Test
    void shouldMapUserToUserDto() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto userDto = UserMapper.toUserDto(user);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldMapUserDtoToUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User user = UserMapper.toUser(userDto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldHandleNullValues() {
        User user = new User();
        UserDto userDto = UserMapper.toUserDto(user);

        assertThat(userDto.getId()).isNull();
        assertThat(userDto.getName()).isNull();
        assertThat(userDto.getEmail()).isNull();
    }
}