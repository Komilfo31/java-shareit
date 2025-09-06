package ru.practicum.shareit.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private BookingDto bookingDto;
    private Validator validator;

    public BookingDtoTest(@Autowired JacksonTester<BookingDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        BookingDto.BookerDto bookerDto = BookingDto.BookerDto.builder()
                .id(1L)
                .name("Booker Name")
                .build();

        BookingDto.ItemDto itemDto = BookingDto.ItemDto.builder()
                .id(1L)
                .name("Item Name")
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.of(2030, 12, 25, 12, 0))
                .end(LocalDateTime.of(2030, 12, 26, 12, 0))
                .status(BookingStatus.WAITING)
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    @Test
    void testJsonBookingDto() throws Exception {
        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-12-26T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");

        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Booker Name");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Item Name");
    }

    @Test
    void testJsonBookingDtoWithNullFields() throws Exception {
        BookingDto dtoWithNulls = BookingDto.builder()
                .id(1L)
                .itemId(null)
                .start(null)
                .end(null)
                .status(null)
                .booker(null)
                .item(null)
                .lastBooking(null)
                .nextBooking(null)
                .build();

        JsonContent<BookingDto> result = json.write(dtoWithNulls);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.itemId").isNull();
        assertThat(result).extractingJsonPathValue("$.start").isNull();
        assertThat(result).extractingJsonPathValue("$.end").isNull();
        assertThat(result).extractingJsonPathValue("$.status").isNull();
        assertThat(result).extractingJsonPathValue("$.booker").isNull();
        assertThat(result).extractingJsonPathValue("$.item").isNull();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
    }

    @Test
    void whenBookingDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testJsonSerializationWithDifferentStatuses() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED);
        JsonContent<BookingDto> result = json.write(bookingDto);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");

        bookingDto.setStatus(BookingStatus.REJECTED);
        result = json.write(bookingDto);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("REJECTED");

        bookingDto.setStatus(BookingStatus.CANCELED);
        result = json.write(bookingDto);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("CANCELED");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"itemId\": 1,\n" +
                "  \"start\": \"2030-12-25T12:00:00\",\n" +
                "  \"end\": \"2030-12-26T12:00:00\",\n" +
                "  \"status\": \"WAITING\",\n" +
                "  \"booker\": {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Booker Name\"\n" +
                "  },\n" +
                "  \"item\": {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Item Name\"\n" +
                "  }\n" +
                "}";

        BookingDto parsedDto = json.parse(jsonContent).getObject();

        assertThat(parsedDto.getId()).isEqualTo(1L);
        assertThat(parsedDto.getItemId()).isEqualTo(1L);
        assertThat(parsedDto.getStart()).isEqualTo(LocalDateTime.of(2030, 12, 25, 12, 0));
        assertThat(parsedDto.getEnd()).isEqualTo(LocalDateTime.of(2030, 12, 26, 12, 0));
        assertThat(parsedDto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(parsedDto.getBooker().getId()).isEqualTo(1L);
        assertThat(parsedDto.getBooker().getName()).isEqualTo("Booker Name");
        assertThat(parsedDto.getItem().getId()).isEqualTo(1L);
        assertThat(parsedDto.getItem().getName()).isEqualTo("Item Name");
    }

    @Test
    void testJsonSerializationWithLastAndNextBooking() throws Exception {
        Booking lastBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.of(2030, 12, 20, 12, 0))
                .end(LocalDateTime.of(2030, 12, 21, 12, 0))
                .status(BookingStatus.APPROVED)
                .build();

        Booking nextBooking = Booking.builder()
                .id(3L)
                .start(LocalDateTime.of(2030, 12, 27, 12, 0))
                .end(LocalDateTime.of(2030, 12, 28, 12, 0))
                .status(BookingStatus.WAITING)
                .build();

        bookingDto.setLastBooking(lastBooking);
        bookingDto.setNextBooking(nextBooking);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(3);
    }
}
