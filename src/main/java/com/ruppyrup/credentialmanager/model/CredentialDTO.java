package com.ruppyrup.credentialmanager.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CredentialDTO {

    @NotBlank(message = "credentialName can't be empty")
    private String credentialName;

    @NotBlank(message = "url can't be empty")
    private String url;

    @NotBlank(message = "login can't be empty")
    private String login;

    @NotBlank(message = "password can't be empty")
    private String password;

    public CredentialDTO() {
    }

    public CredentialDTO(String credentialName, String url, String login, String password) {
        this.credentialName = credentialName;
        this.url = url;
        this.login = login;
        this.password = password;
    }

    public CredentialDTO(Credential credential) {
        this.credentialName = credential.getCredentialName();
        this.login = credential.getLogin();
        this.password = credential.getPassword();
        this.url = credential.getUrl();
    }
}
