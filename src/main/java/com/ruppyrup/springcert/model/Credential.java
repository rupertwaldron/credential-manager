package com.ruppyrup.springcert.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.UUID;

@ToString
@EqualsAndHashCode
@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uuid= UUID.randomUUID().toString();

    @Column
    private String credentialName;

    @Column
    private String url;

    @Column
    private String login;

    @Column
    private String password;

    @Column
    private String user;

    public Credential() {
    }

    public Credential(CredentialDTO credentialDTO) {
        this.credentialName = credentialDTO.getCredentialName();
        this.url = credentialDTO.getUrl();
        this.login = credentialDTO.getLogin();
        this.password = credentialDTO.getPassword();
    }

    public Credential(Long id, String uuid, String credentialName, String url, String login, String password, String user) {
        this.id = id;
        this.uuid = uuid;
        this.credentialName = credentialName;
        this.url = url;
        this.login = login;
        this.password = password;
        this.user = user;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
