package com.rbp.movieapp3.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.List;

@Document(value = "ticket")
@Data
@NoArgsConstructor
public class Ticket {
    private BigInteger _id;
    private String loginId;
    private String movieName;
    private String theatreName;
    private Integer noOfTickets;
    private List<String> seatNumber;

    public Ticket(String loginId, String movieName, String theatreName, Integer noOfTickets, List<String> seatNumber) {
        this.loginId = loginId;
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.noOfTickets = noOfTickets;
        this.seatNumber = seatNumber;
    }

    public Ticket(BigInteger _id,String loginId, String movieName, String theatreName, Integer noOfTickets, List<String> seatNumber) {
        this._id = _id;
        this.loginId = loginId;
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.noOfTickets = noOfTickets;
        this.seatNumber = seatNumber;
    }
}
