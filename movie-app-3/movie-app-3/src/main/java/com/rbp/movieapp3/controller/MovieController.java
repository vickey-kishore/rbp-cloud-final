package com.rbp.movieapp3.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rbp.movieapp3.exception.MoviesNotFound;
import com.rbp.movieapp3.exception.SeatAlreadyBooked;
import com.rbp.movieapp3.models.Movie;
import com.rbp.movieapp3.models.Ticket;
import com.rbp.movieapp3.models.User;
import com.rbp.movieapp3.repository.MovieRepository;
import com.rbp.movieapp3.repository.TicketRepository;
import com.rbp.movieapp3.repository.UserRepository;
import com.rbp.movieapp3.request.LoginRequest;
import com.rbp.movieapp3.security.services.MovieService;
import com.rbp.movieapp3.security.services.UserDetailsServiceImpl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1.0/moviebooking")
@OpenAPIDefinition(
        info = @Info(
                title = "Movie Application API",
                description = "This API provides endpoints for managing movies."
        )
)
@Slf4j
public class MovieController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NewTopic topic;


    /* --- RESET PASSWORD --- */
    @PutMapping("/{loginId}/forgot")
//    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "reset password")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(@RequestBody LoginRequest loginRequest, @PathVariable String loginId){
        System.out.println("forgot password called");
        log.debug("forgot password endopoint accessed by "+loginRequest.getLoginId());
        Optional<User> user1 = userRepository.findByLoginId(loginId);
            User availableUser = user1.get();
            User updatedUser = new User(
                            loginId,
                    availableUser.getFirstName(),
                    availableUser.getLastName(),
                    availableUser.getEmail(),
                    availableUser.getContactNumber(),
                    passwordEncoder.encode(loginRequest.getPassword())
                    );
            updatedUser.set_id(availableUser.get_id());
            updatedUser.setRoles(availableUser.getRoles());
            userRepository.save(updatedUser);
            log.debug(loginRequest.getLoginId()+" has password changed successfully");
            return new ResponseEntity<>("Users password changed successfully",HttpStatus.OK);
    }


    /* --- TO GET LIST OF ALL AVAILABLE MOVIES --- */
    @GetMapping("/all")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "search all movies")
    public ResponseEntity<List<Movie>> getAllMovies(){
        log.debug("here u can access all the available movies");

        List<Movie> movieList = movieService.getAllMovies();

        if(movieList.isEmpty()){
            log.debug("currently no movies are available");
            throw new MoviesNotFound("No Movies are available");
        }
        else{
            log.debug("listed the available movies");
            System.out.println("all movies are fetched");
            System.out.println(new ResponseEntity<>(movieList, HttpStatus.OK));
            return new ResponseEntity<>(movieList, HttpStatus.OK);
        }
    }


    /* --- SEARCH MOVIE BY NAME --- */
    @GetMapping("/movies/search/{movieName}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "search movies by movie name")
    public ResponseEntity<List<Movie>> getMovieByName(@PathVariable String movieName){
        log.debug("here search a movie by its name");
        List<Movie> movieList = movieService.getMovieByName(movieName);
        if(movieList.isEmpty()){
            log.debug("currently no movies are available");
            throw new MoviesNotFound("Movies Not Found");
        }
        else
            log.debug("listed the available movies with title:"+movieName);
            return new ResponseEntity<>(movieList,HttpStatus.OK);
    }


    /* --- BOOK A TICKET --- */
    @PostMapping("/{movieName}/add")@SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "book ticket")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> bookTickets(@RequestBody Ticket ticket, @PathVariable String movieName) {

        System.out.println("ticket -->" + ticket);

        log.debug(ticket.getLoginId()+" entered to book tickets");
        List<Ticket> allTickets = movieService.findSeats(movieName,ticket.getTheatreName());
        for(Ticket each : allTickets){
            for(int i = 0; i < ticket.getNoOfTickets(); i++){
                if(each.getSeatNumber().contains(ticket.getSeatNumber().get(i))){
                    log.debug("seat is already booked");
                    throw new SeatAlreadyBooked("Seat number "+ticket.getSeatNumber().get(i)+" is already booked");
                }
            }
        }

        if(movieService.findAvailableTickets(movieName,ticket.getTheatreName()).get(0).getNoOfTicketsAvailable() >=
                ticket.getNoOfTickets()){

            log.info("available tickets "
                    +movieService.findAvailableTickets(movieName,ticket.getTheatreName())
                                                        .get(0).getNoOfTicketsAvailable());
            movieService.saveTicket(ticket);

            log.debug(ticket.getLoginId()+" booked "+ticket.getNoOfTickets()+" tickets");

            kafkaTemplate.send(topic.name(),"Movie ticket booked. " +
                    "Booking Details are: "+
            ticket);

            List<Movie> movies = movieRepository.findByMovieName(movieName);
            int available_tickets = 0;
            for (Movie movie : movies) {
                available_tickets = movie.getNoOfTicketsAvailable() - ticket.getNoOfTickets();
                if(available_tickets == 0){
                    movie.setTicketsStatus("Booking Closed");
                }
                movie.setNoOfTicketsAvailable(available_tickets);
                movieService.saveMovie(movie);
            }
            return new ResponseEntity<>("Tickets Booked Successfully with seat numbers"+ticket.getSeatNumber(),HttpStatus.OK);
        }
        else{
            log.debug("tickets sold out");
            return new ResponseEntity<>("\"All tickets sold out\"",HttpStatus.OK);
        }
    }


    /* --- GET ALL BOOKED  TICKETS */
    @GetMapping("/getallbookedtickets/{movieName}")@SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "get all booked tickets(Admin Only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ticket>> getAllBookedTickets(@PathVariable String movieName){
        List<Ticket> bookedticckets = movieService.getAllBookedTickets(movieName);
        System.out.println("booked" + bookedticckets);
        return new ResponseEntity<>(movieService.getAllBookedTickets(movieName),HttpStatus.OK);
    }

@PutMapping("/{movieName}/update/{ticketId}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> updateTicketStatus(@PathVariable String movieName, @PathVariable Long ticketId) {
    List<Movie> movie = movieRepository.findByMovieName(movieName);
    List<Ticket> ticket = ticketRepository.findBy_id(ticketId);
    if (movie == null) {
        throw new MoviesNotFound("Movie not found: " + movieName);
    }

    if (ticket == null) {
        throw new NoSuchElementException("Ticket Not found:" + ticketId);
    }
    int ticketsBooked = movieService.getTotalNoTickets(movieName);
    for (Movie movies : movie) {
        if (ticketsBooked >= movies.getNoOfTicketsAvailable()) {
            movies.setTicketsStatus("SOLD OUT");
        } else {
            movies.setTicketsStatus("BOOK ASAP");
        }
        movieService.saveMovie(movies);
    }
    kafkaTemplate.send(topic.name(),"tickets status upadated by the Admin for movie "+movieName);
    return new ResponseEntity<>("Ticket status updated successfully", HttpStatus.OK);

}

    // --- DELETE MOVIE
    @DeleteMapping("/{movieName}/delete")@SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "delete a movie(Admin Only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMovie(@PathVariable String movieName){
        List<Movie> availableMovies = movieService.findByMovieName(movieName);
        if(availableMovies.isEmpty()){
            throw new MoviesNotFound("No movies Available with moviename "+ movieName);
        }
        else {
            movieService.deleteByMovieName(movieName);
            kafkaTemplate.send(topic.name(),"Movie Deleted by the Admin. "+movieName+" is now not available");
            return new ResponseEntity<>("Movie deleted successfully",HttpStatus.OK);
        }

    }

    /* --- ADD MOVIE --- */
    @PostMapping("/addmovie")@SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "add a movie(Admin Only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> saveMovie(@RequestBody Movie movie){
        movieService.saveMovie(movie);
        return new ResponseEntity<>("new Movie Added",HttpStatus.OK);
    }

}
