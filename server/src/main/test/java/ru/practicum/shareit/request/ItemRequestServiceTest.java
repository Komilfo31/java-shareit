package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private ItemRequestService itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestInputDto inputDto;
    private ItemRequestDto requestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@email.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());

        inputDto = new ItemRequestInputDto();
        inputDto.setDescription("Need a drill");

        requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Need a drill");
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(new ArrayList<>());

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(user);
        item.setRequest(itemRequest);
    }

    @Test
    @Transactional
    void shouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestMapper.toRequestEntity(inputDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(requestMapper.toRequestDto(itemRequest)).thenReturn(requestDto);

        ItemRequestDto result = itemRequestService.createRequest(inputDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        verify(userRepository).findById(1L);
        verify(requestMapper).toRequestEntity(inputDto);
        verify(itemRequestRepository).save(itemRequest);
        verify(requestMapper).toRequestDto(itemRequest);
    }

    @Test
    void shouldThrowExceptionWhenCreateRequestWithInvalidUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.createRequest(inputDto, 999L));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void shouldGetUserRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Drill");

        when(itemMapper.toDto(item)).thenReturn(itemDto);

        when(requestMapper.toRequestDtoList(anyList(), anyMap()))
                .thenReturn(List.of(requestDto));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(itemRequestRepository).findByRequesterIdOrderByCreatedDesc(1L);
        verify(itemRepository).findByRequestIdIn(List.of(1L));
    }

    @Test
    void shouldGetAllRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(1L, pageable))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Drill");

        when(itemMapper.toDto(item)).thenReturn(itemDto);

        when(requestMapper.toRequestDtoList(anyList(), anyMap()))
                .thenReturn(List.of(requestDto));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L, pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(itemRequestRepository).findByRequesterIdNotOrderByCreatedDesc(1L, pageable);
    }

    @Test
    void shouldGetRequestById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Drill");

        when(itemMapper.toDto(item)).thenReturn(itemDto);

        when(requestMapper.toRequestDtoWithItems(any(ItemRequest.class), anyList()))
                .thenReturn(requestDto);

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(itemRequestRepository).findById(1L);
        verify(itemRepository).findByRequestIdIn(List.of(1L));
    }

    @Test
    void shouldThrowExceptionWhenGetRequestByIdWithInvalidUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getRequestById(1L, 999L));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenGetRequestByIdWithInvalidRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(999L, 1L));

        assertThat(exception.getMessage()).isEqualTo("Request not found");
    }

    @Test
    void shouldReturnEmptyListWhenNoUserRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(Collections.emptyList());
        when(requestMapper.toRequestDtoList(Collections.emptyList(), Map.of()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).findByRequestIdIn(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoOtherRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(1L, pageable))
                .thenReturn(Collections.emptyList());
        when(requestMapper.toRequestDtoList(Collections.emptyList(), Map.of()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L, pageable);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).findByRequestIdIn(any());
    }

    @Test
    void shouldHandleEmptyItemsInGetItemsForRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(Collections.emptyList());
        when(requestMapper.toRequestDtoList(List.of(itemRequest), Map.of()))
                .thenReturn(List.of(requestDto));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        verify(itemRepository).findByRequestIdIn(List.of(1L));
    }
}
