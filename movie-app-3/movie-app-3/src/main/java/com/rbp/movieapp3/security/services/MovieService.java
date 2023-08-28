package com.rbp.movieapp3.security.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbp.movieapp3.models.Movie;
import com.rbp.movieapp3.models.Ticket;
import com.rbp.movieapp3.repository.MovieRepository;
import com.rbp.movieapp3.repository.TicketRepository;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private TicketRepository ticketRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getMovieByName(String movieName) {
        return movieRepository.findByMovieName(movieName);
    }

    public List<Ticket> findSeats(String movieName, String theatreName) {
        return ticketRepository.findSeats(movieName,theatreName);
    }

    public List<Movie> findAvailableTickets(String movieName, String theatreName) {
        return movieRepository.findAvailableTickets(movieName,theatreName);
    }

    public void saveTicket(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    public void saveMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public List<Ticket> getAllBookedTickets(String movieName) {
        return ticketRepository.findByMovieName(movieName);
    }

    public Integer getTotalNoTickets(String movieName){
        List<Ticket> tickets = ticketRepository.findByMovieName(movieName);
        int totaltickets = 0;
        for(Ticket ticket: tickets){
            totaltickets = totaltickets + ticket.getNoOfTickets();
        }
        return totaltickets;
    }

    public List<Movie> findByMovieName(String movieName) {
        return movieRepository.findByMovieName(movieName);
    }

    public void deleteByMovieName(String movieName) {
        movieRepository.deleteByMovieName(movieName);
    }

    public Object findTickets(String movieName, String s) {
        return null;
    }

    public Object findMovieByMovieNameAndTheatreName(String movieName, String s) {
        return null;
    }
}
