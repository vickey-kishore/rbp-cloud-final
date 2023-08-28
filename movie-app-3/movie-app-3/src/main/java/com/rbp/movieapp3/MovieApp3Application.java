package com.rbp.movieapp3;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.rbp.movieapp3.models.ERole;
import com.rbp.movieapp3.models.Movie;
import com.rbp.movieapp3.models.Role;
import com.rbp.movieapp3.repository.MovieRepository;
import com.rbp.movieapp3.repository.RoleRepository;

@SpringBootApplication
public class MovieApp3Application implements CommandLineRunner {

	@Autowired
	private MovieRepository movieRepository;
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MongoTemplate mongoTemplate;


	public static void main(String[] args) {
		SpringApplication.run(MovieApp3Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("roles");
//		mongoTemplate.dropCollection("ticket");
//		mongoTemplate.dropCollection("users");
		mongoTemplate.dropCollection("movie");

		Movie movie1 = new Movie(BigInteger.valueOf(111L),"naruto","inox",126,"Book ASAP");
	 	Movie movie2 = new Movie(BigInteger.valueOf(222L),"batman","inox",122,"Book ASAP");
	 	Movie movie3 = new Movie(BigInteger.valueOf(333L),"one piece","inox",2,"Book ASAP");
		Movie movie4 = new Movie(BigInteger.valueOf(444L),"spider man","inox",1,"Book ASAP");

	 	movieRepository.saveAll(List.of(movie1,movie2,movie3,movie4));

		Role admin = new Role(ERole.ROLE_ADMIN);
		Role user = new Role(ERole.ROLE_USER);

		roleRepository.saveAll(List.of(admin,user));
	}
}
