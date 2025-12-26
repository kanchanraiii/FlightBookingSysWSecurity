package com.flightservice.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {

    private String flightId;
    private List<String> unbookedSeats;
    private List<String> bookedSeats;
    private List<String> cancelledSeats;
}
