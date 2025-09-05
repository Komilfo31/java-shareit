package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex")
                .email("alex@alex.ru")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Alex")
                .email("alex@alex.ru")
                .build();
    }

    @Test
    void shouldCreateUser() {
        when(userRepository.existsByEmail("alex@alex.ru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alex");
        assertThat(result.getEmail()).isEqualTo("alex@alex.ru");
        verify(userRepository).existsByEmail("alex@alex.ru");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenCreateUserWithExistingEmail() {
        when(userRepository.existsByEmail("alex@alex.ru")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.create(userDto));

        assertThat(exception.getMessage()).isEqualTo("Пользователь с таким email уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUser() {
        UserDto updateDto = UserDto.builder()
                .name("Alex Updated")
                .email("alex.updated@alex.ru")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("alex.updated@alex.ru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        UserDto result = userService.update(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Alex Updated");
        assertThat(result.getEmail()).isEqualTo("alex.updated@alex.ru");
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("alex.updated@alex.ru");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateUserWithWrongId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.update(999L, userDto));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден по id:999");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdateUserWithExistingEmail() {
        UserDto newUserDto = UserDto.builder()
                .email("existing@email.ru")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@email.ru")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.update(1L, newUserDto));

        assertThat(exception.getMessage()).isEqualTo("Пользователь с таким email уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alex");
        assertThat(result.getEmail()).isEqualTo("alex@alex.ru");
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenGetUserWithWrongId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.getById(999L));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден по id: 999");
    }

    @Test
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Alex");
        assertThat(result.get(0).getEmail()).isEqualTo("alex@alex.ru");
        verify(userRepository).findAll();
    }

    @Test
    void shouldDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailIsNull() {
        UserDto invalidUserDto = UserDto.builder()
                .email(null)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(invalidUserDto));

        assertThat(exception.getMessage()).isEqualTo("Email не может быть пустым");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailIsBlank() {
        UserDto invalidUserDto = UserDto.builder()
                .email("   ")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(invalidUserDto));

        assertThat(exception.getMessage()).isEqualTo("Email не может быть пустым");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailWithoutAt() {
        UserDto invalidUserDto = UserDto.builder()
                .email("invalid-email")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(invalidUserDto));

        assertThat(exception.getMessage()).isEqualTo("Email должен содержать символ @");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsNull() {
        User existingUser = User.builder()
                .id(1L)
                .name("Old Name")
                .email("old@email.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .name("New Name")
                .email(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        UserDto result = userService.update(1L, updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("old@email.ru");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void shouldNotUpdateEmailWhenSameEmailProvided() {
        User existingUser = User.builder()
                .id(1L)
                .name("Alex")
                .email("alex@alex.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .name("Alex Updated")
                .email("alex@alex.ru")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        UserDto result = userService.update(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Alex Updated");
        assertThat(result.getEmail()).isEqualTo("alex@alex.ru");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAll();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }
}