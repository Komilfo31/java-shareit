package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestApi.class)
public class ItemRequestApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;
    private static final Long REQUEST_ID = 1L;

    private ItemRequestDto itemRequestDto;
    private ItemRequestInputDto itemRequestInputDto;

    @BeforeEach
    void setUp() {
        itemRequestInputDto = ItemRequestInputDto.builder()
                .description("Need a drill")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(REQUEST_ID)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequestInputDto.class), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(post("/api/requests")
                        .header(USER_ID_HEADER, USER_ID)
                        .content(mapper.writeValueAsString(itemRequestInputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));

        verify(itemRequestService).createRequest(any(ItemRequestInputDto.class), eq(USER_ID));
    }

    @Test
    void shouldReturnRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getUserRequests(anyLong())).thenReturn(requests);

        mvc.perform(get("/api/requests")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));

        verify(itemRequestService).getUserRequests(eq(USER_ID));
    }

    @Test
    void shouldReturnRequestsWithPagination() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getAllRequests(anyLong(), any(Pageable.class))).thenReturn(requests);

        mvc.perform(get("/api/requests/all")
                        .header(USER_ID_HEADER, USER_ID)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));

        verify(itemRequestService).getAllRequests(eq(USER_ID), any(Pageable.class));
    }

    @Test
    void shouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(get("/api/requests/{requestId}", REQUEST_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class));

        verify(itemRequestService).getRequestById(eq(REQUEST_ID), eq(USER_ID));
    }
}
