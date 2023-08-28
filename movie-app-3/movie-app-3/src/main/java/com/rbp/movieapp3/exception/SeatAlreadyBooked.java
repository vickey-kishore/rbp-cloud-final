package com.rbp.movieapp3.exception;

public class SeatAlreadyBooked extends RuntimeException {
    public SeatAlreadyBooked(String s) {
        super(s);
    }
}
