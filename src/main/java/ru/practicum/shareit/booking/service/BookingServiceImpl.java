package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Validation;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.StateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс, ответственный за операции с бронированием
 */
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    @Autowired
    private Validation validation;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserService userService, ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    /**
     * Возвращает список всех бронирований
     */
    @Override
    public List<BookingOutDto> getBookings(long userId) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(user -> user.getId() == userId)
                .collect(Collectors.toList());
        return bookingMapper.toOutDto(bookings);
    }

    /**
     * Возвращает бронь по ID
     */
    @Override
    public BookingOutDto getBookingById(long userId, long bookingId) {
        validation.validationId(userId);
        validation.validationId(bookingId);

        if (bookingRepository.findById(bookingId) == null) {
            throw new NotFoundException("бронь не найдена");
        }
        Booking existBooking = bookingRepository.findById(bookingId);

        if ((existBooking.getUser().getId() == userId) || (existBooking.getItem().getUser().getId() == userId)) {
            return bookingMapper.toOutDto(existBooking);
        } else {
            throw new NotFoundException("бронь не найдена");
        }
    }

    /**
     * Возвращает все брони пользователя по его userId
     */
    @Override
    public List<BookingOutDto> getBookingsByBookerId(long userId, State state) {
        validation.validationId(userId);
        userService.getUserById(userId);

        return getBookingsForBookerFilteredByState(userId, state);
    }

    /**
     * Возвращает все брони владельца вещей по его userId
     */
    @Override
    public List<BookingOutDto> getBookingsByOwnerId(long userId, State state) {
        validation.validationId(userId);
        userService.getUserById(userId);

        return getBookingsForOwnerFilteredByState(userId, state);
    }

    /**
     * Добавляет бронь
     */
    @Override
    public BookingInDto addNewBooking(long userId, BookingInDto bookingInDto) {
        validation.validationId(userId);
        validation.validationId(bookingInDto.getItemId());
        validateDateTimeOfBooking(bookingInDto);

        if (itemService.getItemDtoById(userId, bookingInDto.getItemId()).getAvailable().equals("false")) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        Booking bookingForSave = bookingMapper.toBooking(bookingInDto);

        bookingForSave.setUser(getExistUser(userId));
        bookingForSave.setItem(getExistItem(bookingInDto.getItemId()));

        if (bookingForSave.getItem().getUser().getId() == userId) {
            throw new NotFoundException("Владелец вещи не может забронировать собственную вещь");
        }

        bookingForSave.setStatus(Status.WAITING);

        return bookingMapper.toDto(bookingRepository.save(bookingForSave));
    }

    /**
     * Обновляет статус брони
     */
    @Override
    public BookingOutDto updateBookingStatus(long userId, long bookingId, boolean approved) {
        validation.validationId(userId);
        validation.validationId(bookingId);

        Booking bookingForUpdate = bookingRepository.findById(bookingId);

        if (bookingForUpdate.getStatus().equals(Status.APPROVED) && approved) {
            throw new ValidationException("бронь уже подтверждена");
        }

        if (bookingForUpdate.getItem().getUser().getId() == userId) {
            if (approved) {
                bookingForUpdate.setStatus(Status.APPROVED);
            } else {
                bookingForUpdate.setStatus(Status.REJECTED);
            }
        } else {
            throw new NotFoundException("Вы не являетесь владельцем вещи");
        }

        Booking updatedBooking = bookingRepository.save(bookingForUpdate);

        return bookingMapper.toOutDto(updatedBooking);
    }

    /**
     * Удаляет бронь
     */
    @Override
    public void deleteBooking(long userId, long bookingId) {
        validation.validationId(userId);
        validation.validationId(bookingId);

        bookingRepository.deleteById(bookingId);
    }

    private User getExistUser(long userId) {
        return userMapper.toUser(userService.getUserById(userId));
    }

    private Item getExistItem(long itemId) {
        return itemService.getItemById(itemId);
    }

    List<BookingOutDto> getBookingsForBookerFilteredByState(long userId, State state) {
        switch (state) {
            case ALL:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserId_OrderByStartDesc(userId));
            case CURRENT:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserIdAndStartIsBeforeAndEndIsAfterOrderByStart(
                        userId, LocalDateTime.now(), LocalDateTime.now()));
            case FUTURE:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserIdAndStartIsAfterOrderByStart(userId,
                        LocalDateTime.now()));
            case PAST:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserIdAndEndIsBeforeOrderByStart(userId,
                        LocalDateTime.now()));
            case REJECTED:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserIdAndStatusOrderByStart(userId,
                        Status.REJECTED));
            case WAITING:
                return bookingMapper.toOutDto(bookingRepository.findAllByUserIdAndStatusOrderByStart(userId,
                        Status.WAITING));
            default:
                throw new StateException();
        }
    }

    List<BookingOutDto> getBookingsForOwnerFilteredByState(long userId, State state) {
        switch (state) {
            case ALL:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserId_OrderByStartDesc(userId));
            case CURRENT:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserIdAndStartIsBeforeAndEndIsAfterOrderByStart(
                        userId, LocalDateTime.now(), LocalDateTime.now()));
            case FUTURE:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserIdAndStartIsAfterOrderByStart(userId,
                        LocalDateTime.now()));
            case PAST:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserIdAndEndIsBeforeOrderByStart(userId,
                        LocalDateTime.now()));
            case REJECTED:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserIdAndStatusOrderByStart(userId,
                        Status.REJECTED));
            case WAITING:
                return bookingMapper.toOutDto(bookingRepository.findAllByItemUserIdAndStatusOrderByStart(userId,
                        Status.WAITING));
            default:
                throw new StateException();
        }
    }

    void validateDateTimeOfBooking(BookingInDto bookingInDto) {
        LocalDateTime startTime = bookingInDto.getStart();
        LocalDateTime endTime = bookingInDto.getEnd();

        if (startTime.isBefore(LocalDateTime.now()) || endTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Некорректное время бронирования");
        }
    }
}