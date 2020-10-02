package com.ruppyrup.credentialmanager.model;

import com.ruppyrup.credentialmanager.encryption.Encrypt;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
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

    @Encrypt
    @Column
    private String login;

    @Encrypt
    @Column
    private String password;

    @ManyToOne
    @JoinColumn
    private DAOUser user;

    public Credential() {
    }

    public Credential(CredentialDTO credentialDTO) {
        this.credentialName = credentialDTO.getCredentialName();
        this.url = credentialDTO.getUrl();
        this.login = credentialDTO.getLogin();
        this.password = credentialDTO.getPassword();
    }

    public Credential(Long id, String uuid, String credentialName, String url, String login, String password, DAOUser user) {
        this.id = id;
        this.uuid = uuid;
        this.credentialName = credentialName;
        this.url = url;
        this.login = login;
        this.password = password;
        this.user = user;
    }

}
