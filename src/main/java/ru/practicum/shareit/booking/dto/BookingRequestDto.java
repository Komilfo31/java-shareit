package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDto {
    @NotNull(message = "id Товара не может быть нулевым")
    private Long itemId;

    @NotNull(message = "Дата начала не может быть нулевой")
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull(message = "Дата окончания не может быть нулевой")
    @Future
    private LocalDateTime end;
}
