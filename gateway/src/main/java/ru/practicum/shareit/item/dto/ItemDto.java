package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Наименование не может быть пустым")
    @JsonProperty("name")
    private String name;
    @NotBlank(message = "Описание не может быть пустым")
    @JsonProperty("description")
    private String description;
    @NotNull(message = "Available cannot be null")
    @JsonProperty("available")
    private Boolean available;
    @JsonProperty("requestId")
    private Long requestId;
    @JsonProperty("lastBooking")
    private BookingDto lastBooking;
    @JsonProperty("nextBooking")
    private BookingDto nextBooking;
    @JsonProperty("comments")
    private List<CommentDto> comments;
}
