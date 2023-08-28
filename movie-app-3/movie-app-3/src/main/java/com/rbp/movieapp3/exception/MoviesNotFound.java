package com.rbp.movieapp3.exception;

public class MoviesNotFound extends RuntimeException {
    public MoviesNotFound(String noMoviesAreAvailable) {
        super(noMoviesAreAvailable);
    }
}
