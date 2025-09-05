package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService{
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final RequestMapper requestMapper;

    @Transactional
    public ItemRequestDto createRequest(ItemRequestInputDto requestDto, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        ItemRequest request = requestMapper.toRequestEntity(requestDto);
        request.setRequester(requester);

        ItemRequest savedRequest = itemRequestRepository.save(request);
        return requestMapper.toRequestDto(savedRequest);
    }

    public List<ItemRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        List<Long> requestIds = getRequestIds(requests);

        Map<Long, List<ItemDto>> itemsByRequestId = getItemsForRequests(requestIds);

        return requestMapper.toRequestDtoList(requests, itemsByRequestId);
    }

    public List<ItemRequestDto> getAllRequests(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId, pageable);
        List<Long> requestIds = getRequestIds(requests);

        Map<Long, List<ItemDto>> itemsByRequestId = getItemsForRequests(requestIds);

        return requestMapper.toRequestDtoList(requests, itemsByRequestId);
    }

    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<ItemDto> items = itemRepository.findByRequestIdIn(List.of(requestId))
                .stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());

        return requestMapper.toRequestDtoWithItems(request, items);
    }

    private List<Long> getRequestIds(List<ItemRequest> requests) {
        return requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
    }

    private Map<Long, List<ItemDto>> getItemsForRequests(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return Map.of();
        }

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(itemMapper::toDto, Collectors.toList())
                ));
    }
}
