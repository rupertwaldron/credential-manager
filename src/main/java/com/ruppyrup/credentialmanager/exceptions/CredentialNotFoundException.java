package com.ruppyrup.credentialmanager.exceptions;

public class CredentialNotFoundException extends Exception {
    public CredentialNotFoundException() {
        super("Credential not found");
    }
}
