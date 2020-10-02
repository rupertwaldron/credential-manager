package com.ruppyrup.credentialmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.credentialmanager.config.JwtContextManagerTestImpl;
import com.ruppyrup.credentialmanager.dao.CredentialDao;
import com.ruppyrup.credentialmanager.dao.UserDao;
import com.ruppyrup.credentialmanager.encryption.IEncryptionService;
import com.ruppyrup.credentialmanager.exceptions.ExistingUserException;
import com.ruppyrup.credentialmanager.jwt.JwtTokenUtil;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CredentialControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    CredentialDao credentialDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    JwtContextManagerTestImpl jwtContextManager;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    IEncryptionService<Credential> encryptionService;

    private DAOUser user1Dao;

    private String username = "test";

    private String password = "test";

    private String token;

    private Credential credential1;
    private Credential credential2;

    private HttpHeaders headers;
    private UserDTO user = new UserDTO();
    private ObjectMapper mapper = new ObjectMapper();

    private String user1 = "javainuse";
    private String user2 = "ruppyrup";
    private String user3 = "bob";
    private CredentialDTO amazonUser1DTO = new CredentialDTO( "Amazon", "www.amazon.com", "pete", "football");
    private CredentialDTO pondUser1DTO = new CredentialDTO("PondPlanet", "www.pondplanet.com", "ruppyrup", "feelsick");

    @BeforeEach
    public void setup() throws ExistingUserException {
        // create user and save
        user.setUsername(username);
        user.setPassword(password);
        userDetailsService.save(user);
        user1Dao = userDetailsService.getUser(username);
        //creat credential and save
        credential1 = new Credential(amazonUser1DTO);
        credential1.setUuid("1234");
        credential1.setUser(user1Dao);
        credential1 = encryptionService.encrypt(credential1);
        credentialDao.save(credential1);
        credential2 = new Credential(pondUser1DTO);
        credential2.setUuid("abcd");
        credential2.setUser(user1Dao);
        credential2 = encryptionService.encrypt(credential2);
        credentialDao.save(credential2);



        //set jwtContext user and get token
        jwtContextManager.setUser(username);
        User savedUser = new User(this.user.getUsername(), this.user.getPassword(), new ArrayList<>());
        token = jwtTokenUtil.generateToken(savedUser);

        // set headers
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setBearerAuth(token);
    }

    @AfterEach
    public void clearData() {
        credentialDao.deleteAll();
        userDao.deleteAll();
    }

    @Test
    public void findCredential() {
        //given database has two credentials and valid jwt token
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("credentials/" + credential1.getUuid())
                .build();

        //when
        ResponseEntity<Credential> exchange = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, entity, Credential.class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).isEqualTo(encryptionService.decrypt(credential1));
    }

    @Test
    public void findAllCredentials() {
        //given database has two credentials and valid jwt token
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("credentials")
                .build();

        //when
        ResponseEntity<Credential[]> exchange = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, entity, Credential[].class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).containsExactlyInAnyOrder(encryptionService.decrypt(credential1), encryptionService.decrypt(credential2));
    }


    @Test
    public void createCredential() throws JsonProcessingException {
        //given database has two credential and valid jwt token
        CredentialDTO credentialDTO = new CredentialDTO("John Lewis", "www.jl.com", "rr", "spoons");
        String credentialJson = mapper.writeValueAsString(credentialDTO);
        HttpEntity<String> entity = new HttpEntity<>(credentialJson, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("credentials")
                .build();

        //when
        ResponseEntity<Credential> exchange = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, entity, Credential.class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(new CredentialDTO(exchange.getBody())).isEqualToIgnoringGivenFields(credentialDTO, "login", "password");
    }

    @Test
    public void updateCredential() throws JsonProcessingException {
        //given database has two credential and valid jwt token
        CredentialDTO credentialDTO = new CredentialDTO("John Lewis", "www.jl.com", "rr", "spoons");
        String credentialJson = mapper.writeValueAsString(credentialDTO);
        HttpEntity<String> entity = new HttpEntity<>(credentialJson, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("credentials/" + credential1.getUuid())
                .build();

        //when
        ResponseEntity<Credential> exchange = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.PUT, entity, Credential.class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new CredentialDTO(exchange.getBody())).isEqualToIgnoringGivenFields(credentialDTO, "login", "password");
    }

    @Test
    public void deleteCredential() throws JsonProcessingException {
        //given database has two credential and valid jwt token
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("credentials/" + credential1.getUuid())
                .build();

        //when
        ResponseEntity<Credential> exchange = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.DELETE, entity, Credential.class);

        //then
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).isEqualTo(credential1);
    }
}
