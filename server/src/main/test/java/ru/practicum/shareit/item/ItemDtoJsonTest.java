package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serialize_shouldHandleNullComments() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .comments(null)
                .build();

        JsonContent<ItemDto> result = json.write(item);

        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).extractingJsonPathValue("$.comments").isNull();
    }

    @Test
    void deserialize_shouldIgnoreUnknownFields() throws Exception {
        String content = """
        {
            "id": 1,
            "name": "Item",
            "description": "Description",
            "available": true,
            "unknownField": "value"
        }
        """;

        ItemDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Item");
    }
}
