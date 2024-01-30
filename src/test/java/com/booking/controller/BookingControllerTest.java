package com.booking.controller;

import com.booking.dto.BookingAvailabilityDto;
import com.booking.model.Phone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.booking.dto.BookingDto;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.booking.Application.objectMapper;
import static com.booking.config.DateTimeConfiguration.zoneId;
import static com.booking.model.Phone.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingControllerTest {
    public static final int EXPECTED_BOOKINGS_SINGLE_DEVICE = 1;
    public static final int EXPECTED_BOOKINGS_MULTIPLE_DEVICES = 2;
    public static final short NOT_EXISTED_DEVICE_ID = 21;
    public static final String TEST_USER_ID = "userId";
    @LocalServerPort
    private int port;
    private String url;
    @PostConstruct
    public void init() {
        url = "http://localhost:" + port + "/api/v1/phone";
    }

    @Test
    @Order(1)
    public void testBookingNotExistingDeviceId() throws JsonProcessingException {
        BookingDto secondBookingDto = BookingDto.builder()
                .deviceId(NOT_EXISTED_DEVICE_ID)
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(secondBookingDto))
                .when()
                .post(url + "/book")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Order(2)
    public void testReturnNotExistingDeviceId() throws JsonProcessingException {
        BookingDto secondBookingDto = BookingDto.builder()
                .deviceId(NOT_EXISTED_DEVICE_ID)
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(secondBookingDto))
                .when()
                .post(url + "/return")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Order(3)
    public void testReturnNotBoookedDeviceId() throws JsonProcessingException {
        BookingDto bookingDto = BookingDto.builder()
                .deviceId(APPLE_IPHONE_12.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(bookingDto))
                .when()
                .post(url + "/return")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @Order(4)
    public void testAvailabilityNotExistingOrEmptyDeviceId() {
        assertThrows(AssertionError.class, () -> given()
                .contentType(ContentType.JSON)
                .when()
                .get(url + "/undefined")
                .then()
                .statusCode(HttpStatus.SC_OK));

        assertThrows(AssertionError.class, () -> phoneAvailability(NOT_EXISTED_DEVICE_ID));
    }

    @Test
    @Order(5)
    public void testAvailabilityPriorBooking() throws JsonProcessingException {
        List<BookingAvailabilityDto> bookingAvailabilityDtos = phoneAvailability(SAMSUNG_GALAXY_S9.getId());

        assertEquals(EXPECTED_BOOKINGS_SINGLE_DEVICE, bookingAvailabilityDtos.size());

        validateAvailable(SAMSUNG_GALAXY_S9.getId(), bookingAvailabilityDtos.iterator().next());
    }

    @Test
    @Order(6)
    public void testBookingSucceed() throws JsonProcessingException {
        testBooking(SAMSUNG_GALAXY_S9.getId());
    }

    @Test
    @Order(7)
    public void testBookingFailedDueToAvailability() throws JsonProcessingException {
        testBooking(ONEPLUS_9.getId());

        BookingDto secondBookingDto = BookingDto.builder()
                .deviceId(ONEPLUS_9.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        bookPhoneFailed(secondBookingDto);
    }

    @Test
    @Order(8)
    public void testBookingAndReturn() throws JsonProcessingException {
        testBooking(NOKIA_3310.getId());

        BookingDto returnBookingDto = BookingDto.builder()
                .deviceId(NOKIA_3310.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        returnPhone(returnBookingDto);

        List<BookingAvailabilityDto> bookingAvailabilityDtos = phoneAvailability(NOKIA_3310.getId());

        assertEquals(EXPECTED_BOOKINGS_SINGLE_DEVICE, bookingAvailabilityDtos.size());
        validateAvailable(NOKIA_3310.getId(), bookingAvailabilityDtos.get(0));
    }

    @Test
    @Order(9)
    public void testBookingAndCheckAvailabilityMultipleDevices() throws JsonProcessingException {
        BookingDto bookingDto = BookingDto.builder()
                .deviceId(Phone.SAMSUNG_GALAXY_S8.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        bookPhoneSucceeded(bookingDto);
        List<BookingAvailabilityDto> bookingAvailabilityDtos = phoneAvailability(Phone.SAMSUNG_GALAXY_S8.getId());

        assertEquals(EXPECTED_BOOKINGS_MULTIPLE_DEVICES, bookingAvailabilityDtos.size());

        validateBooked(bookingDto, bookingAvailabilityDtos.get(0));
        validateAvailable(Phone.SAMSUNG_GALAXY_S8.getId(), bookingAvailabilityDtos.get(1));

        BookingDto bookingSecondDto  = BookingDto.builder()
                .deviceId(Phone.SAMSUNG_GALAXY_S8.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        bookPhoneSucceeded(bookingSecondDto);
        bookingAvailabilityDtos = phoneAvailability(Phone.SAMSUNG_GALAXY_S8.getId());

        assertEquals(EXPECTED_BOOKINGS_MULTIPLE_DEVICES, bookingAvailabilityDtos.size());

        validateBooked(bookingSecondDto, bookingAvailabilityDtos.get(0));
        validateBooked(bookingSecondDto, bookingAvailabilityDtos.get(1));

        BookingDto returnBookingDto  = BookingDto.builder()
                .deviceId(Phone.SAMSUNG_GALAXY_S8.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        /*return and check first booking*/
        returnPhone(returnBookingDto);
        bookingAvailabilityDtos = phoneAvailability(SAMSUNG_GALAXY_S8.getId());

        assertEquals(EXPECTED_BOOKINGS_MULTIPLE_DEVICES, bookingAvailabilityDtos.size());
        validateAvailable(SAMSUNG_GALAXY_S8.getId(), bookingAvailabilityDtos.get(1));

        BookingDto returnBookingSecondDto  = BookingDto.builder()
                .deviceId(Phone.SAMSUNG_GALAXY_S8.getId())
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        /*return and check second booking*/
        returnPhone(returnBookingSecondDto);
        bookingAvailabilityDtos = phoneAvailability(SAMSUNG_GALAXY_S8.getId());

        validateAvailable(SAMSUNG_GALAXY_S8.getId(), bookingAvailabilityDtos.get(0));
        validateAvailable(SAMSUNG_GALAXY_S8.getId(), bookingAvailabilityDtos.get(1));
    }

    private void testBooking(short phoneId) throws JsonProcessingException {
        BookingDto bookingDto = BookingDto.builder()
                .deviceId(phoneId)
                .timestamp(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
                .userName(TEST_USER_ID)
                .build();

        bookPhoneSucceeded(bookingDto);

        List<BookingAvailabilityDto> noBookingsAvailable = phoneAvailability(phoneId);

        assertEquals(EXPECTED_BOOKINGS_SINGLE_DEVICE, noBookingsAvailable.size());

        validateBooked(bookingDto, noBookingsAvailable.iterator().next());
    }

    private void returnPhone(BookingDto bookingDto) throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(bookingDto))
                .when()
                .post(url + "/return")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private void bookPhoneSucceeded(BookingDto bookingDto) throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(bookingDto))
                .when()
                .post(url + "/book")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private void bookPhoneFailed(BookingDto bookingDto) throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(bookingDto))
                .when()
                .post(url + "/book")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private List<BookingAvailabilityDto> phoneAvailability(short phoneId) {
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(url + "/" + phoneId)
                .then()
                .statusCode(HttpStatus.SC_OK);

        List<BookingAvailabilityDto> availability = Arrays.asList(response.extract().response().getBody().as(BookingAvailabilityDto[].class));
        return availability;
    }

    private void validateBooked(BookingDto source, BookingAvailabilityDto target) {
        assertEquals(source.getDeviceId(), target.getDeviceId());
        assertEquals(source.getUserName(), target.getUserName());
        assertEquals(target.getAvailable(), false);
    }

    private void validateAvailable(short devideId, BookingAvailabilityDto target) {
        assertEquals(devideId, target.getDeviceId());
        assertEquals(StringUtils.EMPTY, target.getUserName());
        assertEquals(true, target.getAvailable());
    }
}
