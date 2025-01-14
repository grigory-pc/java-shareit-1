package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

/**
 * Интерфейс для бронирования вещей
 */
public interface BookingService {
    BookingOutDto getBookingById(long userId, long bookingId);

    List<BookingOutDto> getBookingsByBookerId(long userId, State state, int from, int size);

    List<BookingOutDto> getBookingsByOwnerId(long userId, State state, int from, int size);

    BookingInDto addNewBooking(long userId, BookingInDto bookingInDto);

    BookingOutDto updateBookingStatus(long userId, long bookingId, boolean approved);
}