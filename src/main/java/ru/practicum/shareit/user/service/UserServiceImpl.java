package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        validateUserEmail(userDto.getEmail());

        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existUser = userStorage.findById(id)
                .orElseThrow(() -> new ValidationException("Пользователь не найден по id:" + id));

        if (userDto.getName() != null) {
            existUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            validateUserEmail(userDto.getEmail());

            if (!userDto.getEmail().equals(existUser.getEmail())) {
                if (userStorage.existsByEmail(userDto.getEmail())) {
                    throw new ConflictException("Пользователь с таким email уже существует");
                }
                existUser.setEmail(userDto.getEmail());
            }
        }

        return UserMapper.toUserDto(userStorage.update(existUser));
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userStorage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден по id: " + id)));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userStorage.deleteById(id);
    }

    private void validateUserEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!email.contains("@")) {
            throw new ValidationException("Email должен содержать символ @");
        }
    }
}
