package com.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    @Schema(
        description = "The timestamp in UTC when phone was booked",
        type = "Long"
    )
    @NotNull
    private Long timestamp;

    @Schema(
        description = "User identifier who booked the phone",
        type = "String",
        example = "user1"
    )
    @NotNull
    private String userName;

    @Schema(
        description = "Device identifier that was booked",
        type = "Short",
        example = "1"
    )
    @NotNull
    private Short deviceId;
}
