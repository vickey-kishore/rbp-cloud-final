package com.rbp.movieapp3.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rbp.movieapp3.exception.MoviesNotFound;
import com.rbp.movieapp3.models.Movie;
import com.rbp.movieapp3.repository.UserRepository;
import com.rbp.movieapp3.security.services.MovieService;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void testGetAllMoviesWithoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllMovies() throws Exception {

        // Create a mock movie service that returns dummy data
        MovieService movieServiceMock = mock(MovieService.class);
        List<Movie> allMovies = new ArrayList<>();
        allMovies.add(new Movie(BigInteger.valueOf(111L),"naruto","inox",126,"Book ASAP"));
        when(movieService.getAllMovies()).thenReturn(allMovies);
        authenticate();
        // Inject the movie service mock into the controller and perform the request
        MovieController movieController = new MovieController();
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }

    @Test
    public void testGetMovieByNameReturnsListOfMovies() throws Exception {
        String movieName = "Avengers";
        List<Movie> movieList = new ArrayList<>();
        movieList.add(new Movie(BigInteger.valueOf(111L),"Avengers","inox",126,"Book ASAP"));
        movieList.add(new Movie(BigInteger.valueOf(111L),"Avengers","inox",126,"Book ASAP"));
        when(movieService.getMovieByName(movieName)).thenReturn(movieList);

        authenticate();

        MovieController movieController = new MovieController();
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/moviebooking/movies/search/Avengers"))
                .andExpect(status().isFound());

    }
    @Test(expected = MoviesNotFound.class)
    public void testGetMovieByNameReturnsNoMovies() throws Exception {
        String movieName = "Project-k";
        List<Movie> movieList = new ArrayList<>();
        movieList.add(new Movie(BigInteger.valueOf(111L),"Project-k","inox",126,"Book ASAP"));
        when(movieService.getMovieByName(movieName)).thenReturn(movieList);
        authenticate();
        MovieController movieController = new MovieController();
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/moviebooking/movies/search/Avengers"))
                .andExpect(status().isNotFound());
    }

    private void authenticate(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}

