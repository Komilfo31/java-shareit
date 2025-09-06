package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    @Null
    private Long id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    @JsonProperty("text")
    private String text;
    @JsonProperty("authorName")
    private String authorName;
    @JsonProperty("created")
    private LocalDateTime created;
}