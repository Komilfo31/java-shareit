package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemApi.class)
public class ItemApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long OWNER_ID = 2L;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private Comment comment;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .comments(Collections.emptyList())
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .build();

        comment = new Comment();
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/api/items")
                        .header(USER_ID_HEADER, OWNER_ID)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService).create(eq(OWNER_ID), any(ItemDto.class));
    }

    @Test
    void createItem_shouldReturnOkEvenWithInvalidData() throws Exception {
        ItemDto invalidItemDto = ItemDto.builder()
                .available(true)
                .build();

        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(invalidItemDto);

        mvc.perform(post("/api/items")
                        .header(USER_ID_HEADER, OWNER_ID)
                        .content(mapper.writeValueAsString(invalidItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(patch("/api/items/{itemId}", ITEM_ID)
                        .header(USER_ID_HEADER, OWNER_ID)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));

        verify(itemService).update(eq(ITEM_ID), eq(OWNER_ID), any(ItemDto.class));
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemDto);

        mvc.perform(get("/api/items/{itemId}", ITEM_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));

        verify(itemService).getById(eq(ITEM_ID), eq(USER_ID));
    }

    @Test
    void getAllByOwner_shouldReturnItemsWithPagination() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.getAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);

        int from = 0;
        int size = 10;

        mvc.perform(get("/api/items")
                        .header(USER_ID_HEADER, OWNER_ID)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));

        verify(itemService).getAllByOwnerId(eq(OWNER_ID), any(Pageable.class));
    }

    @Test
    void getAllByOwner_shouldUseDefaultPagination() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.getAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);

        mvc.perform(get("/api/items")
                        .header(USER_ID_HEADER, OWNER_ID))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemService).getAllByOwnerId(eq(OWNER_ID), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void searchItems_shouldReturnSearchResults() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.search(anyString(), any(Pageable.class))).thenReturn(items);

        String searchText = "test";
        int from = 0;
        int size = 5;

        mvc.perform(get("/api/items/search")
                        .param("text", searchText)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));

        verify(itemService).search(eq(searchText), any(Pageable.class));
    }

    @Test
    void searchItems_shouldReturnEmptyListForEmptyText() throws Exception {
        when(itemService.search(eq(""), any(Pageable.class))).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).search(eq(""), any(Pageable.class));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        when(itemMapper.toComment(any(CommentDto.class))).thenReturn(comment);
        when(itemService.addComment(anyLong(), any(Comment.class), anyLong())).thenReturn(comment);
        when(itemMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        mvc.perform(post("/api/items/{itemId}/comment", ITEM_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())));

        verify(itemMapper).toComment(any(CommentDto.class));
        verify(itemService).addComment(eq(ITEM_ID), any(Comment.class), eq(USER_ID));
        verify(itemMapper).toCommentDto(any(Comment.class));
    }

    @Test
    void addComment_shouldReturnOkEvenWithInvalidComment() throws Exception {
        CommentDto invalidCommentDto = CommentDto.builder().build();

        when(itemMapper.toComment(any(CommentDto.class))).thenReturn(comment);
        when(itemService.addComment(anyLong(), any(Comment.class), anyLong())).thenReturn(comment);
        when(itemMapper.toCommentDto(any(Comment.class))).thenReturn(invalidCommentDto);

        mvc.perform(post("/api/items/{itemId}/comment", ITEM_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(mapper.writeValueAsString(invalidCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_shouldHandleInvalidPagination() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.getAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);

        mvc.perform(get("/api/items")
                        .header(USER_ID_HEADER, OWNER_ID)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}