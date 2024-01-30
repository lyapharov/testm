package com.booking.controller;

import com.booking.dto.BookingAvailabilityDto;
import com.booking.dto.BookingDto;
import com.booking.model.Phone;
import com.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/phone")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(@Autowired BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Book device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception")
    })
    @PostMapping("/book")
    public void bookPhone(@Valid
                          @Parameter(description = "Book phone information", required = true)
                          @RequestBody BookingDto bookingDto) {
        if (Arrays.stream(Phone.values()).noneMatch(device -> device.getId() == bookingDto.getDeviceId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        bookingService.bookPhone(bookingDto);
    }

    @Operation(summary = "Book device by id ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception")
    })
    @PostMapping("/return")
    public void returnPhone(@Valid
                            @Parameter(description = "Return phone", required = true)
                            @RequestBody BookingDto bookingDto) {
        if (Arrays.stream(Phone.values()).noneMatch(device -> device.getId() == bookingDto.getDeviceId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        bookingService.returnPhone(bookingDto);
    }

    @Operation(summary = "Get phones availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception")
    })
    @GetMapping("/{phoneId}")
    public ResponseEntity<List<BookingAvailabilityDto>> availability(
            @Parameter(name = "phoneId", description = "Phone identifier")
            @PathVariable Short phoneId) {
        List<BookingAvailabilityDto> bookingDtos = bookingService.findByPhoneId(phoneId);
        if (CollectionUtils.isEmpty(bookingDtos)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(bookingDtos);
    }
}