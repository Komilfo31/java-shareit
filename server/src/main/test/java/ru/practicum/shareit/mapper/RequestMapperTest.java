package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final RequestMapper requestMapper = new RequestMapper();

    @Test
    void ShouldMapItemRequestToItemRequestDto() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        ItemRequestDto requestDto = requestMapper.toRequestDto(request);

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(requestDto.getItems()).isNotNull().isEmpty();
    }

    @Test
    void ShouldMapItemRequestToItemRequestDtoWithItems() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        List<ItemDto> items = List.of(itemDto);

        ItemRequestDto requestDto = requestMapper.toRequestDtoWithItems(request, items);

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(requestDto.getItems()).hasSize(1);
        assertThat(requestDto.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(requestDto.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void ShouldHandleNullItems() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        ItemRequestDto requestDto = requestMapper.toRequestDtoWithItems(request, null);

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getItems()).isNotNull().isEmpty();
    }

    @Test
    void ShouldHandleEmptyItems() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        ItemRequestDto requestDto = requestMapper.toRequestDtoWithItems(request, new ArrayList<>());

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getItems()).isNotNull().isEmpty();
    }

    @Test
    void ShouldMapItemRequestInputDtoToItemRequest() {
        ItemRequestInputDto requestDto = new ItemRequestInputDto();
        requestDto.setDescription("Need a drill");

        ItemRequest request = requestMapper.toRequestEntity(requestDto);

        assertThat(request).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Need a drill");
        assertThat(request.getCreated()).isNotNull();
        assertThat(request.getId()).isNull();
    }

    @Test
    void ShouldMapListOfItemRequestsToItemRequestDtos() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Need a drill");
        request1.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Need a hammer");
        request2.setCreated(LocalDateTime.of(2023, 1, 2, 10, 0));

        List<ItemRequest> requests = List.of(request1, request2);

        ItemDto item1 = ItemDto.builder().id(1L).name("Drill").build();
        ItemDto item2 = ItemDto.builder().id(2L).name("Hammer").build();

        Map<Long, List<ItemDto>> itemsByRequestId = Map.of(
                1L, List.of(item1),
                2L, List.of(item2)
        );

        List<ItemRequestDto> result = requestMapper.toRequestDtoList(requests, itemsByRequestId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDescription()).isEqualTo("Need a drill");
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getId()).isEqualTo(1L);

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getDescription()).isEqualTo("Need a hammer");
        assertThat(result.get(1).getItems()).hasSize(1);
        assertThat(result.get(1).getItems().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void ShouldHandleEmptyItemsMap() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));

        List<ItemRequest> requests = List.of(request);
        Map<Long, List<ItemDto>> itemsByRequestId = Map.of();

        List<ItemRequestDto> result = requestMapper.toRequestDtoList(requests, itemsByRequestId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getItems()).isNotNull().isEmpty();
    }

    @Test
    void ShouldHandleEmptyRequestsList() {
        List<ItemRequest> requests = List.of();
        Map<Long, List<ItemDto>> itemsByRequestId = Map.of(1L, List.of());

        List<ItemRequestDto> result = requestMapper.toRequestDtoList(requests, itemsByRequestId);

        assertThat(result).isNotNull().isEmpty();
    }
}