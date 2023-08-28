package com.rbp.movieapp3.controller;

import com.rbp.movieapp3.controller.MovieController;
import com.rbp.movieapp3.exception.MoviesNotFound;
import com.rbp.movieapp3.models.Movie;
import com.rbp.movieapp3.models.Ticket;
import com.rbp.movieapp3.models.User;
import com.rbp.movieapp3.request.LoginRequest;
import com.rbp.movieapp3.repository.MovieRepository;
import com.rbp.movieapp3.repository.TicketRepository;
import com.rbp.movieapp3.repository.UserRepository;
import com.rbp.movieapp3.security.services.MovieService;


import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class MovieAppControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MovieService movieService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieController movieController;


    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    void changePasswordSuccessTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("test");
        loginRequest.setPassword("new_password");

        User existingUser = new User("test", "John", "Doe", "johndoe@example.com", 1234567890L, "old_password");
        when(userRepository.findByLoginId("test")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_password");

        ResponseEntity<String> responseEntity = movieController.changePassword(loginRequest, "test");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userRepository, times(1)).findByLoginId("test");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePasswordUserNotFoundTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("test");
        loginRequest.setPassword("new_password");

        when(userRepository.findByLoginId("test")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            movieController.changePassword(loginRequest, "test");
        });

        verify(userRepository, times(1)).findByLoginId("test");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllMoviesSuccessTest() {
        List<Movie> movieList = new ArrayList<>();
        movieList.add(new Movie("Avengers", "Action", 10, "Book ASAP"));

        when(movieService.getAllMovies()).thenReturn(movieList);

        ResponseEntity<List<Movie>> responseEntity = movieController.getAllMovies();

        assertEquals(HttpStatus.FOUND, responseEntity.getStatusCode());
        assertEquals(movieList, responseEntity.getBody());

        verify(movieService, times(1)).getAllMovies();
    }

    @Test
    void getAllMoviesNotFoundTest() {
        when(movieService.getAllMovies()).thenReturn(new ArrayList<>());

        assertThrows(MoviesNotFound.class, () -> {
            movieController.getAllMovies();
        });

        verify(movieService, times(1)).getAllMovies();
    }

    @Test
    void getMovieByNameTest() {

        String movieName = "Avengers";
        List<Movie> expectedMovies = Arrays.asList(new Movie(), new Movie());
        when(movieService.findByMovieName(movieName)).thenReturn(expectedMovies);
        ResponseEntity<List<Movie>> response = movieController.getMovieByName(movieName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedMovies, response.getBody());
    }

    @Test
    void getMovieByNameNotFoundTest() {
        String movieName = "Not Found";
        when(movieService.findByMovieName(movieName)).thenReturn(Collections.emptyList());
        MoviesNotFound exception = assertThrows(MoviesNotFound.class, () -> movieController.getMovieByName(movieName));
        assertEquals("Movies Not Found", exception.getMessage());
    }

    @Test
    void bookTicketsTest() {
        String movieName = "Avengers";
        Ticket ticket = new Ticket();
        ticket.setNoOfTickets(2);
        ticket.setSeatNumber(Arrays.asList("A1", "A2"));
        ticket.setTheatreName("Theatre 1");
        ticket.setLoginId("user1");
        Movie movie = new Movie();
        movie.setMovieName(movieName);
        movie.setTheatreName("Theatre 1");
        movie.setNoOfTicketsAvailable(10);
        when(movieService.findTickets(movieName, "Theatre 1")).thenReturn(Collections.emptyList());
        when(movieService.findMovieByMovieNameAndTheatreName(movieName, "Theatre 1")).thenReturn(movie);
        ResponseEntity<String> response = movieController.bookTickets(ticket, movieName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tickets Booked Successfully with seat numbers[A1, A2]", response.getBody());
        verify(movieService, times(1)).saveTicket(ticket);
        verify(movieService, times(1)).saveMovie(movie);

    }

    @Test
    void bookTicketsInvalidNumberOfSeatsTest() {

        String movieName = "Avengers";
        Ticket ticket = new Ticket();
        ticket.setNoOfTickets(2);
        ticket.setSeatNumber(Arrays.asList("A1"));
        ticket.setTheatreName("Theatre 1");
        ResponseEntity<String> response = movieController.bookTickets(ticket, movieName);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid number of seats", response.getBody());

    }

    @Test
    void getAllBookedTicketsTest() {
        String movieName = "The Matrix";
        Movie movie = new Movie();
        movie.setMovieName(movieName);
        movie.setNoOfTicketsAvailable(10);
        movie.setTicketsStatus("BOOK ASAP");
        movieRepository.save(movie);
        Ticket ticket = new Ticket();
        ticket.setMovieName(movieName);
        ticket.setSeatNumber(List.of("A1"));
        ticketRepository.save(ticket);
        ResponseEntity<List<Ticket>> response = movieController.getAllBookedTickets(movieName);
        List<Ticket> tickets = response.getBody();
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(tickets);
    }

    @Test
    void updateTicketStatusTest() {
        String movieName = "testMovie";
        ObjectId ticketId = new ObjectId();
        List<Movie> movies = new ArrayList<>();
        Movie movie = new Movie();
        movie.setNoOfTicketsAvailable(1);
        movie.setTicketsStatus("BOOK ASAP");
        movies.add(movie);
        Ticket ticket = new Ticket();
        ticket.set_id(BigInteger.ONE);

//        when(movieService.findByMovieName(movieName)).thenReturn(movies);
//        when(ticketRepository.findBy_id(ticketId)).thenReturn(ticket);

        //ResponseEntity<String> response = movieController.updateTicketStatus(movieName, ticketId);
       // ResponseEntity<String> response = movieController.updateTicketStatus(movieName);
        //assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    void deleteMovieTest() {
        String movieName = "testMovie";
        List<Movie> availableMovies = new ArrayList<>();
        Movie movie = new Movie();
        movie.setMovieName(movieName);
        availableMovies.add(movie);

        when(movieService.findByMovieName(movieName)).thenReturn(availableMovies);

        ResponseEntity<String> response = movieController.deleteMovie(movieName);
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }

}
