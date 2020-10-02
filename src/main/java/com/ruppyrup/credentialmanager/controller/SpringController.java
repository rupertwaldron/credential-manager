package com.ruppyrup.credentialmanager.controller;

import com.ruppyrup.credentialmanager.exceptions.CredentialNotFoundException;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;
import com.ruppyrup.credentialmanager.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
public class SpringController {

    @Autowired
    CredentialService credentialService;

    @GetMapping("/credentials")
    public ResponseEntity<List<Credential>> getAllCredentials() {
        return new ResponseEntity<>(credentialService.getAllCredentials(), HttpStatus.OK);
    }

    @GetMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello from credentials";
    }

    @GetMapping("/credentials/{uuid}")
    public ResponseEntity<Credential> findCredential(@PathVariable String uuid) {
        HttpStatus status = HttpStatus.OK;
        Credential credential = null;
        try {
            credential = credentialService.getCredential(uuid);
        } catch (CredentialNotFoundException e) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity
                .status(status)
                .body(credential);
    }

    @PostMapping("/credentials")
    public ResponseEntity<Credential> createCredential(@RequestBody @Valid CredentialDTO credentialDTO) {
        Credential createdCredential = credentialService.createCredential(credentialDTO);
        HttpStatus status = HttpStatus.CREATED;
        if (createdCredential == null) status = HttpStatus.CONFLICT;
        return ResponseEntity
                .status(status)
                .body(createdCredential);
    }

    @PutMapping("/credentials/{uuid}")
    public ResponseEntity<Credential> updateCredential(@PathVariable String uuid, @RequestBody CredentialDTO credentialDTO) {
        HttpStatus status = HttpStatus.OK;
        Credential updatedCredential = null;
        try {
            updatedCredential = credentialService.updateCredential(uuid, credentialDTO);
        } catch (CredentialNotFoundException e) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity
                .status(status)
                .body(updatedCredential);
    }

    @DeleteMapping("/credentials/{uuid}")
    public ResponseEntity<Credential>  deleteCredential(@PathVariable String uuid) {
        HttpStatus status = HttpStatus.OK;
        Credential deletedCredential = null;
        try {
            deletedCredential = credentialService.deleteCredential(uuid);
        } catch (CredentialNotFoundException e) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity
                .status(status)
                .body(deletedCredential);
    }


}
