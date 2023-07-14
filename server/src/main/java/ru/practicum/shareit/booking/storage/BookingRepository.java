package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                LocalDateTime endBefore,
                                                                Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId,
                                                                 LocalDateTime startAfter,
                                                                 Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                             LocalDateTime startBefore,
                                                                             LocalDateTime endAfter,
                                                                             Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId,
                                                             BookingStatus bookingStatus,
                                                             Pageable pageable);

    Page<Booking> findAllByItemOwnerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                   LocalDateTime endBefore,
                                                                   Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long bookerId,
                                                                    LocalDateTime startAfter,
                                                                    Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                LocalDateTime startBefore,
                                                                                LocalDateTime endAfter,
                                                                                Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus bookingStatus);

    List<Booking> findAllByStatus(BookingStatus bookingStatus);

    Page<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId,
                                                                BookingStatus bookingStatus,
                                                                Pageable pageable);

    Booking findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(Long itemId,
                                                                     BookingStatus status,
                                                                     LocalDateTime startBefore);

    Booking findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId,
                                                                   BookingStatus status,
                                                                   LocalDateTime startAfter);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId,
                                                           Long userId,
                                                           BookingStatus status,
                                                           LocalDateTime endBefore);
}
