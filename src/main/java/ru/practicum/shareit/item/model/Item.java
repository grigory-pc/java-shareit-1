package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Объект вещи
 */
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Item {
    private long id;
    private String name;
    private String description;
    private String available;
}
