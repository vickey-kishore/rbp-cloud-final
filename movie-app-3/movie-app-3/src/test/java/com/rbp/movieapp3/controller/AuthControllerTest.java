package com.rbp.movieapp3.controller;

import com.rbp.movieapp3.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.Collections;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;



    @Test
    public void testAuthenticateUser() throws Exception {
        String loginId = "testUser";
        String password = "testPassword";
        String jwtToken = "mockedJwtToken";
        UserDetails userDetails = new UserDetailsImpl("vickey","vickey","vickey,","vickey","vickey@gmail.com",9865371301L,"vickey");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, password);

        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(authentication);

        Mockito.when(userDetails.getAuthorities())
                .thenReturn(Collections.singletonList(() -> "ROLE_USER"));

        Mockito.when(userDetails.getUsername())
                .thenReturn("vickey");



        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/moviebooking/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"testUser\",\"password\":\"testPassword\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").value(jwtToken))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("userId"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testUser"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0]").value("ROLE_USER"));
    }
    

}
