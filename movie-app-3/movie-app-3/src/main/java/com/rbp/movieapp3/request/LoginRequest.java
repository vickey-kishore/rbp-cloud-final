package com.rbp.movieapp3.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
}
