package com.booking.repository;

import com.booking.dto.BookingAvailabilityDto;
import com.booking.dto.BookingDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class BookingRepository {
    public static final String DECREASE_AVAILABILITY = "update availability set quantity = quantity - 1 where device_id = ?;";
    public static final String INCREASE_AVAILABILITY = "update availability set quantity = quantity + 1 where device_id = ?;";
    public static final String BOOK_PHONE = "insert into booking (user_id, device_id, booking_date) values (?,?,?)";
    public static final String FIND_BY_DEVICE_ID = "select user_id, booking_date " +
            "from booking " +
            "where device_id = ?" +
            "order by booking_date desc limit ?";
    public static final String FIND_AVAILABILITY = "select quantity, max_quantity " +
            "from availability " +
            "where device_id = ?";
    private final DataSource dataSource;

    public BookingRepository(@Autowired DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void bookPhone(BookingDto bookingDto) {
        try (
                Connection connection = dataSource.getConnection()
        ) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(DECREASE_AVAILABILITY)) {
                statement.setShort(1, bookingDto.getDeviceId());
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(BOOK_PHONE)) {
                statement.setString(1, bookingDto.getUserName());
                statement.setShort(2, bookingDto.getDeviceId());
                statement.setTimestamp(3, new Timestamp(bookingDto.getTimestamp()));

                statement.executeUpdate();
            }

            connection.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void returnPhone(short phoneId) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INCREASE_AVAILABILITY)
        ) {
            statement.setShort(1, phoneId);

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<BookingAvailabilityDto> findByPhoneId(short phoneId) {
        try (
                Connection connection = dataSource.getConnection()
        ) {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(FIND_AVAILABILITY)) {
                short quantity = 0;
                short maxQuantity = 0;

                statement.setShort(1, phoneId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        quantity = rs.getShort("quantity");
                        maxQuantity = rs.getShort("max_quantity");
                    }
                }

                if (maxQuantity <= 0) {
                    return Collections.EMPTY_LIST;
                }

                List<BookingAvailabilityDto> result = new ArrayList<>();

                if (maxQuantity - quantity > 0) {

                    try (PreparedStatement readBookings = connection.prepareStatement(FIND_BY_DEVICE_ID)) {

                        readBookings.setShort(1, phoneId);
                        readBookings.setShort(2, (short) (maxQuantity - quantity));

                        try (ResultSet rs = readBookings.executeQuery()) {
                            while (rs.next()) {
                                result.add(BookingAvailabilityDto.builder()
                                        .deviceId(phoneId)
                                        .userName(rs.getString("user_id"))
                                        .timestamp(rs.getTimestamp("booking_date").getTime())
                                        .available(false)
                                        .build());
                            }
                        }
                    }
                }

                for (int i = 0; i < quantity; i++) {
                    result.add(BookingAvailabilityDto.builder()
                            .deviceId(phoneId)
                            .userName(StringUtils.EMPTY)
                            .timestamp(null)
                            .available(true)
                            .build());
                }

                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
