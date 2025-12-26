package com.flightservice.request;

import java.util.List;

public class SeatUpdateRequest {
    private List<String> seatNumbers;

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}
