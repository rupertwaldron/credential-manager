package com.ruppyrup.credentialmanager.unit_tests;

import com.ruppyrup.credentialmanager.controller.SpringController;
import com.ruppyrup.credentialmanager.exceptions.CredentialNotFoundException;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.DAOUser;
import com.ruppyrup.credentialmanager.service.CredentialService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class CredentialControllerUT {

    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private SpringController controller;

    private DAOUser user = new DAOUser();
    private Credential credential = new Credential(1L, "123", "Amazon", "www.amazon.com", "login", "password", user);
    private Credential credential2 = new Credential(2L, "456", "Boots", "www.boots.com", "john", "secrete", user);

    @Test
    public void shouldReturnHelloWhenHelloEndpointIsCalled() {
        Assertions.assertEquals("Hello from credentials", controller.sayHello());
    }

    @Test
    public void shouldReturnCredentialForGetRequest() throws CredentialNotFoundException {
        //given
        ResponseEntity<Credential> getResponse = ResponseEntity.ok(credential);
        //when
        Mockito.when(credentialService.getCredential(anyString())).thenReturn(credential);
        //then
        Assertions.assertEquals(getResponse, controller.findCredential("123"));
    }

    @Test
    public void shouldReturnListOfCredentials() {
        //given
        List<Credential> credentials = List.of(credential, credential2);
        ResponseEntity<List<Credential>> getAllResponse = ResponseEntity.ok(credentials);
        //when
        Mockito.when(credentialService.getAllCredentials()).thenReturn(credentials);
        //then
        Assertions.assertEquals(getAllResponse, controller.getAllCredentials());
    }

}
