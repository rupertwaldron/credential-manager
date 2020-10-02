package com.ruppyrup.credentialmanager.service;

import com.ruppyrup.credentialmanager.exceptions.CredentialNotFoundException;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;

import java.util.List;

public interface CredentialService {
    List<Credential> getAllCredentials();

    Credential getCredential(String uuid) throws CredentialNotFoundException;

    Credential createCredential(CredentialDTO credential);

    Credential updateCredential(String uuid, CredentialDTO credential) throws CredentialNotFoundException;

    Credential deleteCredential(String uuid) throws CredentialNotFoundException;
}
