package com.booking.service;

import com.booking.dto.BookingAvailabilityDto;
import com.booking.dto.BookingDto;
import com.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(@Autowired BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void bookPhone(BookingDto bookingDto) {
        bookingRepository.bookPhone(bookingDto);
    }

    public void returnPhone(BookingDto bookingDto) {
        bookingRepository.returnPhone(bookingDto.getDeviceId());
    }
    public List<BookingAvailabilityDto> findByPhoneId(short phoneId) {
        return bookingRepository.findByPhoneId(phoneId);
    }
}
