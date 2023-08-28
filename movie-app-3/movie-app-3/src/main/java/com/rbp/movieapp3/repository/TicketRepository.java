package com.rbp.movieapp3.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.rbp.movieapp3.models.Ticket;

public interface TicketRepository extends MongoRepository<Ticket,String> {
    @Query(value = "{'movieName' : ?0,'theatreName' : ?1}", fields = "{_id:0, seatNumber:1}")
    List<Ticket> findSeats(String movieName, String theatreName);

    List<Ticket> findByMovieName(String movieName);

    List<Ticket> findBy_id(Long _id);
}
