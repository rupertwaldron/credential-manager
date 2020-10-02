package com.ruppyrup.credentialmanager.controller;

import com.ruppyrup.credentialmanager.exceptions.ExistingUserException;
import com.ruppyrup.credentialmanager.jwt.JwtRequest;
import com.ruppyrup.credentialmanager.jwt.JwtResponse;
import com.ruppyrup.credentialmanager.jwt.JwtTokenUtil;
import com.ruppyrup.credentialmanager.model.DAOUser;
import com.ruppyrup.credentialmanager.model.UserDTO;
import com.ruppyrup.credentialmanager.service.impl.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping(value = "/register")
    public ResponseEntity<DAOUser> saveUser(@RequestBody UserDTO user) {
        DAOUser createdUser = null;
        HttpStatus status = HttpStatus.CREATED;
        try {
            createdUser = userDetailsService.save(user);
        } catch (ExistingUserException e) {
            status = HttpStatus.FORBIDDEN;
        }
       return ResponseEntity
               .status(status)
               .body(createdUser);
    }

    @DeleteMapping(value = "/users")
    public void deleteUser(@RequestBody UserDTO userDTO) {
            userDetailsService.deleteUser(userDTO);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
