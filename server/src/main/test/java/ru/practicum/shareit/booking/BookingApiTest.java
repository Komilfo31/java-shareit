package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

@WebMvcTest(controllers = BookingApi.class)
public class BookingApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingMapper bookingMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long ITEM_ID = 1L;

    private BookingDto bookingDto;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingRequestDto = BookingRequestDto.builder()
                .itemId(ITEM_ID)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingDto = BookingDto.builder()
                .id(BOOKING_ID)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();

        booking = Booking.builder()
                .id(BOOKING_ID)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(any(Booking.class), anyLong(), anyLong())).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        mvc.perform(post("/api/bookings")
                        .header(USER_ID_HEADER, USER_ID)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        verify(bookingService).create(any(Booking.class), eq(ITEM_ID), eq(USER_ID));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        BookingDto approvedDto = BookingDto.builder()
                .id(BOOKING_ID)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(approvedDto);

        mvc.perform(patch("/api/bookings/{bookingId}", BOOKING_ID)
                        .header(USER_ID_HEADER, USER_ID)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));

        verify(bookingService).approve(eq(BOOKING_ID), eq(USER_ID), eq(true));
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        mvc.perform(get("/api/bookings/{bookingId}", BOOKING_ID)
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));

        verify(bookingService).getById(eq(BOOKING_ID), eq(USER_ID));
    }

    @Test
    void getAllByBooker_shouldReturnBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        when(bookingService.getAllByBooker(anyLong(), anyString(), any(Pageable.class))).thenReturn(bookings);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        mvc.perform(get("/api/bookings")
                        .header(USER_ID_HEADER, USER_ID)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));

        verify(bookingService).getAllByBooker(eq(USER_ID), eq("ALL"), any(Pageable.class));
    }

    @Test
    void getAllByOwner_shouldReturnBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        when(bookingService.getAllByOwner(anyLong(), anyString(), any(Pageable.class))).thenReturn(bookings);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        mvc.perform(get("/api/bookings/owner")
                        .header(USER_ID_HEADER, USER_ID)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));

        verify(bookingService).getAllByOwner(eq(USER_ID), eq("ALL"), any(Pageable.class));
    }
}