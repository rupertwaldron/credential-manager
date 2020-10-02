package com.ruppyrup.credentialmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ruppyrup.credentialmanager.dao.UserDao;
import com.ruppyrup.credentialmanager.exceptions.ExistingUserException;
import com.ruppyrup.credentialmanager.jwt.JwtTokenUtil;
import com.ruppyrup.credentialmanager.model.DAOUser;
import com.ruppyrup.credentialmanager.model.UserDTO;
import com.ruppyrup.credentialmanager.service.impl.JwtUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private UserDao userDao;

    private String username = "test";

    private String password = "test";

    private HttpHeaders headers;
    private UserDTO user = new UserDTO();
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void getToken() {
        user.setUsername(username);
        user.setPassword(password);
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
    }

    @AfterEach
    void deleteUser() {
        UserDTO userDTO = new UserDTO(username, password);
        userDetailsService.deleteUser(userDTO);
    }

    @Test
    void registerUser() throws JsonProcessingException {
        //given database is empty
        String userJson = mapper.writeValueAsString(user);
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);

        //when
        ResponseEntity<DAOUser> exchange = restTemplate.exchange("http://localhost:" + port + "/register", HttpMethod.POST, entity, DAOUser.class);

        //then
        assertThat(exchange.getBody().getUsername()).isEqualTo(username);
    }

    @Test
    void authenticateUser() throws JsonProcessingException, ExistingUserException, InterruptedException {
        //given
        userDetailsService.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String userJson = mapper.writeValueAsString(user);
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);

        //when
        ResponseEntity<String> exchange = restTemplate.exchange("http://localhost:" + port + "/authenticate", HttpMethod.POST, entity, String.class);
        String jwtToken = JsonPath.parse(exchange.getBody()).read("$.token");

        //then
        assertThat(jwtTokenUtil.validateToken(jwtToken, userDetails)).isTrue();
    }

    @Test
    void registerSameUserTwice_shouldReturnError() throws Exception {
        //given database is empty
        userDetailsService.save(user);
        String userJson = mapper.writeValueAsString(user);
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);

        //when
        ResponseEntity<DAOUser> exchange = restTemplate.exchange("http://localhost:" + port + "/register", HttpMethod.POST, entity, DAOUser.class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}